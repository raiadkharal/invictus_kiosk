package net.invictusmanagement.invictuskiosk.presentation.home

import android.util.Log
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
import net.invictusmanagement.invictuskiosk.domain.model.LeasingOffice
import net.invictusmanagement.invictuskiosk.domain.repository.HomeRepository
import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.invictuskiosk.presentation.residents.ResidentState
import net.invictusmanagement.invictuskiosk.presentation.signalR.MobileChatHubManager
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.MobileChatHubEventListener
import net.invictusmanagement.invictuskiosk.presentation.signalR.listeners.SignalRConnectionListener
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import net.invictusmanagement.invictuskiosk.util.UiEvent
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
    private val dataStoreManager: DataStoreManager,
    private val relayRepository: RelayManagerRepository,
    private val networkMonitor: NetworkMonitor,
    private val relayManagerRepository: RelayManagerRepository,
    private val logger: GlobalLogger
) : ViewModel(), MobileChatHubEventListener{

    val isConnected = networkMonitor.isConnected
    private val _digitalKeyValidationState = MutableStateFlow(DigitalKeyState())
    val digitalKeyValidationState: StateFlow<DigitalKeyState> = _digitalKeyValidationState

    private val _videoUrl = MutableStateFlow("")
    val videoUrl: StateFlow<String> = _videoUrl

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow

    private val _residentState = MutableStateFlow(ResidentState())
    val residentState: StateFlow<ResidentState> = _residentState

    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    private val _leasingOfficeDetails = MutableStateFlow<LeasingOffice?>(null)
    val leasingOfficeDetails: StateFlow<LeasingOffice?> = _leasingOfficeDetails

    private val _introButtons = MutableStateFlow<List<String>>(emptyList())
    val introButtons: StateFlow<List<String>> = _introButtons

    val kioskActivationCode = dataStoreManager.activationCodeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        null
    )

    init {
        logger.logError("networkStatus/HomeViewModel", "Network connected: ${networkMonitor.isConnected.value}")
    }
    private var mobileChatHubManager: MobileChatHubManager? = null

    fun initializeSignalR(kioskId: Int) {
        if (mobileChatHubManager != null) return

        mobileChatHubManager = MobileChatHubManager(
            kioskId = kioskId,
            listener = this,
            connectionListener = object : SignalRConnectionListener {
                override fun onConnected() {
                }

                override fun onConnectionError(method: String, e: Exception) {
                    logger.logError("SignalRConnectionError/HomeViewModel/${method}", "Error connecting to SignalR: ${e.localizedMessage}", e)
                }
            }
        )

        mobileChatHubManager?.connect()
    }

    fun loadInitialData(){
        viewModelScope.launch {
            dataStoreManager.accessPointFlow.collect {
                _accessPoint.value = it
            }
        }
        viewModelScope.launch {
            loadAccessPoints()
        }
        viewModelScope.launch {
            loadLeasingOfficeDetails()
        }
        viewModelScope.launch {
            loadKioskData()
        }
        viewModelScope.launch {
            loadVideoUrl()
        }
        viewModelScope.launch {
            loadIntroButtons()
        }


        viewModelScope.launch {
            relayRepository.initializeRelayManager()
        }
    }
    private suspend fun loadVideoUrl() {
        dataStoreManager.kioskDataFlow.collect {
            _videoUrl.value = it?.ssUrl ?: ""
        }
    }

    private fun loadIntroButtons(){
        repository.getIntroButtons().onEach { result->
            when(result){
                is Resource.Success->{
                    _introButtons.value = result.data?: emptyList()
                }
                is Resource.Error->{
//                    _eventFlow.emit(
//                        UiEvent.ShowError(
//                            result.message?:Constants.CONNECTION_ERROR
//                        )
//                    )
                    Log.d("TAG", "getIntroButtons: ${result.message?: "An unexpected error occurred"}")
                }
                is Resource.Loading->{}
            }
        }.launchIn(viewModelScope)
    }
    private fun loadLeasingOfficeDetails() {
        repository.getLeasingOfficeDetails().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _leasingOfficeDetails.value = result.data
                }

                is Resource.Error -> {
                    Log.d(
                        "TAG",
                        "getLeasingOfficeDetails: ${result.message ?: "An unexpected error occurred"}"
                    )
                }

                is Resource.Loading -> {}
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

                    _digitalKeyValidationState.value = DigitalKeyState(digitalKey = result.data)
                }

                is Resource.Error -> {
                    _eventFlow.emit(
                        UiEvent.ShowError(
                            Constants.DIGITAL_KEY_GENERIC_ERROR
                        )
                    )
                    _digitalKeyValidationState.value =
                        DigitalKeyState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _digitalKeyValidationState.value = DigitalKeyState(isLoading = true)
                }

            }
        }.launchIn(viewModelScope)
    }

    private fun loadAccessPoints() {
        repository.getAccessPoints().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    //save data in datastore
                    if(result.data?.isNotEmpty() == true) {
                        _accessPoint.value = result.data[0]
                        dataStoreManager.saveAccessPoint(result.data[0])
                    }
                }

                is Resource.Error -> {
                    Log.d(
                        "TAG",
                        "getAccessPoints: ${result.message ?: "An unexpected error occurred"}"
                    )
                }

                is Resource.Loading -> {}

            }
        }.launchIn(viewModelScope)
    }

    fun getAllResidents() {
        repository.getAllResidents().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _residentState.value = ResidentState(residents = result.data)
                }

                is Resource.Error -> {
                    _residentState.value = ResidentState(error = result.message?:"An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _residentState.value = ResidentState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadKioskData() {
        repository.getKioskData().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    dataStoreManager.saveKioskData(result.data)
                }

                is Resource.Error -> {
                    Log.d(
                        "TAG",
                        "loadKioskData: ${result.message ?: "An unexpected error occurred"}"
                    )
                }

                is Resource.Loading -> {

                }
            }
        }.launchIn(viewModelScope)
    }

    override fun onSendToVoiceMail() {

    }


    override fun onOpenAccessPoint(
        relayPort: Int,
        relayOpenTimer: Int,
        relayDelayTimer: Int,
        silent: Boolean
    ) {
        viewModelScope.launch {
            //send open AccessPoint request to the relay manager when get the access granted via video call
            relayManagerRepository.openAccessPoint(
                relayPort,
                relayOpenTimer,
                relayDelayTimer
            )
        }
    }

    fun resetState() {
        _digitalKeyValidationState.value = DigitalKeyState()
        _residentState.value = ResidentState()
        _accessPoint.value = null
        _leasingOfficeDetails.value = null
        _videoUrl.value = ""
    }

}