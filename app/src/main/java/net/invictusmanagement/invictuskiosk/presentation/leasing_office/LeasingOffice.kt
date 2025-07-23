package net.invictusmanagement.invictuskiosk.presentation.leasing_office

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.navigation.DirectoryScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.LeasingOfficeCallingScreen

@Composable
fun LeasingOfficeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val locationName by mainViewModel.locationName.collectAsState()
    val kioskName by mainViewModel.kioskName.collectAsState()

    val buttons:List<List<String>> = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("0","X","clear"))


    Column (
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        CustomToolbar(
            title = "$locationName - $kioskName",
            navController = navController
        )

        Column (
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Text(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                text = stringResource(R.string.leasing_agent),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text)))

            Row (
                modifier = Modifier
                    .width(720.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                CustomTextButton(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.directory),
                    onClick = {
                        navController.navigate(DirectoryScreen)
                    }
                )
                Spacer(Modifier.width(16.dp))
                CustomTextButton(
                    modifier = Modifier
                        .weight(1f),
                    isGradient = true,
                    text = stringResource(R.string.video_call),
                    onClick = {navController.navigate(LeasingOfficeCallingScreen)}
                )
            }

            PinInputPanel(
                modifier = Modifier
                    .weight(1f)
                    .width(720.dp),
                buttons = buttons,
                message = stringResource(R.string.service_key_message_text)
            )

        }

    }

}



@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun LeasingOfficeScreenPreview() {
    val navController = rememberNavController()
    LeasingOfficeScreen(navController = navController)
}