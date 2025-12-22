package net.invictusmanagement.invictuskiosk.presentation.keyboard

import android.content.ClipboardManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val clipboardManager: ClipboardManager
) : ViewModel() {

    var state by mutableStateOf(KeyboardState())
        private set

    private var onTextChange: (String) -> Unit = {}

    fun show(
        initialText: String = "",
        isPassword: Boolean = false,
        keyboardType: KeyboardType = KeyboardType.QWERTY,
        onTextChange: (String) -> Unit
    ) {
        this.onTextChange = onTextChange
        state = state.copy(
            isVisible = true,
            text = initialText,
            isPassword = isPassword,
            keyboardType = keyboardType
        )
    }

    fun hide() {
        state = state.copy(isVisible = false)
    }

    fun append(value: String) {
        state = state.copy(text = state.text + value)
        onTextChange(state.text)
    }

    fun backspace() {
        if (state.text.isNotEmpty()) {
            state = state.copy(text = state.text.dropLast(1))
            onTextChange(state.text)
        }
    }

    fun paste() {
        val pastedText = clipboardManager
            .primaryClip
            ?.getItemAt(0)
            ?.coerceToText(null)
            ?.toString()
            ?: return

        if (pastedText.isEmpty()) return

        state = state.copy(text = state.text + pastedText)
        onTextChange(state.text)
    }

    fun switchLayout() {
        state = state.copy(
            keyboardType = when (state.keyboardType) {
                KeyboardType.QWERTY -> KeyboardType.NUMERIC
                KeyboardType.NUMERIC -> KeyboardType.QWERTY
            }
        )
    }
}
