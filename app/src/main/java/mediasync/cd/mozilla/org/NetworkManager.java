package mediasync.cd.mozilla.org;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.cache.DiskBasedCache;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.NetUtils;
import com.android.volley.misc.Utils;
import com.android.volley.request.MultiPartRequest;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.util.Map;

import mediasync.cd.mozilla.org.util.Logger;

/**
 * Created by arcturus on 04/11/2016.
 */

public class NetworkManager {
    private RequestQueue mRequestQueue;
    private static Context mContext;
    private static NetworkManager mInstance;
    private final static String SERVER_URL = "http://192.168.0.15:3000/upload";
    //private final static String SERVER_URL = "http://10.239.30.165:3000/upload";
    private Logger mLogger;

    private NetworkManager(Context ctx) {
        mContext = ctx;

        mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());

        mLogger = Logger.getLogger(mContext, mContext.getResources().getString(R.string.bugfender));

    }

    public synchronized static NetworkManager getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new NetworkManager(ctx);
        }
        return mInstance;
    }

    public void uploadFile(final Map<String, String> mediaData, final IUploadListener listener) {
        if (!mediaData.containsKey("_data") ||!mediaData.containsKey("_display_name")) {
            listener.onError(mediaData, "No file information present");
        }

        final String path = mediaData.get("_data");
        final String name = mediaData.get("_display_name");

        mLogger.d("Uploading " + name);

        Request<String> request = new SimpleMultiPartRequest(Request.Method.POST, SERVER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                listener.onResponse(mediaData, response);
                NetworkManager.this.mLogger.d("Upload ok for " + name);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(mediaData, error.getLocalizedMessage());
                NetworkManager.this.mLogger.e("Upload failed for " + name);
            }
        });



        ((SimpleMultiPartRequest)request).addFile("image", path);
        for (Map.Entry<String, String> entry : mediaData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            ((SimpleMultiPartRequest)request).addStringParam(key, value);
        }

        mRequestQueue.add(request);
    }

    public interface IUploadListener {
        public void onResponse(Map<String, String> data, String response);
        public void onError(Map<String, String> data, String error);
    }
}
