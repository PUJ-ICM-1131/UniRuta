package com.uniruta.app.device.contacts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContract
import com.uniruta.app.data.model.EmergencyContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ContactReadResult {
    data class Success(val contact: EmergencyContact) : ContactReadResult
    data class Failure(val message: String) : ContactReadResult
}

fun interface ContactPhoneReader {
    suspend fun read(contactUri: String): ContactReadResult
}

class PickContactPhone : ActivityResultContract<Unit, Uri?>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_PICK).setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent?.data?.takeIf { resultCode == Activity.RESULT_OK }
}

class AndroidContactPhoneReader(context: Context) : ContactPhoneReader {

    private val resolver = context.applicationContext.contentResolver

    override suspend fun read(contactUri: String): ContactReadResult = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        try {
            resolver.query(Uri.parse(contactUri), projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val name = cursor.getString(0)?.trim().orEmpty()
                val number = cursor.getString(1)?.trim().orEmpty()
                when {
                    number.isEmpty() -> ContactReadResult.Failure("El contacto seleccionado no tiene un número telefónico.")
                    else -> ContactReadResult.Success(EmergencyContact(name.ifEmpty { number }, number))
                }
            } ?: ContactReadResult.Failure("No fue posible leer el contacto seleccionado.")
        } catch (error: SecurityException) {
            ContactReadResult.Failure("UniRuta no recibió acceso al contacto seleccionado. Inténtalo de nuevo.")
        } catch (error: IllegalArgumentException) {
            ContactReadResult.Failure("No fue posible leer el contacto seleccionado.")
        }
    }
}
