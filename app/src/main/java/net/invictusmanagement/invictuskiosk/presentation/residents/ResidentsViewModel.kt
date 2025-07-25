package net.invictusmanagement.invictuskiosk.presentation.residents

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKeyState
import net.invictusmanagement.invictuskiosk.domain.repository.ResidentsRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.UiEvent
import javax.inject.Inject

@HiltViewModel
class ResidentsViewModel @Inject constructor(
    private val repository: ResidentsRepository,
    private val dataStoreManager: DataStoreManager
):ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow

    private val _residentsState = MutableStateFlow(ResidentState())
    val residentsState: StateFlow<ResidentState> = _residentsState

    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    val kioskActivationCode = dataStoreManager.activationCodeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    private val _keyValidationState = mutableStateOf(DigitalKeyState())
    val keyValidationState: State<DigitalKeyState> = _keyValidationState

    fun loadInitialData(){
        viewModelScope.launch {
            dataStoreManager.accessPointFlow.collect {
                _accessPoint.value = it
            }
        }
    }

    fun getResidentsByName(filter: String, byName: String) {
        repository.getResidentsByName(filter, byName).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _residentsState.value =ResidentState(residents = result.data?: emptyList())
                }

                is Resource.Error -> {
                    _residentsState.value = ResidentState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _residentsState.value = ResidentState(isLoading = true)
                }

            }
        }.launchIn(viewModelScope)
    }

    fun getResidentByUnitNumber(unitNumber: String) {
        repository.getResidentsByUnitNumber(unitNumber).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _residentsState.value =ResidentState(residents = result.data?: emptyList())
                }

                is Resource.Error -> {
                    _residentsState.value = ResidentState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _residentsState.value = ResidentState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun validateDigitalKey(digitalKeyDto: DigitalKeyDto) {
        repository.validateDigitalKey(digitalKeyDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _keyValidationState.value = DigitalKeyState(digitalKey = result.data)
                }

                is Resource.Error -> {
                    _eventFlow.emit(
                        UiEvent.ShowError(
                            Constants.DIGITAL_KEY_GENERIC_ERROR
                        )
                    )
                }

                is Resource.Loading -> {
                    _keyValidationState.value = DigitalKeyState(isLoading = true)
                }

            }
        }.launchIn(viewModelScope)
    }

    fun getAllLeasingAgents(byName: String) {
        repository.getAllLeasingAgents(byName).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _residentsState.value =ResidentState(residents = result.data?: emptyList())
                }

                is Resource.Error -> {
                    _residentsState.value = ResidentState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _residentsState.value = ResidentState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}