package net.invictusmanagement.invictuskiosk.presentation.service_key

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.ServiceKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.invictuskiosk.domain.repository.ServiceKeyRepository
import net.invictusmanagement.invictuskiosk.presentation.home.HomeViewModel
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import javax.inject.Inject

@HiltViewModel
class ServiceKeyViewModel @Inject constructor(
    private val repository: ServiceKeyRepository,
    private val dataStoreManager: DataStoreManager,
    private val relayRepository: RelayManagerRepository
):ViewModel() {
    private val _serviceKeyState = MutableStateFlow(ServiceKeyState())
    val serviceKeyState: StateFlow<ServiceKeyState> = _serviceKeyState

    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    init {
        viewModelScope.launch {
            dataStoreManager.accessPointFlow.collect {
                _accessPoint.value = it
            }
        }
    }

    fun validateServiceKey(serviceKeyDto: ServiceKeyDto){
        repository.validateServiceKey(serviceKeyDto).onEach { result->
            when(result){
                is Resource.Success ->{
                    if (result.data?.isValid == true) {
                        //send open AccessPoint request to the relay manager if the digital key is valid
                        relayRepository.openAccessPoint(
                            accessPoint.value?.relayPort,
                            accessPoint.value?.relayOpenTimer,
                            accessPoint.value?.relayDelayTimer
                        )
                    }

                    _serviceKeyState.value = ServiceKeyState(digitalKey = result.data)
                }
                is Resource.Error ->{
                    _serviceKeyState.value = ServiceKeyState(error = result.message ?: "An unexpected error occurred")
                }
                is Resource.Loading ->{
                    _serviceKeyState.value = ServiceKeyState(isLoading = true)
                }

            }
        }.launchIn(viewModelScope)
    }

    fun resetServiceKeyState(){
        _serviceKeyState.value = ServiceKeyState()
    }
}