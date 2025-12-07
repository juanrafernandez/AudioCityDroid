package com.jrlabs.audiocity.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver para manejar acciones de notificaciones
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationService: NotificationService

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val stopId = intent.getStringExtra(NotificationService.EXTRA_STOP_ID) ?: return

        // Procesar la acción
        notificationService.handleNotificationAction(action, stopId)

        // Cancelar la notificación
        notificationService.cancelNotification(stopId)
    }
}
