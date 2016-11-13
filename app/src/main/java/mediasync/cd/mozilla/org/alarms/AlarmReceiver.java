package mediasync.cd.mozilla.org.alarms;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import mediasync.cd.mozilla.org.R;
import mediasync.cd.mozilla.org.SyncService;
import mediasync.cd.mozilla.org.model.Config;
import mediasync.cd.mozilla.org.model.PowerUtil;
import mediasync.cd.mozilla.org.util.Logger;

/**
 * Created by arcturus on 13/11/2016.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Config.syncEnabled(context)) {
            final Logger logger = Logger.getLogger(context, context.getString(R.string.bugfender));
            if (!PowerUtil.isConnected(context)) {
                logger.d("Sync not initialized because phone is not charging");
                return;
            }
            // Start sync
            Intent serviceIntent = new Intent(context, SyncService.class);
            serviceIntent.putExtra("command", SyncService.COMMAND_SYNC);
            context.startService(serviceIntent);
        }
    }
}
