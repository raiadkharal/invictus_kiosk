package net.invictusmanagement.invictuskiosk.presentation.login

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.data.remote.dto.LoginDto
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.home.components.UrlVideoPlayer
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardInputField
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardViewModel
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.util.locale.localizedString


@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
    keyboardVM: KeyboardViewModel
) {

    val keyboardOffset by animateDpAsState(
        targetValue = if (keyboardVM.state.isVisible) (-180).dp else 0.dp,
        label = "keyboardOffset"
    )


    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var activationCode by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    // Convert raw resource to URI string
    val logoUri = remember {
        "android.resource://${context.packageName}/${R.raw.logo_video}"
    }

    LaunchedEffect(state) {
        if (state.login?.success == true) {
            navController.navigate(HomeScreen)
            viewModel.saveActivationCode(activationCode)
        } else if (state.error.isNotEmpty()) {
            validationError = state.error
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboardVM.reset()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = colorResource(R.color.btn_text))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .offset(y = keyboardOffset)// move content up when keyboard is visible
        ) {

            UrlVideoPlayer(
                url = logoUri,
                modifier = Modifier
                    .size(140.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = localizedString(R.string.invictus_lifestyle),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = colorResource(R.color.btn_text),
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = localizedString(R.string.welcome_text),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = localizedString(R.string.kisok_version),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.btn_text))
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = localizedString(R.string.activation_code),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )
            Spacer(modifier = Modifier.height(8.dp))

            KeyboardInputField(
                modifier = Modifier.fillMaxWidth(0.5f),
                value = keyboardVM.state.value,
                onValueChange = keyboardVM::updateFromTextField,
                onFocusChanged = { focusState ->
                    if (focusState.isFocused) {

                        keyboardVM.show(
                            initialText = activationCode
                        ) { tf ->

                            activationCode = tf.text

                            if (validationError.isNotEmpty()) {
                                validationError = ""
                            }
                        }

                    } else {
                        keyboardVM.hide()
                    }
                }
            )


            if (validationError.isNotEmpty()) {
                Text(
                    text = validationError,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.red)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CustomTextButton(
                modifier = Modifier.fillMaxWidth(0.2f),
                text = localizedString(R.string.activate),
                isGradient = true,
                padding = 24,
                onClick = {
                    if (activationCode.isBlank()) {
                        validationError = "Activation code is required"
                    } else {
                        validationError = ""
                        viewModel.login(LoginDto(activationCode = activationCode))
                    }
                }
            )
        }
    }
}