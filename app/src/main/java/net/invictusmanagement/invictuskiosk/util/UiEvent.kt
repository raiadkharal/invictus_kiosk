package net.invictusmanagement.invictuskiosk.util

sealed class UiEvent {
    data class ShowError(val errorMessage: String) : UiEvent()
}