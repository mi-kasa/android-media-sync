package mediasync.cd.mozilla.org.util;

/**
 * Created by arcturus on 12/11/2016.
 */

import android.content.Context;
import android.support.v7.appcompat.BuildConfig;

import com.bugfender.sdk.Bugfender;

public final class Logger {
    private static Logger mLogger;

    private Logger(Context ctx, String id) {
        Bugfender.init(ctx, id, true);
        Bugfender.enableLogcatLogging();
    }

    public static synchronized Logger getLogger(Context ctx, String id) {
        if (mLogger == null) {
            mLogger = new Logger(ctx, id);
        }

        return mLogger;
    }

    public void d(String msg) {
        Bugfender.d("DEBUG", msg);
    }

    public void w(String msg) {
        Bugfender.w("WARNING", msg);
    }

    public void e(String msg) {
        Bugfender.e("ERROR", msg);
    }
}
