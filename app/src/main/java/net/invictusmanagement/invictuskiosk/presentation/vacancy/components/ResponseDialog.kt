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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.LocalUserInteractionReset
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton


@Composable
fun ResponseDialog(
    onDismiss: () -> Unit,
) {

    val resetSleepTimer = LocalUserInteractionReset.current

    Dialog(
        onDismissRequest = {
            resetSleepTimer?.invoke()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.notification),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = colorResource(R.color.btn_text),
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.contact_request_success_message),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displaySmall.copy(color = colorResource(R.color.btn_text))
                )
                Spacer(Modifier.height(24.dp))
                CustomTextButton(
                    modifier = Modifier.fillMaxWidth(0.3f),
                    padding = 24,
                    isGradient = true,
                    text = stringResource(R.string.dismiss),
                    onClick = {
                        resetSleepTimer?.invoke()
                        onDismiss()
                    }
                )

            }

        }
    }
}

@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun ResponseDialogPreview() {
    ResponseDialog(onDismiss = {})
}