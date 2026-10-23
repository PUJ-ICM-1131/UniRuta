package com.uniruta.app.data.contacts

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uniruta.app.data.model.EmergencyContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

interface EmergencyContactStore {
    fun contactFor(ownerKey: String): Flow<EmergencyContact?>
    suspend fun save(ownerKey: String, contact: EmergencyContact)
    suspend fun clear(ownerKey: String)
}

private val Context.contactDataStore: DataStore<Preferences> by preferencesDataStore(name = "emergency_contacts")

class DataStoreEmergencyContactStore(context: Context) : EmergencyContactStore {

    private val dataStore = context.applicationContext.contactDataStore

    override fun contactFor(ownerKey: String): Flow<EmergencyContact?> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences ->
            val name = preferences[nameKey(ownerKey)]
            val phone = preferences[phoneKey(ownerKey)]
            if (name.isNullOrBlank() || phone.isNullOrBlank()) null else EmergencyContact(name, phone)
        }
        .distinctUntilChanged()

    override suspend fun save(ownerKey: String, contact: EmergencyContact) {
        dataStore.edit { preferences ->
            preferences[nameKey(ownerKey)] = contact.name
            preferences[phoneKey(ownerKey)] = contact.phoneNumber
        }
    }

    override suspend fun clear(ownerKey: String) {
        dataStore.edit { preferences ->
            preferences.remove(nameKey(ownerKey))
            preferences.remove(phoneKey(ownerKey))
        }
    }

    private fun nameKey(ownerKey: String) = stringPreferencesKey("$ownerKey.name")

    private fun phoneKey(ownerKey: String) = stringPreferencesKey("$ownerKey.phone")
}
