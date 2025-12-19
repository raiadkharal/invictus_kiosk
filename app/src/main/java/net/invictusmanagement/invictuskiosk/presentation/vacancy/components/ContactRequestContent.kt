package net.invictusmanagement.invictuskiosk.presentation.vacancy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.commons.LocalUserInteractionReset
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardInputField
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardType
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardViewModel
import net.invictusmanagement.invictuskiosk.presentation.vacancy.ContactRequestState
import net.invictusmanagement.invictuskiosk.util.locale.localizedString
import java.util.Locale

@Composable
fun ContactRequestContent(
    onDismiss: () -> Unit,
    requestState: ContactRequestState,
    vacancy: net.invictusmanagement.invictuskiosk.domain.model.Unit,
    onSend: (ContactRequest) -> Unit,
    keyboardVM: KeyboardViewModel
) {
    val resetSleepTimer = LocalUserInteractionReset.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        resetSleepTimer?.invoke()
        name = ""
        contactInfo = ""
        isError = false
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorResource(R.color.btn_text))
                    .padding(4.dp)
                    .clickable {
                        resetSleepTimer?.invoke()
                        keyboardVM.hide()
                        onDismiss()
                    },
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = localizedString(R.string.contact_request),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium.copy(
                color = colorResource(R.color.btn_text),
                fontWeight = FontWeight.Bold
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomTextButton(
                    modifier = Modifier.weight(1f),
                    text = localizedString(R.string.email).uppercase(Locale.ROOT),
                    isGradient = selectedTab == 0,
                    isDarkBackground = true,
                    onClick = { selectedTab = 0 }
                )

                CustomTextButton(
                    modifier = Modifier.weight(1f),
                    text = localizedString(R.string.phone).uppercase(Locale.ROOT),
                    isGradient = selectedTab == 1,
                    isDarkBackground = true,
                    onClick = { selectedTab = 1 }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Name
            KeyboardInputField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                label = localizedString(R.string.name),
                onFocusChanged = {
                    if (it.isFocused) {
                        keyboardVM.show(name) { text ->
                            resetSleepTimer?.invoke()
                            name = text
                        }
                    }else{
                        keyboardVM.hide()
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            // Email / Phone
            KeyboardInputField(
                modifier = Modifier.fillMaxWidth(),
                value = contactInfo,
                label = if (selectedTab == 0)
                    localizedString(R.string.email)
                else localizedString(R.string.phone),
                onFocusChanged = { focusState ->
                    if (focusState.isFocused) {
                        keyboardVM.show(
                            initialText = contactInfo,
                            keyboardType = if (selectedTab ==0) KeyboardType.QWERTY else KeyboardType.NUMERIC
                        ) { newValue ->
                            resetSleepTimer?.invoke()
                            if (selectedTab == 1) {
                                // Extract only digits from input
                                val digitsOnly = newValue.filter { it.isDigit() }

                                // Limit to 10 digits max
                                if (digitsOnly.length <= 10) {
                                    contactInfo = if (digitsOnly.length == 10) {
                                        // Format when exactly 10 digits are entered
                                        Constants.formatPhoneNumber(digitsOnly)
                                    } else {
                                        digitsOnly
                                    }
                                }
                            } else {
                                contactInfo = newValue
                            }
                            isError =
                                if (selectedTab == 0) !Constants.isValidEmail(newValue) else !Constants.isValidPhoneNumber(
                                    contactInfo
                                )
                        }
                    }else{
                        keyboardVM.hide()
                    }
                }
            )


            if (isError) {
                Text(
                    text = if (selectedTab == 0)
                        "Please enter a valid email"
                    else "Please enter a valid phone number",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            if (requestState.isLoading) {
                CircularProgressIndicator(color = colorResource(R.color.btn_text))
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CustomTextButton(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    isGradient = true,
                    enabled = name.isNotBlank() && contactInfo.isNotBlank() && !isError,
                    padding = 24,
                    text = localizedString(R.string.send),
                    onClick = {
                        resetSleepTimer?.invoke()
                        keyboardVM.hide()
                        onSend(
                            ContactRequest(
                                email = if (selectedTab == 0) contactInfo else null,
                                phone = if (selectedTab == 1) contactInfo else null,
                                name = name,
                                unitId = vacancy.id,
                                unitNbr = vacancy.unitNbr
                            )
                        )
                    }
                )
            }
        }
    }
}
