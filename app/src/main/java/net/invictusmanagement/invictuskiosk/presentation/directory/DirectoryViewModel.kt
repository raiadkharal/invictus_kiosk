package net.invictusmanagement.invictuskiosk.presentation.directory

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
import net.invictusmanagement.invictuskiosk.domain.model.UnitList
import net.invictusmanagement.invictuskiosk.domain.repository.DirectoryRepository
import net.invictusmanagement.invictuskiosk.domain.repository.HomeRepository
import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.invictuskiosk.presentation.home.HomeViewModel
import net.invictusmanagement.invictuskiosk.presentation.residents.ResidentState
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import net.invictusmanagement.invictuskiosk.util.UiEvent
import javax.inject.Inject

@HiltViewModel
class DirectoryViewModel @Inject constructor(
    private val repository: DirectoryRepository,
    private val homeRepository: HomeRepository,
    private val dataStoreManager: DataStoreManager,
    private val relayRepository: RelayManagerRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val isConnected = networkMonitor.isConnected
    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    private val _residentState = MutableStateFlow(ResidentState())
    val residentState: StateFlow<ResidentState> = _residentState

    val activationCode = dataStoreManager.activationCodeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    private val _unitList = MutableStateFlow<List<UnitList>?>(emptyList())
    val unitList: StateFlow<List<UnitList>?> = _unitList

    private val _keyValidationState = MutableStateFlow(DigitalKeyState())
    val keyValidationState: StateFlow<DigitalKeyState> = _keyValidationState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow


    fun loadInitialData() {
        viewModelScope.launch {
            dataStoreManager.accessPointFlow.collect {
                _accessPoint.value = it
            }
        }
    }

    fun getUnitList() {
        repository.getUnitList().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _unitList.value = result.data
                }

                is Resource.Error -> {
                    _unitList.value = result.data
                }

                is Resource.Loading -> {
                    _unitList.value = null
                }
            }
        }.launchIn(viewModelScope)
    }

    fun validateDigitalKey(digitalKeyDto: DigitalKeyDto) {
        repository.validateDigitalKey(digitalKeyDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data?.isValid == true) {
                        //send open AccessPoint request to the relay manager if the digital key is valid
                        relayRepository.openAccessPoint(
                            accessPoint.value?.relayPort,
                            accessPoint.value?.relayOpenTimer,
                            accessPoint.value?.relayDelayTimer
                        )
                    }
                    _keyValidationState.value = DigitalKeyState(digitalKey = result.data)
                }

                is Resource.Error -> {
                    _keyValidationState.value =
                        DigitalKeyState(error = result.message ?: "An unexpected error occurred")
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

    fun getAllResidents() {
        homeRepository.getAllResidents().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _residentState.value = ResidentState(residents = result.data)
                }

                is Resource.Error -> {
                    _residentState.value =
                        ResidentState(residents = result.data,error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _residentState.value = ResidentState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

}