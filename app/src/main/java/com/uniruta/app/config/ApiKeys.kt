package com.uniruta.app.config

import com.uniruta.app.BuildConfig

object ApiKeys {

    private const val PLACEHOLDER = "DEFAULT_API_KEY"

    val mapsConfigured: Boolean
        get() = isConfigured(BuildConfig.MAPS_API_KEY)

    val openRouteService: String
        get() = BuildConfig.ORS_API_KEY

    val firebaseTestRoles: String
        get() = BuildConfig.FIREBASE_TEST_ROLES

    fun isConfigured(value: String?): Boolean =
        !value.isNullOrBlank() && value.trim() != PLACEHOLDER
}
