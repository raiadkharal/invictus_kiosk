package net.invictusmanagement.invictuskiosk.presentation.keyboard

import android.content.ClipboardManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val clipboardManager: ClipboardManager
) : ViewModel() {

    var state by mutableStateOf(KeyboardState())
        private set

    private var onValueChange: (TextFieldValue) -> Unit = {}

    fun show(
        initialText: String = "",
        isPassword: Boolean = false,
        keyboardType: KeyboardType = KeyboardType.QWERTY,
        onValueChange: (TextFieldValue) -> Unit
    ) {
        this.onValueChange = onValueChange
        state = state.copy(
            isVisible = true,
            value = TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            ),
            isPassword = isPassword,
            keyboardType = keyboardType
        )
    }

    fun hide() {
        state = state.copy(isVisible = false)
    }

    fun updateFromTextField(value: TextFieldValue) {
        state = state.copy(value = value)
        onValueChange(value)
    }

    fun append(text: String) {
        val v = state.value
        val start = v.selection.start
        val end = v.selection.end

        val newText =
            v.text.substring(0, start) +
                    text +
                    v.text.substring(end)

        val newCursor = start + text.length

        val updated = v.copy(
            text = newText,
            selection = TextRange(newCursor)
        )

        state = state.copy(value = updated)
        onValueChange(updated)
    }

    fun backspace() {
        val v = state.value
        if (v.selection.start == 0) return

        val start = v.selection.start
        val end = v.selection.end

        val deleteFrom = if (start != end) start else start - 1

        val newText =
            v.text.removeRange(deleteFrom, end)

        val updated = v.copy(
            text = newText,
            selection = TextRange(deleteFrom)
        )

        state = state.copy(value = updated)
        onValueChange(updated)
    }

    fun paste() {
        val pasted = clipboardManager.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(null)
            ?.toString()
            ?: return

        if (pasted.isNotEmpty()) {
            append(pasted)
        }
    }

    fun reset(){
        state = KeyboardState()
        onValueChange = {}
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

