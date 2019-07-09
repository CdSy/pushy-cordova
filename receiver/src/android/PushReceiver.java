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
    if(PushyPersistence.getConfiguration("onlyInForeground", context) && !PushyPlugin.isInForeground()) {
      return;
    }

    String notificationTitle = getAppName(context);
    String notificationText = "";
    int notId = intent.getIntExtra("notid", 1);

    if (intent.getStringExtra("title") != null) {
      notificationTitle = intent.getStringExtra("title");
    }

    // Attempt to extract the notification text from the "message" property of the data payload
    if (intent.getStringExtra("message") != null) {
      notificationText = intent.getStringExtra("message");
    }

    Map<String, String> pendingIntentData = new HashMap<String, String>();

    if (intent.getExtras() != null) {
			for (String key : intent.getExtras().keySet()) {
        String stringValue = intent.getExtras().getString(key);
        int intValue = intent.getExtras().getInt(key);

        if (stringValue == null) {
          pendingIntentData.put(key, Integer.toString(intValue));
        } else {
          pendingIntentData.put(key, stringValue);
        }
      }
    }

    pendingIntentData.put("notid", Integer.toString(notId));

    PendingIntent activePendingIntent = getPendingIntent(context, pendingIntentData, "activity");
    PendingIntent dismissPendingIntent = getPendingIntent(context, pendingIntentData, "service");

    int colorCode = Color.parseColor("#1e9ee0") ;

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

  private PendingIntent getPendingIntent(Context context, Map<String, String> pendingIntentData, String type) {
    try {
      Intent intent;
      
      if (type == "activity") {
        intent = new Intent(context, PushyActivity.class);
      } else {
        intent = new Intent(context, PushyDismissService.class);
      }

      int requestCode = 0;
      
      try {
        requestCode = Integer.parseInt(pendingIntentData.get("notid"));
      } catch (NumberFormatException e) {
        Log.d(PushyLogging.TAG, " ===>  Couldn't convert string notId to int notId");
      }
  
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
  
      for (String key : pendingIntentData.keySet()) {
        if (pendingIntentData.get(key) != null) {
          intent.putExtra(key, pendingIntentData.get(key));
        }
      }
  
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
