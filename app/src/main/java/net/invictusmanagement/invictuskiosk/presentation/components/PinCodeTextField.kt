package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.R

@ExperimentalComposeUiApi
@Composable
fun PinCodeTextField(
    modifier: Modifier = Modifier,
    length: Int = 4,
    value: String = "",
    isError: Boolean = false,
    onValueChanged: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    TextField(
        value = value,
        onValueChange = {
            if (it.length <= length) {
                if (it.all { c -> c in '0'..'9' }) {
                    onValueChanged(it)
                }
                if (it.length >= length) {
                    keyboard?.hide()
                }
            }
        },
        // Hide the text field
        modifier = Modifier
            .size(0.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth()
            .border(width = 2.dp, color = if(isError) Color.Red else colorResource(R.color.btn_text),shape = MaterialTheme.shapes.large),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(length) {
            OtpCell(
                modifier = modifier
                    .size(width = 65.dp, height = 90.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(colorResource(R.color.background))
//                    .border(width = 2.dp,color = colorResource(R.color.divider_color), shape = MaterialTheme.shapes.large)
                    .clickable {
                        focusRequester.requestFocus()
                        keyboard?.show()
                    },
                isError =isError,
                value = value.getOrNull(it)?.toString() ?: "",
                isCursorVisible = value.length == it
            )
            if (it != length - 1) Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
fun OtpCell(
    modifier: Modifier = Modifier,
    value: String,
    isError: Boolean=false,
    isCursorVisible: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val (cursorSymbol, setCursorSymbol) = remember { mutableStateOf("") }

    LaunchedEffect(key1 = cursorSymbol, isCursorVisible) {
        if (isCursorVisible) {
            scope.launch {
                delay(350)
                setCursorSymbol(if (cursorSymbol.isEmpty()) "|" else "")
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        Text(
            text = if (isCursorVisible) cursorSymbol else value,
            style = MaterialTheme.typography.headlineSmall.copy(color = if (isError) Color.Red else colorResource(R.color.btn_text)),
            modifier = Modifier.align(Alignment.Center)
        )
        if(value.isEmpty()){
            HorizontalDivider(thickness = 2.dp, color = colorResource(R.color.btn_text), modifier = Modifier.align(Alignment.Center))
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
private fun OTPCellPreview() {
    PinCodeTextField(value = "", onValueChanged = {})
}