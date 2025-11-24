package net.invictusmanagement.invictuskiosk.presentation.vacancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.domain.model.Unit
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import net.invictusmanagement.invictuskiosk.util.UiEvent
import javax.inject.Inject

@HiltViewModel
class VacancyViewModel @Inject constructor(
    private val repository: VacancyRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val isConnected = networkMonitor.isConnected
    private val _unitList = MutableStateFlow(VacancyState())
    val unitList: StateFlow<VacancyState> = _unitList

    private val _contactRequestState = MutableStateFlow(ContactRequestState())
    val contactRequestState: StateFlow<ContactRequestState> = _contactRequestState


    fun getUnits() {
        repository.getUnits().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _unitList.value = VacancyState(vacancies = result.data ?: emptyList())
                }

                is Resource.Error -> {
                    _unitList.value = VacancyState(
                        vacancies = result.data ?: emptyList(),
                        error = result.message ?: "An unexpected error occurred"
                    )
                }

                is Resource.Loading -> {
                    _unitList.value = VacancyState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun sendContactRequest(contactRequest: ContactRequest) {
        repository.sendContactRequest(contactRequest).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _contactRequestState.value = ContactRequestState(contactRequest = result.data)
                }

                is Resource.Error -> {
                    _contactRequestState.value = ContactRequestState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }

                is Resource.Loading -> {
                    _contactRequestState.value = ContactRequestState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

}