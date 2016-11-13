package mediasync.cd.mozilla.org.alarms;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mediasync.cd.mozilla.org.model.Config;

/**
 * Created by arcturus on 13/11/2016.
 */

public class AlarmBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            if (Config.syncEnabled(context)) {
                AlarmManager.getInstance(context).setAlarm(true);
            }
        }
    }
}
