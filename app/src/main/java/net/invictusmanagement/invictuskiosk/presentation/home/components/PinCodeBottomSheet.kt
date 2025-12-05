package net.invictusmanagement.invictuskiosk.presentation.home.components

import android.Manifest
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.Resident
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomIconButton
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.home.HomeViewModel
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreenRoute
import net.invictusmanagement.invictuskiosk.util.UiEvent
import java.security.Permissions

@Composable
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
fun PinCodeBottomSheet(
    modifier: Modifier = Modifier,
    selectedResident: Resident,
    viewModel: HomeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    isError: Boolean = false,
    onHomeClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onCallBtnClick: (Resident) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentAccessPoint by viewModel.accessPoint.collectAsState()

//    val previewView = remember { PreviewView(context) }
//    LaunchedEffect(previewView) {
//        mainViewModel.snapshotManager.startCamera(
//            previewView,
//            context,
//            lifecycleOwner
//        )
//    }
//    AndroidView(
//        factory = { previewView },
//        modifier = Modifier
//            .size(1.dp) // make it 1 pixel
//            .alpha(0f)  // fully invisible
//    )


//    LaunchedEffect(Unit) {
//        delay(1000)
//        mainViewModel.snapshotManager.recordStampVideoAndUpload(selectedResident.id.toLong())
//    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .heightIn(max = 400.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(10f)
                .fillMaxSize()
        ) {
            // Pin Input section

            val buttons: List<List<String>> = listOf(
                listOf("1", "2", "3", "4", "5", "6"),
                listOf("7", "8", "9", "0", "X", "clear")
            )
            PinInputPanel(
                modifier = Modifier.fillMaxSize(),
                buttons = buttons,
                isError = isError,
                onCompleted = { pinCode ->
                    viewModel.validateDigitalKey(
                        DigitalKeyDto(
                            accessPointId = currentAccessPoint?.id?.toLong() ?: 0L,
                            key = pinCode,
                            activationCode = selectedResident.activationCode ?: ""
                        )
                    )
                }
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(2f)
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
            ) {
                CustomIconButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    icon = R.drawable.ic_video,
                    text = "VIDEO CALL",
                    onClick = { onCallBtnClick(selectedResident) }
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = colorResource(R.color.btn_text))
                            .padding(vertical = 20.dp),
                        onClick = onBackClick
                    ) {
                        Icon(
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp),
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = colorResource(R.color.btn_pin_code)
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    IconButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = colorResource(R.color.btn_pin_code))
                            .padding(20.dp),
                        onClick = onHomeClick
                    ) {
                        Icon(
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp),
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = colorResource(R.color.btn_text)
                        )
                    }
                }
            }
        }
    }
}
