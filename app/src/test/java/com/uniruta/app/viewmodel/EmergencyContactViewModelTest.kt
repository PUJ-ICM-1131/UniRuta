package com.uniruta.app.viewmodel

import com.uniruta.app.data.contacts.EmergencyContactStore
import com.uniruta.app.data.model.EmergencyContact
import com.uniruta.app.device.contacts.ContactPhoneReader
import com.uniruta.app.device.contacts.ContactReadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmergencyContactViewModelTest {

    private class InMemoryStore : EmergencyContactStore {
        val contacts = MutableStateFlow<Map<String, EmergencyContact>>(emptyMap())
        override fun contactFor(ownerKey: String): Flow<EmergencyContact?> =
            contacts.map { it[ownerKey] }.distinctUntilChanged()
        override suspend fun save(ownerKey: String, contact: EmergencyContact) {
            contacts.update { it + (ownerKey to contact) }
        }
        override suspend fun clear(ownerKey: String) {
            contacts.update { it - ownerKey }
        }
    }

    private val mother = EmergencyContact("Marta Gómez", "+57 300 123 4567")
    private val brother = EmergencyContact("Julián Gómez", "3105550000")
    private val store = InMemoryStore()
    private val picked = mutableMapOf(
        "content://contacts/1" to ContactReadResult.Success(mother),
        "content://contacts/2" to ContactReadResult.Success(brother),
        "content://contacts/3" to ContactReadResult.Failure("El contacto seleccionado no tiene un número telefónico.")
    )
    private lateinit var viewModel: EmergencyContactViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = EmergencyContactViewModel(store, ContactPhoneReader { uri -> picked.getValue(uri) })
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val state get() = viewModel.uiState.value

    @Test
    fun pickedContactIsStoredForTheCurrentIdentity() {
        viewModel.bind("firebase:uid-ana")
        assertTrue(state.isLoaded)
        assertNull(state.contact)

        viewModel.onContactPicked("content://contacts/1")

        assertEquals(mother, state.contact)
        assertEquals(mother, store.contacts.value["firebase:uid-ana"])
    }

    @Test
    fun contactCanBeChangedAndRemoved() {
        viewModel.bind("demo:est-001")
        viewModel.onContactPicked("content://contacts/1")
        viewModel.onContactPicked("content://contacts/2")
        assertEquals(brother, state.contact)

        viewModel.removeContact()

        assertNull(state.contact)
        assertNull(store.contacts.value["demo:est-001"])
    }

    @Test
    fun contactsAreNeverSharedBetweenIdentitiesAndSurviveLogout() {
        viewModel.bind("firebase:uid-ana")
        viewModel.onContactPicked("content://contacts/1")

        viewModel.bind(null)
        assertNull(state.contact)
        viewModel.bind("demo:est-001")
        assertNull(state.contact)

        viewModel.bind("firebase:uid-ana")
        assertEquals(mother, state.contact)
    }

    @Test
    fun contactWithoutNumberShowsAnErrorAndKeepsThePreviousOne() {
        viewModel.bind("demo:est-001")
        viewModel.onContactPicked("content://contacts/1")

        viewModel.onContactPicked("content://contacts/3")

        assertEquals(mother, state.contact)
        assertEquals("El contacto seleccionado no tiene un número telefónico.", state.errorMessage)
        viewModel.clearError()
        assertNull(state.errorMessage)
    }

    @Test
    fun withoutIdentityNothingIsStored() {
        viewModel.onContactPicked("content://contacts/1")

        assertTrue(store.contacts.value.isEmpty())
        assertNull(state.contact)
    }
}
