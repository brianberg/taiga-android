package co.brianberg.taiga.service;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Request service for managing a volley request queue
 */
public class RequestManager {

  private static RequestManager mInstance;

  private RequestQueue mRequestQueue;

  private RequestManager(Context context) {
    mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
  }

  public static synchronized RequestManager getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new RequestManager(context);
    }
    return mInstance;
  }

  public <T> void addToRequestQueue(Request<T> request) {
    mRequestQueue.add(request);
  }

}
