package net.invictusmanagement.invictuskiosk.presentation.service_key

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.data.remote.dto.ServiceKeyDto
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.navigation.DirectoryScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreenRoute

@Composable
fun ServiceKeyScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ServiceKeyViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val state by viewModel.serviceKeyState.collectAsStateWithLifecycle()
    val currentAccessPoint by viewModel.accessPoint.collectAsStateWithLifecycle()
    var isError by remember { mutableStateOf(false) }
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    val otpButtons: List<List<String>> = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("0", "X", "clear")
    )

    LaunchedEffect(state) {
        if (state.digitalKey?.isValid == true) {
            isError = false
            delay(3000)
            navController.navigate(
                UnlockedScreenRoute(
                    unitId = 0,
                    mapId = 0
                )
            ){
                popUpTo(HomeScreen)
            }
        } else if(state.digitalKey?.isValid == false){
            isError = true
            delay(2000)
            isError = false
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomToolbar(
            title = "$locationName - $kioskName",
            navController = navController
        )
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.service_key),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )

            Spacer(Modifier.height(8.dp))

            PinInputPanel(
                modifier = Modifier
                    .weight(1f)
                    .width(720.dp),
                buttons = otpButtons,
                isError = isError,
                message = if (isError) stringResource(R.string.invalid_service_key) else stringResource(
                    R.string.service_key_message_text
                ),
                onMessageClick = {
                    navController.navigate(DirectoryScreen)
                },
                onCompleted = { pinCode ->
                    viewModel.validateServiceKey(
                        ServiceKeyDto(
                            accessPointId = currentAccessPoint?.id ?: 0,
                            key = pinCode
                        )
                    )
                }
            )

        }

    }

}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun ServiceScreenPreview() {
    val navController = rememberNavController()
    ServiceKeyScreen(navController = navController)
}