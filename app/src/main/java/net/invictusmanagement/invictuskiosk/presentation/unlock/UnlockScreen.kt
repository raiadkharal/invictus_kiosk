package net.invictusmanagement.invictuskiosk.presentation.unlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.util.locale.localizedString

@Composable
fun UnlockScreen(
    modifier: Modifier = Modifier,
    unitId: Int,
    mapId: Int,
    toPackageCenter: Boolean,
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val unitMapImagePath by mainViewModel.mapImagePath.collectAsStateWithLifecycle()
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        if (unitId != 0 && mapId != 0) {
            mainViewModel.fetchMapImage(
                unitId = unitId.toLong(),
                unitMapId = mapId.toLong(),
                toPackageCenter = toPackageCenter
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CustomToolbar(
            title = "$locationName - $kioskName",
            showBackArrow = true,
            navController = navController
        )
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (unitMapImagePath == null || unitMapImagePath!!.isEmpty()) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(R.drawable.unlock_bg_img),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Background image"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.btn_pin_code).copy(alpha = 0.5f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = localizedString(R.string.unlocked),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = colorResource(R.color.btn_text),
                            fontWeight = FontWeight.W400,
                            fontSize = 120.sp
                        )
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        text = localizedString(R.string.unlocked_message),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = colorResource(R.color.btn_text),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.background)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = unitMapImagePath,
                        contentDescription = "Map Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentScale = ContentScale.Fit
                    )
                }

            }

        }
    }
}
