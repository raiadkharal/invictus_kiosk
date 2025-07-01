package net.invictusmanagement.invictuskiosk.presentation.residents

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.components.QRCodePanel
import net.invictusmanagement.invictuskiosk.presentation.residents.components.ResidentListItem
import net.invictusmanagement.invictuskiosk.presentation.residents.components.SearchTextField
import net.invictusmanagement.invictuskiosk.ui.theme.InvictusKioskTheme

@Composable
fun ResidentsScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    var searchQuery by remember { mutableStateOf("") }

    val residents = listOf(
        "James Bell", "Janii Russell", "Jade Warren", "Jenny Wilson",
        "John Fox", "Johnny Steward", "Johnny Webb"
    )
    val filteredResidents = residents.filter { it.contains(searchQuery, ignoreCase = true) }

    var isPinSelected by remember { mutableStateOf(true) }

    Column (
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row (
            modifier = Modifier.fillMaxWidth()
                .background(colorResource(R.color.background_dark))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                modifier=Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clickable(onClick = {
                        navController.popBackStack()
                    }),
                painter = painterResource(R.drawable.angle_left),
                contentDescription = "Back arrow icon"
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.title_text),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold,color = colorResource(R.color.btn_text))
            )
        }

        Row(
            modifier = modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.background))
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.weight(6f)
                    .fillMaxSize()
            )      {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //search bar
                    SearchTextField(
                        modifier = Modifier.weight(8f),
                        searchQuery = searchQuery,
                        onValueChange = { searchQuery = it }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    // Filter Button
                   CustomTextButton(modifier = Modifier.weight(2f),text = "Filter", onClick = {})
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tenant List
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(filteredResidents) { resident ->
                        ResidentListItem(residentName = resident)
                    }
                }
            }

            Spacer(Modifier.width(16.dp))
            VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 2.dp, color = colorResource(R.color.divider_color))
            Spacer(Modifier.width(16.dp))

            // Pin or QR Code Section
            Column(
                modifier = Modifier.weight(4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    CustomTextButton(modifier = Modifier.weight(1f), isGradient = isPinSelected, text = stringResource(R.string.pin), onClick = {isPinSelected=true})
                    Spacer(Modifier.width(16.dp))
                    CustomTextButton(modifier = Modifier.weight(1f), text = stringResource(R.string.qr_code),isGradient = !isPinSelected, onClick = {isPinSelected=false})
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (isPinSelected) stringResource(R.string.pin_title_text) else stringResource(R.string.qr_code_title_text),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text)))

                if(isPinSelected) {
                    PinInputPanel(modifier = Modifier.weight(1f))
                }else{
                    QRCodePanel(modifier = Modifier.weight(1f), imageWidth = 300.dp, imageHeight = 300.dp)
                }

//                Row (
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End,
//                    verticalAlignment = Alignment.CenterVertically
//                ){
//                    Image(
//                        modifier=Modifier
//                            .width(60.dp)
//                            .height(60.dp),
//                        painter = painterResource(R.drawable.ic_language),
//                        contentDescription = "Language icon"
//                    )
//                    Spacer(Modifier.width(12.dp))
//                    Image(
//                        modifier=Modifier
//                            .width(60.dp)
//                            .height(60.dp),
//                        painter = painterResource(R.drawable.ic_wheel_chair),
//                        contentDescription = "Wheel chair icon"
//                    )
//                }
            }
        }
    }
}

@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun ResidentsScreenPreview() {
    InvictusKioskTheme {
        val navController = rememberNavController()
        ResidentsScreen(navController = navController)
    }
}