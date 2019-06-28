package me.pushy.sdk.cordova.internal.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.config.PushyLogging;
import me.pushy.sdk.cordova.internal.PushyPlugin;

public class PushyDismissActivity extends Activity {
    private static String TAG = "PushyPlugin";
  /*
   * this activity will be started if the user dismiss a notification.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
		Log.d(TAG, "==> PushyDismissActivity onCreate");

    JSONObject json = new JSONObject();

    if (getIntent().getExtras() != null) {
      Log.d(TAG, "==> USER DISMISSED NOTFICATION:" + getIntent().getExtras().getString("notid"));

      try {
        json.put("notid", getIntent().getExtras().getString("notid"));
      } catch (JSONException e) {
        Log.e(PushyLogging.TAG, "Failed to insert +notid+ extra into JSONObject:" + e.getMessage());
        return;
      }
    }
		
		sendPushPayload(json);
    finish();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    String notid = "defaultId";

    if (intent.getExtras() != null) {
      notid = intent.getExtras().getString("notid");
    }

    Log.d(TAG, "==> PushyDismissActivity onNewIntent" + notid);

    if (intent.getExtras() != null) {
      Log.d(TAG, "==> PushyDismissActivity onNewIntent notID: " + intent.getExtras().getString("notid"));
    } else {
      Log.d(TAG, "==> PushyDismissActivity onNewIntent Cant read notID");
    }
  }

  private void sendPushPayload(JSONObject data) {
    PushyPlugin.onDismissNotification(data);
  }

  @Override
  protected void onResume() {
    super.onResume();
	  Log.d(TAG, "==> PushyDismissActivity onResume");
  }
	
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "==> PushyDismissActivity onStart");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "==> PushyDismissActivity onStop");
  }
  
  @Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "==> PushyDismissActivity onDestroy");
	}
}
