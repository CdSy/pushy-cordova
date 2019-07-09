# pushy-cordova
[![npm version](https://badge.fury.io/js/pushy-cordova.svg)](https://www.npmjs.com/package/pushy-cordova)

The official [Pushy SDK](https://pushy.me/) for [Cordova](https://cordova.apache.org/) apps.

> [Pushy](https://pushy.me/) is the most reliable push notification gateway, perfect for real-time, mission-critical applications.

## Usage

Please refer to our [detailed documentation](https://pushy.me/docs/additional-platforms/cordova) to get started.

Note: There is some additional features. (only for Android for now)
- Notifications have "wasTapped" prop in the notification listener. When user tapped on notification it will be emitted again but it will have "wasTapped": true
- It's possible to have multiple notifications in the top bar. You should provide unique 'notid' prop in the data
- Added dismiss listener. You can use it for detect when user cancel notification. It will emit object with 'notid' prop. For using just call Pushy.setDismissNotificationListener(({notid}) => // to do action).
- Added 'cancel' action. Call Pushy.cancelNotification(notid).
- Added 'cancel all' action. Call Pushy.cancelAllNotifications().
- You can configure to show notifications only when app is running. Call Pushy.onlyInForeground(true).
- Now when app was started it will receive array of active notifications from Notification manager instead app shared preferences. Notification object contains only 'notid' property.

## License

[Apache 2.0](LICENSE)
