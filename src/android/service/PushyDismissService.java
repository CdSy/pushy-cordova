package me.pushy.sdk.cordova.internal.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.config.PushyLogging;
import me.pushy.sdk.cordova.internal.PushyPlugin;

public class PushyDismissService extends IntentService {
  private static String TAG = "PushyPlugin";

  public PushyDismissService() {
    super("PushyDismissService");
  }

  /*
  * this activity will be started if the user dismiss a notification.
  */
  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    JSONObject json = new JSONObject();

    if (intent.getExtras() != null) {
      try {
        json.put("notid", intent.getExtras().getString("notid"));
        sendPushPayload(json);
      } catch (JSONException e) {
        Log.e(PushyLogging.TAG, "Failed to insert +notid+ extra into JSONObject:" + e.getMessage());
      }
    }
  }

  private void sendPushPayload(JSONObject data) {
    PushyPlugin.onDismissNotification(data, getApplicationContext());
  }

  @Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "==> PushyIntentService onDestroy");
	}
}
