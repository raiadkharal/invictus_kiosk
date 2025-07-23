package net.invictusmanagement.invictuskiosk.presentation.vacancy.components

import android.os.Build
import androidx.annotation.RequiresApi
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
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApartmentInfoDialog(
    vacancy: net.invictusmanagement.invictuskiosk.domain.model.Unit,
    onDismiss: () -> Unit,
    onContactClick: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                Row (
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.End
                ){
                    Icon(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colorResource(R.color.btn_text))
                            .padding(4.dp)
                            .clickable{onDismiss()},
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
                Image(
                    painter = painterResource(id = R.drawable.image3),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(6f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                )
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
                        style = MaterialTheme.typography.displayMedium.copy(color = colorResource(R.color.btn_text), fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Column (
                        modifier = Modifier.weight(1f)
                    ){
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = "Bed / Bath: ${vacancy.bedrooms}/${vacancy.bathrooms}",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text), fontWeight = FontWeight.Bold)
                        )
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = "Square Feet: ${vacancy.area} sqft",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text), fontWeight = FontWeight.Bold)
                        )
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = "Floor: ${vacancy.floor}",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text), fontWeight = FontWeight.Bold)
                        )
                        Text(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            text = "Rent : $${vacancy.rent}/month",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text), fontWeight = FontWeight.Bold)
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = "Available : ${Constants.formatDateString(vacancy.availableDateUtc)}",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text), fontWeight = FontWeight.Bold)
                        )
                    }

                    CustomTextButton(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = stringResource(R.string.contact_now),
                        isGradient = true,
                        onClick = onContactClick
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
        area = 1450,
        availableDateUtc = "22/10/2024",
        bathrooms = 3,
        bedrooms = 2,
        floor = 1,
        id = 1,
        imageIds = listOf(),
        isRentHide = false,
        isUnitsForSale = false,
        rent = 1500,
        unitNbr = "1"
    )
    ApartmentInfoDialog(
        vacancy =unit,
        onDismiss = {}
    )
}