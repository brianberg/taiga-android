package co.brianberg.taiga.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.brianberg.taiga.auth.User;
import co.brianberg.taiga.db.project.Project;
import co.brianberg.taiga.db.timeline.TimelineEntry;

/**
 * Service for interacting with the Taiga API
 */
public abstract class Taiga {

  public static final String TAG = Taiga.class.getSimpleName();

  public static final String BASE_URL = "https://api.taiga.io/api/v1";

  public static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ssZ";

  public static User signIn(Context context, String username, String password, Response.ErrorListener errorListener) {
    String url = getAbsoluteUrl("/auth");
    try {
      JSONObject data = new JSONObject();
      data.put("type", "normal");
      data.put("username", username);
      data.put("password", password);
      JSONObject response = HttpHelper.requestJsonObject(context, Request.Method.POST, null, url, data, errorListener);
      if (response != null) {
        return new Gson().fromJson(response.toString(), User.class);
      }
    } catch (JSONException e) {
      Log.e(TAG, "Error building sign in request data");
    }
    return null;
  }

  public static List<Project> getProjectList(Context context, int memberId, Response.ErrorListener errorListener) {
    String url = getAbsoluteUrl("/projects?member=" + memberId);
    Map<String, String> headers = getRequestHeaders(context);
    JSONArray response = HttpHelper.requestJsonArray(context, Request.Method.GET, headers, url, null, errorListener);
    if (response != null) {
      Project[] projects = new Gson().fromJson(response.toString(), Project[].class);
      return new ArrayList<>(Arrays.asList(projects));
    }
    return new ArrayList<>();
  }

  public static Project getProject(Context context, int projectId, Response.ErrorListener errorListener) {
    String url = getAbsoluteUrl("/projects/" + projectId);
    Map<String, String> headers = getRequestHeaders(context);
    JSONObject response = HttpHelper.requestJsonObject(context, Request.Method.GET, headers, url, null, errorListener);
    if (response != null) {
      return new Gson().fromJson(response.toString(), Project.class);
    }
    return null;
  }

  public static List<TimelineEntry> getProjectTimeline(Context context, int projectId,
                                                       Response.ErrorListener errorListener) {
    String url = getAbsoluteUrl("/timeline/project/" + projectId);
    Map<String, String> headers = getRequestHeaders(context);
    JSONArray response = HttpHelper.requestJsonArray(context, Request.Method.GET, headers, url, null, errorListener);
    if (response != null) {
      Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
      TimelineEntry[] timelineEntries = gson.fromJson(response.toString(), TimelineEntry[].class);
      return Arrays.asList(timelineEntries);
    }
    return new ArrayList<>();
  }

  private static String getAbsoluteUrl(String relativeUrl) {
    return BASE_URL + relativeUrl;
  }

  private static Map<String, String> getRequestHeaders(Context context) {
    Map<String, String> headers = new HashMap<>();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    if (prefs.contains("user")) {
      User user  = new Gson().fromJson(prefs.getString("user", null), User.class);
      if (user != null) {
        headers.put("Authorization", "Bearer " + user.getAuthToken());
      }
    }
    return headers;
  }

}
