package com.jrlabs.audiocity.domain.service

import com.jrlabs.audiocity.domain.model.Stop

/**
 * Interface for notification management.
 * Follows Single Responsibility Principle - only handles notifications.
 */
interface NotificationService {

    /**
     * Shows a notification for arriving at a stop.
     * @param stop The stop the user has arrived at.
     */
    suspend fun showStopArrivedNotification(stop: Stop)

    /**
     * Creates a foreground service notification for location tracking.
     * @param routeName The name of the active route.
     */
    fun createForegroundNotification(routeName: String): android.app.Notification

    /**
     * Cancels all notifications.
     */
    fun cancelAllNotifications()

    /**
     * Checks if notification permission is granted (Android 13+).
     */
    fun hasNotificationPermission(): Boolean
}
