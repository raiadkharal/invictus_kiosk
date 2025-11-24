package net.invictusmanagement.invictuskiosk.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.LoginDto
import net.invictusmanagement.invictuskiosk.data.sync.SyncScheduler
import net.invictusmanagement.invictuskiosk.domain.repository.LoginRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val datastoreManager: DataStoreManager,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun login(loginDto: LoginDto) {
        repository.login(loginDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = LoginState(login = result.data)
                    datastoreManager.saveAccessToken(result.data?.token ?: "")
                    // schedule data sync worker
//                    syncScheduler.schedulePeriodicSync()
                }

                is Resource.Error -> {
                    _state.value =
                        LoginState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _state.value = LoginState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    suspend fun saveActivationCode(activationCode: String){
        datastoreManager.saveActivationCode(activationCode)
    }
}