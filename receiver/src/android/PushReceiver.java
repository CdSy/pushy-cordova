package me.pushy.sdk;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import me.pushy.sdk.config.PushyLogging;
import me.pushy.sdk.cordova.internal.PushyPlugin;
import me.pushy.sdk.cordova.internal.util.PushyPersistence;
import me.pushy.sdk.cordova.internal.activity.PushyActivity;
import me.pushy.sdk.cordova.internal.service.PushyDismissService;

public class PushReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    // Notification title and text
    if(PushyPersistence.getConfiguration("onlyWhenRunning", context) && !PushyPlugin.isApplicationRunning()) {
      return;
    }

    if(PushyPersistence.getConfiguration("onlyInForeground", context) && !PushyPlugin.isInForeground()) {
      return;
    }

    String notificationTitle = getAppName(context);
    String notificationText = "";
    int notId = (int) (System.currentTimeMillis() / 1000);

    if (intent.getStringExtra("title") != null) {
      notificationTitle = intent.getStringExtra("title");
    }

    // Attempt to extract the notification text from the "message" property of the data payload
    if (intent.getStringExtra("message") != null) {
      notificationText = intent.getStringExtra("message");
    }

    JSONObject json = new JSONObject();

    if (intent.getExtras() != null) {
      Bundle bundle = intent.getExtras();
      Set<String> keys = bundle.keySet();

			for (String key : keys) {
        try {
          // Attempt to insert the key and its value into the JSONObject
          json.put(key, bundle.get(key));
        }
        catch (JSONException e) {
          // Log error to logcat and stop execution
          Log.e(PushyLogging.TAG, "Failed to insert intent extra into JSONObject:" + e.getMessage(), e);
          return;
        }
      }
    }

    intent.putExtra("notid", notId);

    try {
      json.put("notid", notId);
      json.put("wasTapped", false);
    }
    catch (JSONException e) {
      // Log error to logcat and stop execution
      Log.e(PushyLogging.TAG, "Failed to insert additional data into JSONObject:" + e.getMessage(), e);
      return;
    }

    PendingIntent activePendingIntent = getPendingIntent(context, intent, "activity");
    PendingIntent dismissPendingIntent = getPendingIntent(context, intent, "service");

    int colorCode = Color.parseColor("#1e9ee0");

    // Prepare a notification with vibration and sound
    Notification.Builder builder = new Notification.Builder(context)
      .setAutoCancel(true)
      .setContentTitle(notificationTitle)
      .setContentText(notificationText)
      .setStyle(
        new Notification.BigTextStyle()
          .bigText(notificationText)
      )
      .setVibrate(new long[]{0, 400, 250, 400})
      .setColor(colorCode)
      .setSmallIcon(getNotificationIcon(context))
      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

    if (activePendingIntent != null) {
      builder.setContentIntent(activePendingIntent);
    }

    if (dismissPendingIntent != null) {
      builder.setDeleteIntent(dismissPendingIntent);
    }

    // Get an instance of the NotificationManager service
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

    // Automatically configure a Notification Channel for devices running Android O+
    Pushy.setNotificationChannel(builder, context);

    // Build the notification and display it
    notificationManager.notify(notId, builder.build());
    PushyPlugin.onNotificationReceived(json, context);
  }

  private int getNotificationIcon(Context context) {
    // Attempt to fetch icon name from SharedPreferences
    String icon = PushyPersistence.getNotificationIcon(context);
    
    // Did we configure a custom icon?
    if (icon != null) {
      // Cache app resources
      Resources resources = context.getResources();

      // Cache app package name
      String packageName = context.getPackageName();

      // Look for icon in drawable folders
      int iconId = resources.getIdentifier(icon, "drawable", packageName);

      // Found it?
      if (iconId != 0) {
          return iconId;
      }

      // Look for icon in mipmap folders
      iconId = resources.getIdentifier(icon, "mipmap", packageName);

      // Found it?
      if (iconId != 0) {
          return iconId;
      }
    }

    // Fallback to generic icon
    return android.R.drawable.ic_dialog_info;
  }

  private static String getAppName(Context context) {
    // Attempt to determine app name via package manager
    return context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
  }

  private PendingIntent getPendingIntent(Context context, Intent pendingIntentData, String type) {
    try {
      Intent intent;
      
      if (type == "activity") {
        intent = new Intent(context, PushyActivity.class);
      } else {
        intent = new Intent(context, PushyDismissService.class);
      }

      int requestCode = pendingIntentData.getIntExtra("notid", 0);
  
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtras(pendingIntentData);
  
      PendingIntent pendingIntent;
      
      if (type == "activity") {
        pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
      } else {
        pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
      }

      return pendingIntent;
    } catch (Exception e) {
      Log.e(PushyLogging.TAG, "Failed create PendingIntent for Notification" + e.getMessage());
      return null;
    }
  }
}
