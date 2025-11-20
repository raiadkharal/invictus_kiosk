package net.invictusmanagement.invictuskiosk.presentation.coupons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import net.invictusmanagement.invictuskiosk.presentation.coupons_business_list.CouponsBusinessState
import javax.inject.Inject

@HiltViewModel
class CouponsViewModel @Inject constructor(
    private val repository: CouponsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CouponsCategoryState())
    val state: StateFlow<CouponsCategoryState> = _state

    private val _businessPromotions = MutableStateFlow<CouponsBusinessState>(CouponsBusinessState())
    val businessPromotions: StateFlow<CouponsBusinessState> = _businessPromotions

    fun getPromotionsCategory() {
        repository.getCouponsCategories().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = CouponsCategoryState(couponsCategories = result.data ?: emptyList())
                }

                is Resource.Error -> {
                    _state.value = CouponsCategoryState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _state.value = CouponsCategoryState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getPromotionsByCategory(id: String) {
        repository.getCouponsByCategory(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _businessPromotions.value = CouponsBusinessState(businessPromotions =result.data ?: emptyList())
                }

                is Resource.Error -> {
                    _businessPromotions.value = CouponsBusinessState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    _businessPromotions.value =  CouponsBusinessState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}