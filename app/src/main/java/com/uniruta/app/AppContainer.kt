package com.uniruta.app

import android.content.Context
import com.uniruta.app.auth.AuthGateway
import com.uniruta.app.auth.FirebaseAuthGateway
import com.uniruta.app.auth.RoleDirectory
import com.uniruta.app.config.ApiKeys
import com.uniruta.app.data.contacts.DataStoreEmergencyContactStore
import com.uniruta.app.data.contacts.EmergencyContactStore
import com.uniruta.app.data.routing.OpenRouteServiceClient
import com.uniruta.app.data.routing.RoutePlanner
import com.uniruta.app.data.storage.ImageStore
import com.uniruta.app.data.storage.LocalImageStore
import com.uniruta.app.device.contacts.AndroidContactPhoneReader
import com.uniruta.app.device.contacts.ContactPhoneReader
import com.uniruta.app.device.location.FusedLocationTracker
import com.uniruta.app.device.location.LocationTracker
import com.uniruta.app.device.sensors.AndroidMotionSensors
import com.uniruta.app.device.sensors.MotionSensorSource
import com.uniruta.app.notifications.AndroidNotifier
import com.uniruta.app.notifications.AppNotifier

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val authGateway: AuthGateway by lazy { FirebaseAuthGateway(appContext) }

    val roleDirectory: RoleDirectory by lazy { RoleDirectory.parse(ApiKeys.firebaseTestRoles) }

    val routePlanner: RoutePlanner by lazy {
        RoutePlanner(OpenRouteServiceClient(apiKey = ApiKeys.openRouteService))
    }

    val locationTracker: LocationTracker by lazy { FusedLocationTracker(appContext) }

    val motionSensors: MotionSensorSource by lazy { AndroidMotionSensors(appContext) }

    val imageStore: ImageStore by lazy { LocalImageStore(appContext) }

    val notifier: AppNotifier by lazy { AndroidNotifier(appContext) }

    val emergencyContactStore: EmergencyContactStore by lazy { DataStoreEmergencyContactStore(appContext) }

    val contactPhoneReader: ContactPhoneReader by lazy { AndroidContactPhoneReader(appContext) }
}
