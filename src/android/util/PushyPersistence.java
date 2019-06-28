package me.pushy.sdk.cordova.internal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.config.PushyLogging;
import me.pushy.sdk.util.PushySingleton;

public class PushyPersistence {
    public static final String NOTIFICATION_ICON = "pushyNotificationIcon";
    public static final String PENDING_NOTIFICATIONS = "pushyPendingNotifications";
    public static final String ACTIVE_NOTIFICATIONS = "pushyActiveNotifications";
    public static final String CANCEL_NOTIFICATIONS = "pushyCancelNotifications";

    private static SharedPreferences getSettings(Context context) {
        // Get default app SharedPreferences
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void persistNotification(JSONObject notification, Context context, String type) {
        // Get pending notifications from SharedPreferences
        JSONArray pendingNotifications = getNotifications(context, type);

        // Add new notification
        pendingNotifications.put(notification);

        // Store notification JSON array in SharedPreferences
        getSettings(context).edit().putString(type, pendingNotifications.toString()).commit();
    }

    public static void setNotificationIcon(String icon, Context context) {
        // Store notification icon in SharedPreferences
        getSettings(context).edit().putString(PushyPersistence.NOTIFICATION_ICON, icon).commit();
    }

    public static String getNotificationIcon( Context context) {
        // Get notification icon from SharedPreferences
        return getSettings(context).getString(PushyPersistence.NOTIFICATION_ICON, null);
    }

    public static JSONArray getNotifications(Context context, String type) {
        // Get pending notifications from SharedPreferences
        String pendingNotifications = PushySingleton.getSettings(context).getString(type, null);

        // Prepare JSON array with notifications
        JSONArray json = new JSONArray();

        // Nothing persisted?
        if (pendingNotifications == null) {
            return json;
        }

        try {
            // Attempt to parse string into JSON array
            json = new JSONArray(pendingNotifications);
        }
        catch (JSONException e) {
            // Log error to logcat
            Log.e(PushyLogging.TAG, "Failed to convert JSON string into array:" + e.getMessage(), e);
        }

        // Always return JSON array
        return json;
    }

    public static void clearNotifications(Context context, String type) {
        // Clear the pending notifications from SharedPreferences
        PushySingleton.getSettings(context).edit().remove(type).commit();
    }
}
