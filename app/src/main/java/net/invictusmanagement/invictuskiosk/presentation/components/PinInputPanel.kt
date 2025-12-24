package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PinInputPanel(
    modifier: Modifier = Modifier,
    pinLength: Int = 4,
    message: String = "",
    onMessageClick: () -> Unit = {},
    isError: Boolean = false,
    onCompleted: (String) -> Unit = {},
    buttons: List<List<String>> = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("0", "⌫", "clear")
    )
) {
    var otp by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        // OTP display
        PinCodeTextField(
            value = otp,
            isError = isError,
            length = pinLength,
            onValueChanged = {
                otp = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onMessageClick),
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = if (isError) Color.Red else colorResource(
                        R.color.btn_text
                    )
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Buttons for digits
        OTPButtonGrid(
            buttons, otp = otp,
            onOtpChange = {
                otp = it
                if (otp.length == pinLength) {
                    onCompleted(otp)
                }
            })
    }
}

@Composable
fun OTPButtonGrid(buttons: List<List<String>>, otp: String, onOtpChange: (String) -> Unit) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        buttons.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(6f)
            ) {
                row.forEach { buttonText ->
                    when (buttonText) {
                        "clear" -> {
                            IconButton(
                                onClick = { onOtpChange("") },
                                modifier = Modifier
                                    .weight(if (row.size == 2 || row.size == 5) 2f else 1f) // Adjust the weight as needed
                                    .height(72.dp)
                                    .background(
                                        color = colorResource(R.color.btn_pin_code),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Text(
                                    "Clear",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = colorResource(R.color.btn_text)
                                    )
                                )
                            }
                        }

                        "⌫" -> {
                            OTPButton(
                                modifier = Modifier.weight(1f),
                                text = buttonText,
                                onClick = {
                                    if (otp.isNotEmpty()) {
                                        onOtpChange(otp.dropLast(1))
                                    }
                                }
                            )
                        }

                        else -> {
                            OTPButton(
                                modifier = Modifier.weight(1f),
                                text = buttonText,
                                onClick = {
                                    if (otp.length < 4) {
                                        onOtpChange(otp + buttonText)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
fun OTPEntryScreenPreview() {
    PinInputPanel()
}
