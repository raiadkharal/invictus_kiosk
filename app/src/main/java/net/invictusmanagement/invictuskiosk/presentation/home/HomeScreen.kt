package net.invictusmanagement.invictuskiosk.presentation.home

import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.components.CustomIconButton
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.components.QRCodePanel
import net.invictusmanagement.invictuskiosk.presentation.home.components.CustomBottomSheet
import net.invictusmanagement.invictuskiosk.presentation.home.components.HomeBottomSheet
import net.invictusmanagement.invictuskiosk.presentation.home.components.PinCodeBottomSheet
import net.invictusmanagement.invictuskiosk.presentation.home.components.UrlVideoPlayer
import net.invictusmanagement.invictuskiosk.presentation.navigation.ResidentsScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.ServiceKeyScreen
import net.invictusmanagement.invictuskiosk.ui.theme.InvictusKioskTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    var isPinSelected by remember { mutableStateOf(false) }

    var showHomeBottomSheet by remember { mutableStateOf(false) }
    var showPinCodeBottomSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(6f)
                .fillMaxSize()
        ) {
            UrlVideoPlayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                url = "https://www.w3schools.com/html/mov_bbb.mp4"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomIconButton(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_directory,
                    text = stringResource(R.string.directory),
                    onClick = {
                        navController.navigate(ResidentsScreen)
                    })

                Spacer(Modifier.width(20.dp))

                CustomIconButton(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_service_key,
                    text = stringResource(R.string.service_key_all_caps),
                    onClick = {
                        navController.navigate(ServiceKeyScreen)
                    })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomIconButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    icon = R.drawable.ic_check_in,
                    text = stringResource(R.string.check_in),
                    onClick = {})

                Spacer(Modifier.width(20.dp))

                CustomIconButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    icon = R.drawable.ic_leasing_office,
                    text = stringResource(R.string.leasing_office),
                    onClick = {})

                Spacer(Modifier.width(20.dp))

                CustomIconButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    icon = R.drawable.ic_coupons,
                    text = stringResource(R.string.local_coupons),
                    onClick = {})

                Spacer(Modifier.width(20.dp))

                CustomIconButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    icon = R.drawable.ic_vacancies,
                    text = stringResource(R.string.vacancies),
                    onClick = {})
            }
        }

        Spacer(Modifier.width(16.dp))
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 2.dp,
            color = colorResource(R.color.divider_color)
        )
        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(4f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CustomTextButton(
                    modifier = Modifier.weight(1f),
                    isGradient = isPinSelected,
                    text = stringResource(R.string.pin),
                    onClick = { isPinSelected = true })
                Spacer(Modifier.width(16.dp))
                CustomTextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.qr_code),
                    isGradient = !isPinSelected,
                    onClick = { isPinSelected = false })
            }

            Spacer(Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = if (isPinSelected) stringResource(R.string.pin_title_text) else stringResource(
                    R.string.qr_code_title_text
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )

            if (isPinSelected) {
                PinInputPanel(modifier = Modifier.weight(1f))
            } else {
                QRCodePanel(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                        .clickable(onClick = {}),
                    painter = painterResource(R.drawable.ic_language),
                    contentDescription = "Language icon"
                )
                Spacer(Modifier.width(12.dp))
                Image(
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                        .clickable(onClick = { showHomeBottomSheet = true }),
                    painter = painterResource(R.drawable.ic_wheel_chair),
                    contentDescription = "Wheel chair icon"
                )
            }
        }
    }


    CustomBottomSheet(
        isVisible = showHomeBottomSheet,
        onDismiss = { showHomeBottomSheet = false }
    ) {
        // Place the bottom sheet content here
        HomeBottomSheet(
            onPinCodeClick = {
                showHomeBottomSheet = false
                showPinCodeBottomSheet = true
            },
            onQrCodeClick = {
                //handle qr code click here
            }
        )
    }

    CustomBottomSheet(
        isVisible = showPinCodeBottomSheet,
        onDismiss = { showPinCodeBottomSheet = false }
    ) {
        // Place the bottom sheet content here
        PinCodeBottomSheet(
            onHomeClick = {
                showPinCodeBottomSheet = false
                showHomeBottomSheet = false
            },
            onBackClick = {
                showPinCodeBottomSheet = false
                showHomeBottomSheet = true
            }
        )
    }
}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    InvictusKioskTheme {
        val navController = rememberNavController()
        HomeScreen(navController=navController)
    }
}