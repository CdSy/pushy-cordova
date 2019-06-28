package me.pushy.sdk.cordova.internal;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.config.PushyLogging;
import me.pushy.sdk.cordova.internal.util.PushyPersistence;
import me.pushy.sdk.util.exceptions.PushyException;

public class PushyPlugin extends CordovaPlugin {
  private static final String TAG = "PushyPlugin";
  private static PushyPlugin mInstance;
  private static Context mContext;
  private CallbackContext mNotificationHandler;
  private CallbackContext mDismissNotificationHandler;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    // Store plugin instance
    Log.e(PushyLogging.TAG, "===> PushyPlugin has been initialized");
    mInstance = this;
  }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        // Run all plugin actions in background thread
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                // Restart the socket service
                if (action.equals("listen")) {
                    Pushy.listen(cordova.getActivity());
                }

                // Register devices
                if (action.equals("register")) {
                    register(callbackContext);
                }

                // Listen for notifications
                if (action.equals("setNotificationListener")) {
                    setNotificationListener(callbackContext);
                }

                // Listen for dismiss notifications
                if (action.equals("setDismissNotificationListener")) {
                  setDismissNotificationListener(callbackContext);
                }

                // Request WRITE_EXTERNAL_STORAGE permission
                if (action.equals("requestStoragePermission")) {
                    requestStoragePermission();
                }

                // Check if device is registered
                if (action.equals("isRegistered")) {
                    isRegistered(callbackContext);
                }

                // Subscribe device to topic
                if (action.equals("subscribe")) {
                    subscribe(args, callbackContext);
                }

                // Unsubscribe device from topic
                if (action.equals("unsubscribe")) {
                    unsubscribe(args, callbackContext);
                }

                // Pushy Enterprise support
                if (action.equals("setEnterpriseConfig")) {
                    setEnterpriseConfig(args, callbackContext);
                }

                // Pushy Enterprise custom certificate support
                if (action.equals("setEnterpriseCertificate")) {
                    setEnterpriseCertificate(args, callbackContext);
                }

                // Custom icon support
                if (action.equals("setNotificationIcon")) {
                    setNotificationIcon(args);
                }

                // Cancel a previously shown notification
                if (action.equals("cancelNotification")) {
                    cancelNotification(args, callbackContext, cordova.getActivity());
                }
            }
        });

        // Always return true regardless of action validity
        return true;
    }

    private void cancelNotification(JSONArray args, CallbackContext callbackContext, Context context) {
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

      int notId;

      try {
        notId = args.getInt(0);
      } catch (JSONException e) {
        Log.e(PushyLogging.TAG, "Failed get +notId+ from JSONObject:" + e.getMessage(), e);
        return;
      }

      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, args);

      // Keep the callback valid for future use
      pluginResult.setKeepCallback(true);
      // Invoke the JavaScript callback
      callbackContext.sendPluginResult(pluginResult);
      // Cancel notification
      notificationManager.cancel(notId);
    }

    private void setNotificationListener(CallbackContext callbackContext) {
        // Save notification listener callback for later
        mNotificationHandler = callbackContext;

        // Attempt to deliver any pending notifications
        deliverPendingNotifications();
        deliverActiveNotifications();
    }

    private void setDismissNotificationListener(CallbackContext callbackContext) {
      // Save dismiss notification listener callback for later
      mDismissNotificationHandler = callbackContext;
      deliverCancelNotifications();
    }

    private void deliverPendingNotifications() {
        // Activity must be running for this to work
        if (!isActivityRunning()) {
            return;
        }

        // Get pending notifications
        JSONArray notifications = PushyPersistence.getNotifications(cordova.getActivity(), PushyPersistence.PENDING_NOTIFICATIONS);

        // Got at least one?
        if (notifications.length() > 0) {
            // Traverse notifications
            for (int i = 0; i < notifications.length(); i++) {
                try {
                    // Emit notification to listener
                    onNotificationReceived(notifications.getJSONObject(i), cordova.getActivity());
                }
                catch (JSONException e) {
                    // Log error to logcat
                    Log.e(PushyLogging.TAG, "Failed to parse JSON object:" + e.getMessage(), e);
                }
            }

            // Clear persisted notifications
            PushyPersistence.clearNotifications(cordova.getActivity(), PushyPersistence.PENDING_NOTIFICATIONS);
        }
    }

    private void deliverActiveNotifications() {
        // Activity must be running for this to work
        if (!isActivityRunning()) {
            return;
        }

        // Get pending notifications
        JSONArray notifications = PushyPersistence.getNotifications(cordova.getActivity(), PushyPersistence.ACTIVE_NOTIFICATIONS);

        // Got at least one?
        if (notifications.length() > 0) {
            // Traverse notifications
            for (int i = 0; i < notifications.length(); i++) {
                try {
                    // Emit notification to listener
                    onClickNotification(notifications.getJSONObject(i));
                }
                catch (JSONException e) {
                    // Log error to logcat
                    Log.e(PushyLogging.TAG, "Failed to parse JSON object:" + e.getMessage(), e);
                }
            }

            // Clear persisted notifications
            PushyPersistence.clearNotifications(cordova.getActivity(), PushyPersistence.ACTIVE_NOTIFICATIONS);
        }
    }

    private void deliverCancelNotifications() {
        // Activity must be running for this to work
        if (!isActivityRunning()) {
            return;
        }

        // Get pending notifications
        JSONArray notifications = PushyPersistence.getNotifications(cordova.getActivity(), PushyPersistence.CANCEL_NOTIFICATIONS);

        // Got at least one?
        if (notifications.length() > 0) {
            // Traverse notifications
            for (int i = 0; i < notifications.length(); i++) {
                try {
                    // Emit notification to listener
                    onDismissNotification(notifications.getJSONObject(i));
                }
                catch (JSONException e) {
                    // Log error to logcat
                    Log.e(PushyLogging.TAG, "Failed to parse JSON object:" + e.getMessage(), e);
                }
            }

            // Clear persisted notifications
            PushyPersistence.clearNotifications(cordova.getActivity(), PushyPersistence.CANCEL_NOTIFICATIONS);
        }
    }

    private void requestStoragePermission() {
        // Request permission method
        Method requestPermission;

        try {
            // Get method reference via reflection (to support Cordova Android 4.0)
            requestPermission = CordovaInterface.class.getMethod("requestPermission", CordovaPlugin.class, int.class, String.class);

            // Request the permission via user-friendly dialog
            requestPermission.invoke(cordova, this, 0, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } catch (Exception e) {
            // Log error
            Log.d(PushyLogging.TAG, "Failed to request WRITE_EXTERNAL_STORAGE permission", e);
        }
    }

    private boolean isActivityRunning() {
        // Cache activity object
        Activity activity = cordova.getActivity();

        // Check whether activity exists and is not finishing up or destroyed
        return activity != null && ! activity.isFinishing();
    }

    public static void onNotificationReceived(JSONObject notification, Context context) {
        // Activity is not running or no notification handler defined?
        if (mInstance == null || !mInstance.isActivityRunning() || mInstance.mNotificationHandler == null) {
            mContext = context;
            // Store notification JSON in SharedPreferences and deliver it when app is opened
            PushyPersistence.persistNotification(notification, context, PushyPersistence.PENDING_NOTIFICATIONS);
            return;
        }

        // We're live, prepare a plugin result object that allows invoking the notification listener multiple times
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, notification);

        // Keep the callback valid for future use
        pluginResult.setKeepCallback(true);

        // Invoke the JavaScript callback
        mInstance.mNotificationHandler.sendPluginResult(pluginResult);
    }

    public static void onClickNotification(JSONObject notification) {
        if (mInstance == null || !mInstance.isActivityRunning() || mInstance.mNotificationHandler == null) {
            // Store notification JSON in SharedPreferences and deliver it when app is opened
            PushyPersistence.persistNotification(notification, mContext, PushyPersistence.ACTIVE_NOTIFICATIONS);
            return;
        }

         // We're live, prepare a plugin result object that allows invoking the notification listener multiple times
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, notification);

        // Keep the callback valid for future use
        pluginResult.setKeepCallback(true);

        // Invoke the JavaScript callback
        mInstance.mNotificationHandler.sendPluginResult(pluginResult);
    }

    public static void onDismissNotification(JSONObject notification) {
        if (mInstance == null || !mInstance.isActivityRunning() || mInstance.mDismissNotificationHandler == null) {
            // Store notification JSON in SharedPreferences and deliver it when app is opened
            PushyPersistence.persistNotification(notification, mContext, PushyPersistence.CANCEL_NOTIFICATIONS);
            return;
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, notification);

        // Keep the callback valid for future use
        pluginResult.setKeepCallback(true);
        // Invoke the JavaScript callback
        mInstance.mDismissNotificationHandler.sendPluginResult(pluginResult);
    }

    private void register(final CallbackContext callback) {
        try {
            // Assign a unique token to this device
            String deviceToken = Pushy.register(cordova.getActivity());

            // Resolve the callback with the token
            callback.success(deviceToken);
        }
        catch (PushyException exc) {
            // Reject the callback with the exception
            callback.error(exc.getMessage());
        }
    }

    private void isRegistered(CallbackContext callback) {
        // Resolve the callback with boolean result
        callback.sendPluginResult(new PluginResult(PluginResult.Status.OK, Pushy.isRegistered(cordova.getActivity())));
    }

    private void setEnterpriseConfig(JSONArray args, CallbackContext callback) {
        try {
            // Attempt to set Enterprise endpoints
            Pushy.setEnterpriseConfig(args.getString(0), args.getString(1), cordova.getActivity());

            // Resolve the callback with success
            callback.success();
        }
        catch (Exception exc) {
            // Reject the callback with the exception
            callback.error(exc.getMessage());
        }
    }

    private void setEnterpriseCertificate(JSONArray args, CallbackContext callback) {
        try {
            // Attempt to set custom certificate
            Pushy.setEnterpriseCertificate(args.getString(0), cordova.getActivity());

            // Resolve the callback with success
            callback.success();
        }
        catch (Exception exc) {
            // Reject the callback with the expection
            callback.error(exc.getMessage());
        }
    }

    private void setNotificationIcon(JSONArray args) {
        String iconResourceName;

        try {
            // Attempt to get icon resource name from first parameter
            iconResourceName = args.getString(0);
        } catch (JSONException e) {
            return;
        }

        // Store in SharedPreferences using PushyPersistence helper
        PushyPersistence.setNotificationIcon(iconResourceName, cordova.getActivity());
    }

    private void subscribe(JSONArray args, CallbackContext callback) {
        try {
            // Attempt to subscribe the device to topic
            Pushy.subscribe(args.getString(0), cordova.getActivity());

            // Resolve the callback with success
            callback.success();
        }
        catch (Exception exc) {
            // Reject the callback with the exception
            callback.error(exc.getMessage());
        }
    }

    private void unsubscribe(JSONArray args, CallbackContext callback) {
        try {
            // Attempt to unsubscribe the device from topic
            Pushy.unsubscribe(args.getString(0), cordova.getActivity());

            // Resolve the callback with success
            callback.success();
        }
        catch (Exception exc) {
            // Reject the callback with the exception
            callback.error(exc.getMessage());
        }
    }
}
