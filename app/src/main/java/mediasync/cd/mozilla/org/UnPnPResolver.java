package mediasync.cd.mozilla.org;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.android.volley.request.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by arcturus on 05/12/2016.
 */

public final class UnPnPResolver {
    private RequestQueue mRequestQueue;
    private static UnPnPResolver mInstance;
    private final static String PING_URL = "https://knilxof.org:4443/ping";

    public static synchronized UnPnPResolver getInstance(Context ctx) {
        if (UnPnPResolver.mInstance == null) {
            mInstance = new UnPnPResolver(ctx);
        }

        return mInstance;
    }

    private UnPnPResolver(Context ctx) {
        this.mRequestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
    }

    public void checkRegistration(final OnUnPnPResolved resolver) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, PING_URL, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                resolver.onResponse(response);
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                resolver.onError(error.getMessage());
            }
        });

        mRequestQueue.add(request);

    }

    public interface OnUnPnPResolved {
        public void onResponse(JSONArray obj);
        public void onError(String reason);
    }
}
