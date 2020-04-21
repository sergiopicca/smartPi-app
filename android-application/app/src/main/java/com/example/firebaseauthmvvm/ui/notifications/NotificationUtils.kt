package com.example.firebaseauthmvvm.ui.notifications

import android.app.NotificationManager

/**
 * Builds and delivers a notification.
 *
 * @param messageBody, notification text.
 * @param context, activity context.
 */
// Notification ID.
private val NOTIFICATION_ID = 0

/**
 * Cancels all notifications.
 *
 */
// You need to clear the previous notification
fun NotificationManager.cancelNotifications() {
    cancelAll()
}