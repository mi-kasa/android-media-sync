package mediasync.cd.mozilla.org.alarms;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.Calendar;

import mediasync.cd.mozilla.org.model.Config;

/**
 * Created by arcturus on 13/11/2016.
 */

public class AlarmManager {

    private android.app.AlarmManager mAlarmManager;
    private static AlarmManager mInstance;
    private Context mContext;

    private AlarmManager(Context ctx) {
        mAlarmManager = (android.app.AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        mContext = ctx;
    }

    public synchronized static AlarmManager getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new AlarmManager(ctx);
        }

        return mInstance;
    }

    public void setAlarm(boolean enabled) {
        if (enabled) {
            createAlarm();
        } else {
            clearAlarm();
        }
    }

    private void createAlarm() {
        PendingIntent alarmIntent = getAlarmIntent();

        // Set the alarm to start at 4:00 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 0);

        mAlarmManager.setRepeating(android.app.AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                1000 * 60 * 60 * 24,
                alarmIntent);

        // Enable on boot receiver
        ComponentName receiver = new ComponentName(mContext, AlarmBoot.class);
        PackageManager pm = mContext.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Config.setSyncEnabled(mContext, true);
    }

    public void clearAlarm() {
        PendingIntent alarmIntent = getAlarmIntent();
        mAlarmManager.cancel(alarmIntent);

        // Disable the on boot receiver
        ComponentName receiver = new ComponentName(mContext, AlarmBoot.class);
        PackageManager pm = mContext.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Config.setSyncEnabled(mContext, false);
    }

    private PendingIntent getAlarmIntent() {
        Intent intent = new Intent(mContext, AlarmReceiver.class);
        return PendingIntent.getBroadcast(mContext, 0, intent, 0);
    }
}
