package com.uniruta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uniruta.app.UniRutaApplication
import com.uniruta.app.data.contacts.EmergencyContactStore
import com.uniruta.app.data.model.EmergencyContact
import com.uniruta.app.device.contacts.ContactPhoneReader
import com.uniruta.app.device.contacts.ContactReadResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmergencyContactUiState(
    val ownerKey: String? = null,
    val contact: EmergencyContact? = null,
    val isLoaded: Boolean = false,
    val isBusy: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class EmergencyContactViewModel(
    private val store: EmergencyContactStore,
    private val reader: ContactPhoneReader
) : ViewModel() {

    private val owner = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(EmergencyContactUiState())
    val uiState: StateFlow<EmergencyContactUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            owner
                .flatMapLatest { key ->
                    if (key == null) {
                        flowOf(null to null)
                    } else {
                        store.contactFor(key)
                            .map { key to it }
                            .catch { emit(key to null) }
                    }
                }
                .collect { (key, contact) ->
                    _uiState.update { state ->
                        if (state.ownerKey != key) state else state.copy(contact = contact, isLoaded = key != null)
                    }
                }
        }
    }

    fun bind(ownerKey: String?) {
        if (owner.value == ownerKey) return
        _uiState.value = EmergencyContactUiState(ownerKey = ownerKey)
        owner.value = ownerKey
    }

    fun onContactPicked(contactUri: String) {
        val key = owner.value ?: return
        if (_uiState.value.isBusy) return
        _uiState.update { it.copy(isBusy = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = reader.read(contactUri)) {
                is ContactReadResult.Success -> {
                    store.save(key, result.contact)
                    _uiState.update { if (it.ownerKey == key) it.copy(isBusy = false) else it }
                }
                is ContactReadResult.Failure -> _uiState.update {
                    if (it.ownerKey == key) it.copy(isBusy = false, errorMessage = result.message) else it
                }
            }
        }
    }

    fun removeContact() {
        val key = owner.value ?: return
        _uiState.update { it.copy(errorMessage = null) }
        viewModelScope.launch { store.clear(key) }
    }

    fun onPickerUnavailable() {
        _uiState.update { it.copy(errorMessage = "No hay una aplicación de contactos disponible en este dispositivo.") }
    }

    fun onDialerUnavailable() {
        _uiState.update { it.copy(errorMessage = "No hay una aplicación de teléfono disponible para llamar.") }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as UniRutaApplication
                EmergencyContactViewModel(
                    store = application.container.emergencyContactStore,
                    reader = application.container.contactPhoneReader
                )
            }
        }
    }
}
