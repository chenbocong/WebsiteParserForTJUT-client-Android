package org.orange.querysystem;

import org.orange.querysystem.content.AccountSettingPreference;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;

public class SettingsActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    /**
     * 设置项“帐号”的KEY
     */
    public static final String KEY_PREF_ACCOUNT = "pref_account";

    /**
     * 设置项“帐号”中学号的KEY
     */
    public static final String KEY_PREF_ACCOUNT_STUDENT_ID = KEY_PREF_ACCOUNT
            + AccountSettingPreference.STUDENT_ID_SUFFIX;

    /**
     * 设置项“帐号”中密码的KEY
     */
    public static final String KEY_PREF_ACCOUNT_PASSWORD = KEY_PREF_ACCOUNT
            + AccountSettingPreference.PASSWORD_SUFFIX;

    public static final String KEY_PREF_REQUEST_PASSWORD_FOR_PRIVATE_INFORMATION =
            "pref_request_password_for_private_information";

    /**
     * 设置项“第0周”的KEY
     */
    public static final String KEY_PREF_SCHOOL_STARTING_DATE = "pref_startingDate";

    /**
     * 设置项“自动更新通知”的KEY
     */
    public static final String KEY_PREF_UPDATE_POST_AUTOMATICALLY
            = "pref_update_post_automatically";

    /**
     * 设置项“通知更新间隔”的KEY
     */
    public static final String KEY_PREF_INTERVAL_OF_POST_UPDATING
            = "pref_interval_of_post_updating";

    /**
     * 设置项“移动网络下使用后备方案”的KEY
     */
    public static final String KEY_PREF_USE_ALTERNATIVE_IN_MOBILE_CONNECTION
            = "pref_use_alternative_in_mobile_connection";

    @SuppressWarnings("deprecation")
    @TargetApi(11)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        refreshSummaryOfIntervalOfPostUpdating();

        getListView().setBackgroundResource(R.drawable.back);
        getListView().setCacheColorHint(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar mActionBar = getActionBar();
            mActionBar.setTitle(R.string.settings);
            //横屏时，为节省空间隐藏ActionBar
            if (getResources().getConfiguration().orientation ==
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                mActionBar.hide();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_INTERVAL_OF_POST_UPDATING)) {
            refreshSummaryOfIntervalOfPostUpdating();
        }
    }

    @SuppressWarnings("deprecation")
    private void refreshSummaryOfIntervalOfPostUpdating() {
        ListPreference intervalPref = (ListPreference) findPreference(
                KEY_PREF_INTERVAL_OF_POST_UPDATING);
        // Set summary to be the user-description for the selected value
        String newValue = getResources().getString(R.string.pref_interval_of_post_updating_summary,
                intervalPref.getEntry().toString());
        intervalPref.setSummary(newValue);
    }

    /**
     * 取得指定周的周一。以周一为一周的开始。
     *
     * @param milliseconds 目标周中的一个时间戳，单位ms
     * @return milliseconds所在周的周一。小时、分、秒、毫秒都为0
     */
    public static Calendar getMondayOfWeek(long milliseconds) {
        Calendar result = Calendar.getInstance();
        result.setFirstDayOfWeek(Calendar.MONDAY);
        result.setTimeInMillis(milliseconds);
        result.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        result.set(Calendar.HOUR_OF_DAY, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }

    /**
     * 取得第0周的星期一
     *
     * @param context 上下文环境
     * @return 如果设置过开学日期，返回第0周的星期一；如果尚没设置开学时间，返回null
     */
    public static Calendar getMondayOfZeroWeek(Context context) {
        long schoolStarting = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(SettingsActivity.KEY_PREF_SCHOOL_STARTING_DATE, -1);
        if (schoolStarting == -1) {
            return null;
        } else {
            return getMondayOfWeek(schoolStarting);
        }
    }

    /**
     * 根据{@link #getMondayOfZeroWeek(Context)}，<strong>推断</strong>当前学年。
     * <p><strong>注意</strong>：当前的推断是基于上下学期（只有第一、二学期）的，可能不准确</p>
     *
     * @param context 上下文环境
     * @return 推断结果，如2013表示当前是2013~2014学年
     * @see #getCurrentSemester(Context)
     */
    public static int getCurrentAcademicYear(Context context) {
        Calendar calendar = SettingsActivity.getMondayOfZeroWeek(context);
        int year = calendar.get(Calendar.YEAR);
        if (getCurrentSemester(context) != 1) {
            year--;
        }
        return year;
    }

    /**
     * 根据{@link #getMondayOfZeroWeek(Context)}，<strong>推断</strong>当前学期。
     * <p><strong>注意</strong>：当前的推断是基于上下学期（只有第一、二学期）的，可能不准确</p>
     *
     * @param context 上下文环境
     * @return 推断结果，如1表示第一学期
     * @see #getCurrentAcademicYear(Context)
     */
    public static byte getCurrentSemester(Context context) {
        Calendar calendar = getMondayOfZeroWeek(context);
        int month = calendar.get(Calendar.MONTH) - Calendar.JANUARY + 1;
        month++; // 第0周有可能在假期所在月，为提高判断的准确性，以后一个月判断
        int semester = (month >= 3 && month <= 8) ? 2 : 1; //TODO 第三小学期怎么判断？
        return (byte) semester;
    }

    /**
     * 返回现在距指定时间的周数
     *
     * @param timestampOfZeroWeek 起始时间戳，单位ms
     * @return 现在距指定时间的周次
     */
    public static int getCurrentWeekNumber(long timestampOfZeroWeek) {
        return (int) ((new Date().getTime() - timestampOfZeroWeek) / (7L * 24 * 60 * 60 * 1000));
    }

    /**
     * 取得当前周次。以周一为一周的开始
     *
     * @param context 上下文环境
     * @return 如果设置过开学时间，返回当前周次；如果尚没设置开学时间，返回null
     */
    public static Integer getCurrentWeekNumber(Context context) {
        Calendar c = getMondayOfZeroWeek(context);
        if (c == null) {
            return null;
        } else {
            return getCurrentWeekNumber(c.getTimeInMillis());
        }
    }

    /**
     * 已经设置了教务处帐号（学号）和密码
     *
     * @param context 上下文环境
     * @return 如果教务处帐号（学号）和密码都已经设置了，返回true；否则返回false
     */
    public static boolean hasSetAccountStudentIDAndPassword(Context context) {
        String username = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PREF_ACCOUNT_STUDENT_ID, null);
        String password = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PREF_ACCOUNT_PASSWORD, null);
        return username != null && password != null;
    }

    /**
     * 取得账号的学号
     *
     * @param context 上下文环境
     * @return 如果设置过学号，返回此学号；如果尚没设置学号，返回null
     */
    public static String getAccountStudentID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PREF_ACCOUNT_STUDENT_ID, null);
    }

    /**
     * 取得账号的密码
     *
     * @param context 上下文环境
     * @return 如果设置过密码，返回此密码；如果尚没设置密码，返回null
     */
    public static String getAccountPassword(Context context) {
        String username = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PREF_ACCOUNT_STUDENT_ID, null);
        String password = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PREF_ACCOUNT_PASSWORD, null);
        if (password == null) {
            return null;
        } else {
            return AccountSettingPreference.decrypt(
                    AccountSettingPreference.getStoragePassword(username), password);
        }
    }

    /**
     * 显示私密信息（例如成绩、个人信息）前询问密码
     *
     * @param context 上下文环境
     * @return 如果需要检查，返回true；如果不需要，返回false
     */
    public static boolean requestPasswordForPrivateInformation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_PREF_REQUEST_PASSWORD_FOR_PRIVATE_INFORMATION, false);
    }

    /**
     * 是否自动更新通知
     *
     * @param context 上下文环境
     * @return 如果应当自动更新通知，返回true；如果已禁用自动更新通知，返回false
     */
    public static boolean isUpdatePostAutomatically(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_PREF_UPDATE_POST_AUTOMATICALLY, true);
    }

    /**
     * 取得通知更新的间隔时间
     *
     * @param context 上下文环境
     * @return 通知更新的间隔时间。单位：毫秒
     */
    public static long getIntervalOfPostUpdating(Context context) {
        String updateIntervalString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PREF_INTERVAL_OF_POST_UPDATING, null);
        long updateInterval = 4L * 24 * 60 * 60 * 1000;
        if (updateIntervalString != null) {
            updateInterval = Long.parseLong(updateIntervalString);
        }
        return updateInterval;
    }

    /**
     * 移动网络下，当首选网络更新通知方案失效时，是否使用后备方案（费较多流量）。
     *
     * @param context 上下文环境
     * @return 如果使用，返回true；如果禁止使用，返回false
     */
    public static boolean useAlternativeInMobileConnection(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_PREF_USE_ALTERNATIVE_IN_MOBILE_CONNECTION, false);
    }
}
