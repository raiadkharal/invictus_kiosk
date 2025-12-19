package net.invictusmanagement.invictuskiosk.presentation.keyboard

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@Composable
fun KeyboardInputField(
    modifier: Modifier = Modifier,
    value: String,
    label: String = "",
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    onFocusChanged: (FocusState) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length) // cursor at end
            )
        )
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it.copy(text = value)
        },
        enabled = true,
        readOnly = false,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.btn_text))
            )
        },
        leadingIcon = leadingIcon,
        placeholder = {Text(placeholder)},
        visualTransformation =
            if (isPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
        modifier = modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    keyboardController?.hide() //kill OS keyboard
                }
                onFocusChanged(focusState)
            },
        textStyle = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text)),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = colorResource(R.color.btn_text),
            focusedBorderColor = colorResource(R.color.btn_text),
            unfocusedBorderColor = colorResource(R.color.btn_text),
            focusedTextColor = colorResource(R.color.btn_text),
        )
    )
}
