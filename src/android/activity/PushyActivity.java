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

public class PushyActivity extends Activity {
  private static String TAG = "PushyPlugin";
  /*
   * this activity will be started if the user touches a notification.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    JSONObject json = new JSONObject();

    if (getIntent().getExtras() != null) {
      try {
        json.put("wasTapped", true);
      } catch (JSONException e) {
        Log.e(PushyLogging.TAG, "Failed to insert +wasTapped+ extra into JSONObject:" + e.getMessage());
        return;
      }

			for (String key : getIntent().getExtras().keySet()) {
        String value = getIntent().getExtras().getString(key);

        try {
          json.put(key, value);
        } catch (JSONException e) {
          // Log error to logcat and stop execution
          Log.e(PushyLogging.TAG, "Failed to insert intent extra into JSONObject:" + e.getMessage());
          return;
        }
      }
    }
		
		sendPushPayload(json);
    finish();
    forceMainActivityReload();
  }

  private void sendPushPayload(JSONObject data) {
    PushyPlugin.onClickNotification(data, getApplicationContext());
  }

  private void forceMainActivityReload() {
    PackageManager pm = getPackageManager();
    String packageName;

    try {
      packageName = getApplicationContext().getPackageName();
      Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
      startActivity(launchIntent);
    } catch (Exception e) {
      Log.d(TAG, "==> PushyPluginActivity couldnt forceMainActivityReload" + e.getMessage());
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancelAll();
  }
	
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "==> PushyPluginActivity onStart");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "==> PushyPluginActivity onStop");
  }

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "==> PushyPluginActivity onDestroy");
	}
}
