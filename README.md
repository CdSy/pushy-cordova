# pushy-cordova
[![npm version](https://badge.fury.io/js/pushy-cordova.svg)](https://www.npmjs.com/package/pushy-cordova)

The official [Pushy SDK](https://pushy.me/) for [Cordova](https://cordova.apache.org/) apps.

> [Pushy](https://pushy.me/) is the most reliable push notification gateway, perfect for real-time, mission-critical applications.

## Usage

Please refer to our [detailed documentation](https://pushy.me/docs/additional-platforms/cordova) to get started.

Note: There are some additional features.
- Notifications have "wasTapped" prop in the notification listener. When user tapped on notification it will be emitted again but it will have "wasTapped": true

(Features only for Android)
- It's possible to have multiple notifications in the top bar. Notification will come with 'notid' property
- Added dismiss listener. You can use it for detect when user cancel notification. It will emit object with 'notid' prop. For using just call Pushy.setDismissNotificationListener(({notid}) => // to do action).
- Added 'cancel' action. Call Pushy.cancelNotification(notid).
- Added 'cancel all' action. Call Pushy.cancelAllNotifications().
- You can configure to show notifications only when app is running and only when app is in foreground by pass 'onlyWhenRunning' and 'onlyInForeground' to object. e.g. Call Pushy.setConfiguration({onlyWhenRunning: true, onlyInForeground: true}).
- Now when app was started it will receive array of active notifications from Notification manager instead app shared preferences. Notification object contains only 'notid' property.

## License

[Apache 2.0](LICENSE)
