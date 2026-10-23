package com.uniruta.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uniruta.app.notifications.NotificationDestination
import com.uniruta.app.notifications.NotificationTarget
import com.uniruta.app.notifications.clearNotificationTarget
import com.uniruta.app.notifications.notificationTarget
import com.uniruta.app.ui.navigation.UniRutaApp
import com.uniruta.app.ui.theme.UniRutaTheme
import kotlinx.coroutines.flow.MutableStateFlow

private const val STATE_TARGET_DESTINATION = "pending_target_destination"
private const val STATE_TARGET_ENTITY = "pending_target_entity"

class MainActivity : ComponentActivity() {

    private val pendingTarget = MutableStateFlow<NotificationTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            consumeNotificationIntent(intent)
        } else {
            pendingTarget.value = savedInstanceState.restoreTarget()
        }
        setContent {
            val target by pendingTarget.collectAsStateWithLifecycle()
            UniRutaTheme {
                UniRutaApp(
                    pendingTarget = target,
                    onTargetHandled = { pendingTarget.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeNotificationIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pendingTarget.value?.let { target ->
            outState.putString(STATE_TARGET_DESTINATION, target.destination.name)
            outState.putString(STATE_TARGET_ENTITY, target.entityId)
        }
    }

    private fun consumeNotificationIntent(intent: Intent?) {
        val target = intent?.notificationTarget() ?: return
        intent.clearNotificationTarget()
        pendingTarget.value = target
    }

    private fun Bundle.restoreTarget(): NotificationTarget? {
        val destination = getString(STATE_TARGET_DESTINATION)
            ?.let { name -> NotificationDestination.entries.firstOrNull { it.name == name } }
            ?: return null
        return NotificationTarget(destination, getString(STATE_TARGET_ENTITY))
    }
}
