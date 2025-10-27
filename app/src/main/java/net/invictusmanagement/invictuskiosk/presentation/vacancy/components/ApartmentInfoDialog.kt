package net.invictusmanagement.invictuskiosk.presentation.vacancy.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.commons.LocalUserInteractionReset
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApartmentInfoDialog(
    vacancy: net.invictusmanagement.invictuskiosk.domain.model.Unit,
    onDismiss: () -> Unit,
    onContactClick: () -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val resetTimer = LocalUserInteractionReset.current

    val unitImages = mainViewModel.unitImages
    val currentImageIndex = mainViewModel.currentImageIndex

    LaunchedEffect(vacancy.id) {
        if (vacancy.imageIds.isNotEmpty()) {
            mainViewModel.loadImages(vacancy.id.toLong(), vacancy.imageIds)
        } else {
            mainViewModel.clearImages()
        }
    }

    Dialog(
        onDismissRequest = {
            resetTimer?.invoke()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f) // 70% of screen width
                .fillMaxHeight(0.7f), // 60% of screen height
            shape = RoundedCornerShape(16.dp),
            color = colorResource(R.color.background_dark),
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colorResource(R.color.btn_text))
                            .padding(4.dp)
                            .clickable {
                                resetTimer?.invoke()
                                onDismiss()
                            },
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(6f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (unitImages.isNotEmpty()) {
                        AsyncImage(
                            model = unitImages[currentImageIndex],
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Left button
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable {
                                    resetTimer?.invoke()
                                    mainViewModel.showPreviousImage()
                                },
                            tint = Color.White
                        )

                        // Right button
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable {
                                    resetTimer?.invoke()
                                    mainViewModel.showNextImage()
                                },
                            tint = Color.White
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.placeholder_image),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(4f)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = stringResource(R.string.unit_details),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = colorResource(R.color.btn_text),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = "Bed / Bath: ${Constants.formatNumber(vacancy.bedrooms)}/${
                                Constants.formatNumber(
                                    vacancy.bathrooms
                                )
                            }",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = colorResource(
                                    R.color.btn_text
                                ), fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = "Square Feet: ${Constants.formatNumber(vacancy.area)} sqft",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = colorResource(
                                    R.color.btn_text
                                ), fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = "Floor: ${vacancy.floor}",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = colorResource(
                                    R.color.btn_text
                                ), fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = "Rent : ${
                                if (vacancy.isRentHide) {
                                    "Contact me"
                                } else {
                                    "$${Constants.formatNumber(vacancy.rent)}/month"
                                }
                            }",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = colorResource(
                                    R.color.btn_text
                                ), fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = "Available : ${Constants.formatDateString(vacancy.availableDateUtc)}",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = colorResource(
                                    R.color.btn_text
                                ), fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    CustomTextButton(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = stringResource(R.string.contact_now),
                        isGradient = true,
                        onClick = {
                            resetTimer?.invoke()
                            onContactClick()
                        }
                    )
                }

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun ApartmentInfoDialogPreview() {
    val unit = net.invictusmanagement.invictuskiosk.domain.model.Unit(
        area = 1450F,
        availableDateUtc = "22/10/2024",
        bathrooms = 3.0F,
        bedrooms = 2F,
        floor = 1,
        id = 1,
        imageIds = listOf(),
        isRentHide = false,
        isUnitsForSale = false,
        rent = 1500F,
        unitNbr = "1"
    )
    ApartmentInfoDialog(
        vacancy = unit,
        onDismiss = {}
    )
}