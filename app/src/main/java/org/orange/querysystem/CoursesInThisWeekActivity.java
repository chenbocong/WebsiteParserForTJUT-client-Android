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
package org.orange.querysystem;

import org.orange.parser.entity.Course;
import org.orange.parser.entity.Course.TimeAndAddress;
import org.orange.querysystem.AllCoursesActivity.CourseToSimpleCourse;
import org.orange.querysystem.content.InsertDBFragmentActivity;
import org.orange.querysystem.content.ListCoursesFragment;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;
import org.orange.querysystem.content.TabsAdapter;
import org.orange.querysystem.util.Network;
import org.orange.querysystem.util.ReadDB;
import org.orange.querysystem.util.ReadDB.OnPostExcuteListerner;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CoursesInThisWeekActivity extends FragmentActivity implements OnPostExcuteListerner {

    private static final int REQUEST_UPDATE_COURSES_FROM_NETWORK = 1;

    private static final int DIALOG_NO_COURSES_IN_DATABASE = 1;

    private static final int DIALOG_INCORRECT_ID_OR_PASSWORD = 2;

    protected TabHost mTabHost;

    private ViewPager mViewPager;

    protected TabsAdapter mTabsAdapter;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_tabs_pager);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

        setTitle();

        readDB();
    }

    /**
     * 设置标题区域
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void setTitle() {
        TextView currentTime = (TextView) findViewById(R.id.currentTime);
        String title = getString(R.string.curriculum_schedule_in_this_week);
        Integer weekNumber = SettingsActivity.getCurrentWeekNumber(this);
        if (weekNumber != null) {
            title += "(" + getString(R.string.week_of_semester, weekNumber) + ")";
        }
        //3.0以及以上版本，使用ActionBar，3.0以下版本使用currentTime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //隐藏低版本用的标题
            currentTime.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            ActionBar mActionBar = getActionBar();
            mActionBar.setTitle(title);
            //横屏时，为节省空间隐藏ActionBar
            if (getResources().getConfiguration().orientation ==
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                mActionBar.hide();
            }
        } else {
            currentTime.setText(title + "\t\t" + DateFormat.getDateInstance().format(new Date()));
        }
    }

    public void readDB() {
        new ReadDB(this, this).execute(SettingsActivity.getAccountStudentID(this), "this");
    }

    @Override
    public void onPostReadFromDB(List<Course> courses) {
        if (courses != null && !courses.isEmpty()) {
            showCoursesInfo(courses, mCourseToSimpleCourse);
        } else {
            showDialogFragment(DIALOG_NO_COURSES_IN_DATABASE);
        }
    }

    public void showCoursesInfo(List<Course> courses, CourseToSimpleCourse converter) {
        mTabsAdapter.clear();

        Bundle[] args = new Bundle[8];
        List<SimpleCourse>[][] lesson = AllCoursesActivity.getTimeTable(courses, converter,
                SettingsActivity.getCurrentWeekNumber(this));

        //把每天的课程放到传到ListCoursesFragment的参数容器中
        for (int dayOfWeek = 0; dayOfWeek <= 7; dayOfWeek++) {
            ArrayList<SimpleCourse> coursesInADay = new ArrayList<SimpleCourse>();
            for (int period = 1; period < lesson[dayOfWeek].length; period++) {
                for (SimpleCourse course : lesson[dayOfWeek][period]) {
                    coursesInADay.add(course);
                }
            }
            Bundle argForFragment = new Bundle();
            argForFragment.putParcelableArrayList(ListCoursesFragment.COURSES_KEY, coursesInADay);
            args[dayOfWeek] = argForFragment;
        }
        //交换周日args[0]和时间未定args[7]，把周日显示在最后
        Bundle temp = args[0];
        args[0] = args[7];
        args[7] = temp;

        String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week);

        for (int day = 0; day <= 7; day++) {
            TabSpec tabSpec = mTabHost.newTabSpec(daysOfWeek[day]);
            mTabsAdapter.addTab(tabSpec.setIndicator(daysOfWeek[day]),
                    ListCoursesFragment.class, args[day]);
        }

        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        mTabHost.setCurrentTab(dayOfWeek != Calendar.SUNDAY ? dayOfWeek - Calendar.SUNDAY : 7);
    }

    private static final CourseToSimpleCourse mCourseToSimpleCourse = new CourseToSimpleCourse() {
        @Override
        public SimpleCourse toSimpleCourse(Course course,
                TimeAndAddress timeAndAddress, Integer period) {
            SimpleCourse result;
            if (timeAndAddress != null) {
                result = new SimpleCourse(course.getId(), course.getName(),
                        period != null ? String.valueOf(period) : null,
                        timeAndAddress.getAddress());
            } else {
                result = new SimpleCourse(course.getId(), course.getName(), null, null);
            }
            return result;
        }
    };

    /* (non-Javadoc)
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.refresh);
        menu.add(0, 2, 2, R.string.settings);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == 1) {
            if (Network.isConnected(this)) {
                startActivityForResult(new Intent(this, InsertDBFragmentActivity.class),
                        REQUEST_UPDATE_COURSES_FROM_NETWORK);
            } else {
                Network.openNoConnectionDialog(this);
            }
        } else if (item.getItemId() == 2) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE_COURSES_FROM_NETWORK) {
            switch (resultCode) {
                case InsertDBFragmentActivity.RESULT_OK:
                    readDB();
                    break;
                case InsertDBFragmentActivity.RESULT_CANNOT_LOGIN:
                case InsertDBFragmentActivity.RESULT_NO_STUDENT_ID_OR_PASSWORD:
                    showDialogFragment(DIALOG_INCORRECT_ID_OR_PASSWORD);
                    break;
            }
        }
    }

    private void showDialogFragment(int dialogCode) {
        switch (dialogCode) {
            case DIALOG_NO_COURSES_IN_DATABASE:
                new NoCoursesDialogFragment().show(getSupportFragmentManager(),
                        "NoCoursesInDatabaseDialog");
                break;
            case DIALOG_INCORRECT_ID_OR_PASSWORD:
                new IncorrectIdOrPasswordDialogFragment().show(getSupportFragmentManager(),
                        "IncorrectIdOrPasswordDialog");
                break;
            default:
                throw new IllegalArgumentException("非法参数：" + dialogCode);
        }
    }

    public static class NoCoursesDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.no_courses_in_database_dialog_title)
                    .setMessage(R.string.no_courses_in_database_dialog_message)
                    .setPositiveButton(R.string.no_courses_in_database_dialog_positive,
                            new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().startActivityForResult(
                                            new Intent(getActivity(),
                                                    InsertDBFragmentActivity.class),
                                            REQUEST_UPDATE_COURSES_FROM_NETWORK
                                    );
                                }
                            }
                    )
                    .setNegativeButton(R.string.no_courses_in_database_dialog_negative, null)
                    .create();
        }
    }

    public static class IncorrectIdOrPasswordDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.incorrect_id_or_password_dialog_title)
                    .setMessage(R.string.incorrect_id_or_password_dialog_message)
                    .setPositiveButton(R.string.incorrect_id_or_password_dialog_positive,
                            new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(
                                            new Intent(getActivity(), SettingsActivity.class));
                                }
                            }
                    )
                    .setNegativeButton(R.string.incorrect_id_or_password_dialog_negative, null)
                    .create();
        }
    }
}
