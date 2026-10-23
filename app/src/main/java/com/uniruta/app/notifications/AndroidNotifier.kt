package com.uniruta.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.uniruta.app.MainActivity
import com.uniruta.app.R

private const val EXTRA_DESTINATION = "com.uniruta.app.extra.NOTIFICATION_DESTINATION"
private const val EXTRA_ENTITY_ID = "com.uniruta.app.extra.NOTIFICATION_ENTITY_ID"
private const val BRAND_COLOR = 0xFF12325F.toInt()

fun Context.canPostNotifications(): Boolean {
    val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    return permissionGranted && NotificationManagerCompat.from(this).areNotificationsEnabled()
}

fun Intent.notificationTarget(): NotificationTarget? {
    val destination = getStringExtra(EXTRA_DESTINATION)
        ?.let { name -> NotificationDestination.entries.firstOrNull { it.name == name } }
        ?: return null
    return NotificationTarget(destination, getStringExtra(EXTRA_ENTITY_ID))
}

fun Intent.clearNotificationTarget() {
    removeExtra(EXTRA_DESTINATION)
    removeExtra(EXTRA_ENTITY_ID)
}

class AndroidNotifier(context: Context) : AppNotifier {

    private val appContext = context.applicationContext

    init {
        createChannels()
    }

    override fun notify(notification: AppNotification) {
        if (!appContext.canPostNotifications()) return

        val publicVersion = NotificationCompat.Builder(appContext, notification.channel.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle("UniRuta")
            .setContentText("Tienes una actualización en UniRuta.")
            .build()

        val built = NotificationCompat.Builder(appContext, notification.channel.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle(notification.title)
            .setContentText(notification.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setContentIntent(contentIntentFor(notification))
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(appContext).notify(notification.key.hashCode(), built)
        } catch (error: SecurityException) {
            Unit
        }
    }

    private fun contentIntentFor(notification: AppNotification): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_DESTINATION, notification.target.destination.name)
            .putExtra(EXTRA_ENTITY_ID, notification.target.entityId)
        return PendingIntent.getActivity(
            appContext,
            notification.key.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createChannels() {
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return
        NotificationChannelSpec.entries.forEach { spec ->
            val channel = NotificationChannel(spec.id, spec.title, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = spec.description
            }
            manager.createNotificationChannel(channel)
        }
    }
}
