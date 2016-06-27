package co.brianberg.taiga.service;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for http requests
 */
public abstract class HttpHelper {

  private static final int REQUEST_TIMEOUT = 30;

  public static JSONObject requestJsonObject(Context context, int method, Map<String, String> headers, String url,
                                             JSONObject params, Response.ErrorListener errorListener) {
    RequestFuture<JSONObject> future = RequestFuture.newFuture();
    final Map<String, String> reqHeaders = headers;
    JsonObjectRequest request = new JsonObjectRequest(method, url, params, future, future) {
      public Map<String, String> getHeaders() {
        return reqHeaders != null ? reqHeaders : new HashMap<String, String>();
      }
    };
    RequestManager.getInstance(context).addToRequestQueue(request);
    try {
      return future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
    } catch (Exception e) {
      errorListener.onErrorResponse(new VolleyError(e));
    }
    return null;
  }

  public static JSONArray requestJsonArray(Context context, int method, Map<String, String> headers, String url,
                                           JSONObject params, Response.ErrorListener errorListener) {
    RequestFuture<JSONArray> future = RequestFuture.newFuture();
    final Map<String, String> reqHeaders = headers;
    JsonArrayRequest request = new JsonArrayRequest(method, url, params, future, future) {
      public Map<String, String> getHeaders() {
        return reqHeaders != null ? reqHeaders : new HashMap<String, String>();
      }
    };
    RequestManager.getInstance(context).addToRequestQueue(request);
    try {
      return future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);
    } catch (Exception e) {
      errorListener.onErrorResponse(new VolleyError(e));
    }
    return null;
  }

  
}
