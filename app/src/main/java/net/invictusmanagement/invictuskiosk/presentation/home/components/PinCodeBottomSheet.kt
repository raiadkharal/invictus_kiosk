package net.invictusmanagement.invictuskiosk.presentation.home.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.home.HomeViewModel
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreen
import net.invictusmanagement.invictuskiosk.util.UiEvent

@Composable
fun PinCodeBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController,
    onHomeClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val keyValidationState by viewModel.digitalKeyValidationState.collectAsState()
    val currentAccessPoint by viewModel.accessPoint.collectAsState()
    val activationCode by viewModel.activationCode.collectAsState()
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(keyValidationState) {
        if (keyValidationState.digitalKey?.isValid == true) {
            isError = false
            delay(2000)
            navController.navigate(UnlockedScreen)
        } else if (keyValidationState.digitalKey?.isValid == false) {
            isError = true
            delay(3000)
            isError = false
        }
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    Toast.makeText(navController.context, event.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    
    Row(
        modifier = modifier
            .fillMaxSize()
            .heightIn(max = 400.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 30.dp)
    ) {
        Column(
            modifier = Modifier.weight(8f)
                .fillMaxSize()
        ) {
            // Pin Input section

            val buttons:List<List<String>> = listOf(
            listOf("1", "2", "3","4", "5", "6"),
            listOf("7", "8", "9","0", "clear"))
           PinInputPanel(
               modifier=Modifier.fillMaxSize(),
               buttons = buttons,
               isError = isError,
               onCompleted = { pinCode ->
                   viewModel.validateDigitalKey(
                       DigitalKeyDto(
                           accessPointId = currentAccessPoint?.id ?: 0,
                           key = pinCode,
                           activationCode = activationCode ?: ""
                       )
                   )
               }
           )
        }

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ){
                IconButton(
                    modifier = Modifier.weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(color = colorResource(R.color.btn_pin_code))
                        .padding(20.dp),
                    onClick = onHomeClick
                ) {
                    Icon(
                        modifier = Modifier.width(50.dp).height(50.dp),
                        imageVector =  Icons.Default.Home,
                        contentDescription = "Home",
                        tint = colorResource(R.color.btn_text)
                    )
                }
                Spacer(Modifier.height(20.dp))
                IconButton(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(color = colorResource(R.color.btn_text))
                        .padding(vertical = 20.dp),
                    onClick = onBackClick
                ) {
                    Icon(
                        modifier = Modifier.width(50.dp).height(50.dp),
                       imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = colorResource(R.color.btn_pin_code)
                    )
                }
            }
        }
    }
}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun BottomSheetHomePreview() {
    PinCodeBottomSheet(
        viewModel = hiltViewModel(),
        navController = rememberNavController()
    )
}