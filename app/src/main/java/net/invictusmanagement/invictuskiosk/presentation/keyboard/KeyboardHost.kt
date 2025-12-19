package net.invictusmanagement.invictuskiosk.presentation.keyboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun KeyboardHost(
    keyboardVM: KeyboardViewModel,
    content: @Composable () -> Unit
) {

    val focusManager = LocalFocusManager.current

    Box(Modifier.fillMaxSize()) {

        content()

        if (keyboardVM.state.isVisible) {
            Column {
                Spacer(Modifier.weight(1f))

                when (keyboardVM.state.keyboardType) {

                    KeyboardType.QWERTY ->
                        QwertyKeyboard(
                            onKeyPress = keyboardVM::append,
                            onBackspace = keyboardVM::backspace,
                            onKeyboardSwitch = keyboardVM::switchLayout,
                            onDone = {
                                keyboardVM.hide()
                                focusManager.clearFocus(force = true)
                            }
                        )

                    KeyboardType.NUMERIC ->
                        NumericKeyboard(
                            onKeyPress = keyboardVM::append,
                            onBackspace = keyboardVM::backspace,
                            onKeyboardSwitch = keyboardVM::switchLayout,
                            onDone = {
                                keyboardVM.hide()
                                focusManager.clearFocus(force = true)
                            }
                        )
                }
            }
        }
    }
}

