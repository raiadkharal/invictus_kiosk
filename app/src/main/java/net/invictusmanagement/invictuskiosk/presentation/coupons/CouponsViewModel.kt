package net.invictusmanagement.invictuskiosk.presentation.coupons

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import javax.inject.Inject

@HiltViewModel
class CouponsViewModel @Inject constructor(
    private val repository: CouponsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(emptyList<PromotionsCategory>())
    val state: StateFlow<List<PromotionsCategory>> = _state

    private val _couponCodes = MutableStateFlow(emptyList<String>())
    val couponCodes: StateFlow<List<String>> = _couponCodes

    fun getPromotionsCategory() {
        repository.getPromotionsCategories().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = result.data ?: emptyList()
                }

                is Resource.Error -> {
                    _state.value = emptyList()
                }

                is Resource.Loading -> {
                    _state.value = emptyList()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getPromotionCodesById(id: String) {
        repository.getPromotionCodesById(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _couponCodes.value = result.data ?: emptyList()
                }

                is Resource.Error -> {
                    _couponCodes.value =  emptyList()
                }

                is Resource.Loading -> {
                    _couponCodes.value =  emptyList()
                }
            }
        }.launchIn(viewModelScope)
    }
}