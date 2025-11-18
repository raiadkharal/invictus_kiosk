package net.invictusmanagement.invictuskiosk.presentation.video_call.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.LocalUserInteractionReset
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton

@Composable
fun VoiceMailConfirmationDialog(
    modifier: Modifier = Modifier,
    navController: NavController,
    onYesClick: () -> Unit = {},
    onNoClick: () -> Unit = {}
) {

    val resetSleepTimer = LocalUserInteractionReset.current

    Dialog(
        onDismissRequest = {
            resetSleepTimer?.invoke()
            navController.popBackStack()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f) // 70% of screen width
                .fillMaxHeight(0.6f), // 80% of screen height
            shape = RoundedCornerShape(16.dp),
            color = colorResource(R.color.background_dark),
            tonalElevation = 8.dp,
        ) {
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
                    text = stringResource(R.string.video_voice_mail_message),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge.copy(color = colorResource(R.color.btn_text))
                )
                Spacer(Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomTextButton(
                        modifier = Modifier.weight(1f),
                        padding = 24,
                        isGradient = true,
                        text = stringResource(R.string.yes).uppercase(),
                        onClick = {
                            resetSleepTimer?.invoke()
                            onYesClick()
                        }
                    )
                    Spacer(Modifier.width(16.dp))
                    CustomTextButton(
                        modifier = Modifier.weight(1f),
                        padding = 24,
                        isGradient = false,
                        isDarkBackground = true,
                        text = stringResource(R.string.no).uppercase(),
                        onClick = {
                            resetSleepTimer?.invoke()
                            onNoClick()
                        }
                    )
                }


            }

        }
    }
}

@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun VoiceMailConfirmationDialogPreview() {
    VoiceMailConfirmationDialog(navController = rememberNavController())
}