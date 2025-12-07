package com.jrlabs.audiocity.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jrlabs.audiocity.MainActivity
import com.jrlabs.audiocity.R
import com.jrlabs.audiocity.data.model.Stop
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para gestión de notificaciones locales
 * Equivalente a NotificationService.swift en iOS
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "stop_arrival_channel"
        const val CHANNEL_NAME = "Llegada a paradas"
        const val CHANNEL_DESCRIPTION = "Notificaciones cuando llegas a un punto de interés"

        const val ACTION_LISTEN = "com.jrlabs.audiocity.ACTION_LISTEN"
        const val ACTION_SKIP = "com.jrlabs.audiocity.ACTION_SKIP"

        const val EXTRA_STOP_ID = "extra_stop_id"
        const val EXTRA_STOP_NAME = "extra_stop_name"
    }

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    private val _lastActionStopId = MutableStateFlow<String?>(null)
    val lastActionStopId: StateFlow<String?> = _lastActionStopId.asStateFlow()

    private val _lastAction = MutableStateFlow<NotificationAction?>(null)
    val lastAction: StateFlow<NotificationAction?> = _lastAction.asStateFlow()

    init {
        createNotificationChannel()
        checkAuthorizationStatus()
    }

    /**
     * Verificar estado de autorización
     */
    fun checkAuthorizationStatus() {
        _isAuthorized.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Crear canal de notificaciones (requerido para Android 8+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Mostrar notificación de llegada a parada
     */
    suspend fun showStopArrivalNotification(stop: Stop) {
        if (!_isAuthorized.value) return

        // Intent para abrir la app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_STOP_ID, stop.id)
            putExtra(EXTRA_STOP_NAME, stop.name)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            stop.id.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para acción "Escuchar"
        val listenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_LISTEN
            putExtra(EXTRA_STOP_ID, stop.id)
            putExtra(EXTRA_STOP_NAME, stop.name)
        }
        val listenPendingIntent = PendingIntent.getBroadcast(
            context,
            stop.id.hashCode() + 1,
            listenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para acción "Saltar"
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SKIP
            putExtra(EXTRA_STOP_ID, stop.id)
            putExtra(EXTRA_STOP_NAME, stop.name)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            stop.id.hashCode() + 2,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Has llegado a un punto de interés")
            .setContentText(stop.name)
            .setStyle(NotificationCompat.BigTextStyle().bigText(stop.description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_media_play,
                "Escuchar",
                listenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Saltar",
                skipPendingIntent
            )

        // Intentar cargar imagen si existe
        stop.imageUrl?.let { imageUrl ->
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val url = URL(imageUrl)
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                }
                bitmap?.let {
                    builder.setLargeIcon(it)
                    builder.setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(it)
                            .bigLargeIcon(null as android.graphics.Bitmap?)
                            .setSummaryText(stop.description)
                    )
                }
            } catch (e: Exception) {
                // Si falla la carga de imagen, continuamos sin ella
            }
        }

        // Mostrar notificación
        try {
            NotificationManagerCompat.from(context).notify(
                stop.id.hashCode(),
                builder.build()
            )
        } catch (e: SecurityException) {
            // Permiso de notificaciones no concedido
            _isAuthorized.value = false
        }
    }

    /**
     * Cancelar todas las notificaciones pendientes
     */
    fun cancelAllPendingNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /**
     * Cancelar notificación para una parada específica
     */
    fun cancelNotification(stopId: String) {
        NotificationManagerCompat.from(context).cancel(stopId.hashCode())
    }

    /**
     * Procesar acción de notificación
     */
    fun handleNotificationAction(action: String, stopId: String) {
        _lastActionStopId.value = stopId
        _lastAction.value = when (action) {
            ACTION_LISTEN -> NotificationAction.LISTEN
            ACTION_SKIP -> NotificationAction.SKIP
            else -> null
        }
    }

    /**
     * Limpiar última acción
     */
    fun clearLastAction() {
        _lastActionStopId.value = null
        _lastAction.value = null
    }
}

/**
 * Acciones de notificación
 */
enum class NotificationAction {
    LISTEN,
    SKIP
}
