/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orange.querysystem.content;

import org.orange.querysystem.R;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class that implements the management of tabs and all
 * details of connecting a ViewPager with associated TabHost.  It relies on a
 * trick.  Normally a tab host has a simple API for supplying a View or
 * Intent that each tab will show.  This is not sufficient for switching
 * between pages.  So instead we make the content part of the tab host
 * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
 * view to show as the tab content.  It listens to changes in tabs, and takes
 * care of switch to the correct paged in the ViewPager whenever the selected
 * tab changes.
 */
public class TabsAdapter extends FragmentPagerAdapter
        implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

    private final Context mContext;

    private final TabHost mTabHost;

    private final ViewPager mViewPager;

    private final List<WeakReference<Fragment>> mFragmentPagers =new ArrayList<>();

    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    static final class TabInfo {

        //        private final String tag;
        private final Class<?> clss;

        private final Bundle args;

        TabInfo(Class<?> _class, Bundle _args) {
//            tag = _tag;
            clss = _class;
            args = _args;
        }
    }

    static class DummyTabFactory implements TabHost.TabContentFactory {

        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
        super(activity.getSupportFragmentManager());
        mContext = activity;
        mTabHost = tabHost;
        mTabHost.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                adjustSelectedTabToCenter();
            }
        });
        mViewPager = pager;
        mTabHost.setOnTabChangedListener(this);
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
        tabSpec.setContent(new DummyTabFactory(mContext));
//        String tag = tabSpec.getTag();

        TabInfo info = new TabInfo(clss, args);
        mTabs.add(info);
        notifyDataSetChanged();
        mTabHost.addTab(tabSpec);
        setTabForIfLowerThanHONEYCOMB();
    }

    private void setTabForIfLowerThanHONEYCOMB() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            View child = mTabHost.getTabWidget().getChildAt(getCount() - 1);
            child.setBackgroundColor(0xFFB5E61D);
            TextView tv = (TextView) child.findViewById(android.R.id.title);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); //取消文字底边对齐  
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); //设置文字居中对齐  
            params.setMargins(dp2px(10), dp2px(4), dp2px(10), dp2px(4));
            child.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        }
    }

    private int dp2px(int dp) {
        return (int) (mTabHost.getContext().getResources().getDisplayMetrics().density * dp
                + 0.5f);
    }

    /**
     * 清空Tabs
     */
    public void clear() {
        mTabs.clear();
        mFragmentPagers.clear();
        mTabHost.setCurrentTab(0);
        mTabHost.clearAllTabs();
        mViewPager.removeAllViews();
        notifyDataSetChanged();
    }

    public List<Fragment> getAddedFragmentPagers() {
        ArrayList<Fragment> result = new ArrayList<>();
        for(WeakReference<Fragment> ref : mFragmentPagers) {
            Fragment f = ref.get();
            if(f != null) {
                result.add(f);
            }
        }
        return result;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        TabInfo info = mTabs.get(position);
        Fragment fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
        mFragmentPagers.add(new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public void onTabChanged(String tabId) {
        int position = mTabHost.getCurrentTab();
        adjustSelectedTabToCenter();
        mViewPager.setCurrentItem(position);
    }

    public void adjustSelectedTabToCenter() {
        View currentTab = mTabHost.getCurrentTabView();
        if (currentTab == null) {
            return;
        }
        HorizontalScrollView hsv = (HorizontalScrollView) mTabHost.findViewById(R.id.tabs_scroll);
        hsv.smoothScrollTo(currentTab.getLeft() + (currentTab.getWidth() - hsv.getWidth()) / 2, 0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        // Unfortunately when TabHost changes the current tab, it kindly
        // also takes care of putting focus on it when not in touch mode.
        // The jerk.
        // This hack tries to prevent this from pulling focus out of our
        // ViewPager.
        TabWidget widget = mTabHost.getTabWidget();
        int oldFocusability = widget.getDescendantFocusability();
        widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mTabHost.setCurrentTab(position);
        widget.setDescendantFocusability(oldFocusability);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
