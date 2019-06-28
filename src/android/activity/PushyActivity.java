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
		Log.d(TAG, "==> PushyPluginActivity onCreate");

    JSONObject json = new JSONObject();

    if (getIntent().getExtras() != null) {
      Log.d(TAG, "==> USER TAPPED NOTFICATION");
      try {
        json.put("wasTapped", true);
      } catch (JSONException e) {
        Log.e(PushyLogging.TAG, "Failed to insert +wasTapped+ extra into JSONObject:" + e.getMessage());
        return;
      }

			for (String key : getIntent().getExtras().keySet()) {
        String value = getIntent().getExtras().getString(key);
        Log.d(TAG, "\tKey: " + key + " Value: " + value);

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

  @Override
  protected void onNewIntent(Intent intent) {
    String notid = "defaultId";

    if (intent.getExtras() != null) {
      notid = intent.getExtras().getString("notid");
    }

    Log.d(TAG, "==> PushyPluginActivity onNewIntent" + notid);

    if (intent.getExtras() != null) {
      Log.d(TAG, "==> PushyPluginActivity onNewIntent notID: " + intent.getExtras().getString("notid"));
    } else {
      Log.d(TAG, "==> PushyPluginActivity onNewIntent Cant read notID");
    }
  }

  private void sendPushPayload(JSONObject data) {
    PushyPlugin.onClickNotification(data);
  }

  private void forceMainActivityReload() {
    Log.e(PushyLogging.TAG, "Force MainActivity Reload");
    PackageManager pm = getPackageManager();
    Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
    startActivity(launchIntent);
  }

  @Override
  protected void onResume() {
    super.onResume();
	  Log.d(TAG, "==> PushyPluginActivity onResume");
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
