package net.invictusmanagement.invictuskiosk.presentation.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.BusinessPromotionDto
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import javax.inject.Inject

@HiltViewModel
class CouponsViewModel @Inject constructor(
    private val repository: CouponsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(emptyList<PromotionsCategory>())
    val state: StateFlow<List<PromotionsCategory>> = _state

    private val _businessPromotions = MutableStateFlow(emptyList<BusinessPromotionDto>())
    val businessPromotions: StateFlow<List<BusinessPromotionDto>> = _businessPromotions

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

    fun getPromotionsByCategory(id: String) {
        repository.getPromotionsByCategory(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _businessPromotions.value = result.data ?: emptyList()
                }

                is Resource.Error -> {
                    _businessPromotions.value =  emptyList()
                }

                is Resource.Loading -> {
                    _businessPromotions.value =  emptyList()
                }
            }
        }.launchIn(viewModelScope)
    }
}