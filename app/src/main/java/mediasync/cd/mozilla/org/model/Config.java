package mediasync.cd.mozilla.org.model;

import android.content.Context;

/**
 * Created by arcturus on 13/11/2016.
 */

public class Config {

    public static boolean syncEnabled(Context ctx) {
        return ctx.
                getSharedPreferences("preferences", Context.MODE_PRIVATE).
                getBoolean("syncenabled", false);
    }

    public static void setSyncEnabled(Context ctx, boolean value) {
        ctx.
                getSharedPreferences("preferences", Context.MODE_PRIVATE).
                edit().
                putBoolean("syncenabled", value).commit();
    }
}
