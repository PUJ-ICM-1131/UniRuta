package com.uniruta.app.ui.permissions

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

fun Context.hasLocationPermission(): Boolean = locationPermissions.any { permission ->
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun rememberLocationPermissionRequest(onResult: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { callback() }
    return remember(context, launcher) {
        {
            if (context.hasLocationPermission()) callback() else launcher.launch(locationPermissions)
        }
    }
}

fun Context.openAppSettings() {
    launchSettings(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    )
}

fun Context.openLocationSettings() {
    launchSettings(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

private fun Context.launchSettings(intent: Intent) {
    try {
        startActivity(intent)
    } catch (error: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}

fun Context.openDialer(phoneNumber: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)))
    true
} catch (error: ActivityNotFoundException) {
    false
}
