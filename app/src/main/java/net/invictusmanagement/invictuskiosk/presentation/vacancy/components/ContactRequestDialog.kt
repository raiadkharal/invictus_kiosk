package net.invictusmanagement.invictuskiosk.presentation.vacancy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.commons.LocalUserInteractionReset
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.vacancy.ContactRequestState
import java.util.Locale


@Composable
fun ContactRequestDialog(
    onDismiss: () -> Unit,
    requestState: ContactRequestState,
    vacancy: net.invictusmanagement.invictuskiosk.domain.model.Unit,
    onSend: (ContactRequest) -> Unit = {}
) {

    val resetSleepTimer = LocalUserInteractionReset.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Email, 1=Phone
    var name by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        resetSleepTimer?.invoke()
        contactInfo = ""
        name = ""
        isError = false
    }

    Dialog(
        onDismissRequest = {
            resetSleepTimer?.invoke()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f) // 80% of screen width
                .fillMaxHeight(0.7f), // 70% of screen height
            shape = RoundedCornerShape(16.dp),
            color = colorResource(R.color.background_dark),
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colorResource(R.color.btn_text))
                            .padding(4.dp)
                            .clickable {
                                resetSleepTimer?.invoke()
                                onDismiss()
                            },
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.contact_request),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomTextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.email).uppercase(Locale.ROOT),
                            isGradient = selectedTab == 0,
                            isDarkBackground = true,
                            onClick = {
                                resetSleepTimer?.invoke()
                                selectedTab = 0
                            }
                        )
                        Spacer(Modifier.width(16.dp))
                        CustomTextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(R.string.phone).uppercase(Locale.ROOT),
                            isGradient = selectedTab == 1,
                            isDarkBackground = true,
                            onClick = {
                                resetSleepTimer?.invoke()
                                selectedTab = 1
                            }
                        )
                    }

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            resetSleepTimer?.invoke()
                            name = it
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = colorResource(
                                R.color.btn_text
                            )
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                stringResource(R.string.name),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colorResource(
                                        R.color.btn_text
                                    )
                                )
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.btn_text),  // Active state border
                            unfocusedBorderColor = colorResource(R.color.btn_text) // Inactive state border
                        )
                    )

                    // Contact field (dynamic based on selection)
                    OutlinedTextField(
                        value = contactInfo,
                        onValueChange = { newValue ->
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
                                if (selectedTab == 0) !Constants.isValidEmail(newValue) else !Constants.isValidPhoneNumber(contactInfo)
                        },
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = colorResource(
                                R.color.btn_text
                            )
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = {
                            Text(
                                if (selectedTab == 0) stringResource(R.string.email) else stringResource(
                                    R.string.phone
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colorResource(R.color.btn_text)
                                )
                            )
                        },
                        placeholder = {
                            if (selectedTab == 0) {
                                Text("eg: jsmith@invictus.com")
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedTab == 0) KeyboardType.Email
                            else KeyboardType.Phone
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.btn_text),  // Active state border
                            unfocusedBorderColor = colorResource(R.color.btn_text) // Inactive state border
                        )
                    )
                    if (isError && selectedTab == 0) {
                        Text(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth(),
                            text = "Please enter a valid email address",
                            textAlign = TextAlign.Start,
                            color = Color.Red,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = colorResource(
                                    R.color.btn_text
                                )
                            )
                        )
                    }else if(isError && selectedTab == 1){
                        Text(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth(),
                            text = "Please enter a valid phone number",
                            textAlign = TextAlign.Start,
                            color = Color.Red,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = colorResource(
                                    R.color.btn_text
                                )
                            )
                        )
                    }
                    if (!isError) {
                        Text(
                            modifier = Modifier
                                .padding(top = 4.dp, start = 24.dp)
                                .fillMaxWidth(),
                            text = if (selectedTab == 0) stringResource(R.string.placeholder_email) else stringResource(
                                R.string.placeholder_phone
                            ),
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = colorResource(
                                    R.color.btn_text
                                )
                            )
                        )
                    }
                    if (requestState.isLoading) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(color = colorResource(R.color.btn_text))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        CustomTextButton(
                            modifier = Modifier.fillMaxWidth(0.6f),
                            padding = 24,
                            isGradient = true,
                            enabled = name.isNotEmpty() && contactInfo.isNotEmpty() && !isError,
                            text = stringResource(R.string.send),
                            onClick = {
                                resetSleepTimer?.invoke()
                                val contactRequest = ContactRequest(
                                    email = if (selectedTab == 0) contactInfo else null,
                                    name = name,
                                    phone = if (selectedTab == 1) contactInfo else null,
                                    unitId = vacancy.id,
                                    unitNbr = vacancy.unitNbr
                                )
                                onSend(contactRequest)
                            }
                        )
                    }
                }


            }

        }
    }
}