package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardInputField
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardViewModel
import net.invictusmanagement.invictuskiosk.util.locale.localizedString


@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    searchQuery: String,
    placeholder: String = localizedString(R.string.search_resident),
    onValueChange: (String) -> Unit,
    keyboardVM: KeyboardViewModel
) {

    DisposableEffect(Unit) {
        onDispose {
            keyboardVM.reset()
        }
    }

    KeyboardInputField(
        modifier = modifier.fillMaxWidth(),
        value = keyboardVM.state.value,
        label = placeholder,
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = colorResource(R.color.btn_text)
            )
        },

        onValueChange = keyboardVM::updateFromTextField,
        onFocusChanged = { focusState ->
            if (focusState.isFocused) {

                keyboardVM.show(
                    initialText = searchQuery
                ) { tf ->
                    onValueChange(tf.text)
                }

            } else {
                keyboardVM.hide()
            }
        }
    )
}
