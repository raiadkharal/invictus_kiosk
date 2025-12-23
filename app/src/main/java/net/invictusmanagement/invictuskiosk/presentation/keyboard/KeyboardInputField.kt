package net.invictusmanagement.invictuskiosk.presentation.keyboard

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@Composable
fun KeyboardInputField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    label: String = "",
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChanged: (FocusState) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = colorResource(R.color.btn_text)
                )
            )
        },
        leadingIcon = leadingIcon,
        placeholder = { Text(placeholder) },
        visualTransformation =
            if (isPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
        modifier = modifier.onFocusChanged {
            if (it.isFocused) keyboardController?.hide()
            onFocusChanged(it)
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineSmall.copy(
            color = colorResource(R.color.btn_text)
        ),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = colorResource(R.color.btn_text),
            focusedBorderColor = colorResource(R.color.btn_text),
            unfocusedBorderColor = colorResource(R.color.btn_text)
        )
    )
}
