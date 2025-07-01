package net.invictusmanagement.invictuskiosk.presentation.service_key

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel

@Composable
fun ServiceKeyScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    Column (
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally,
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

        Column (
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.service_key),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text)))

            Spacer(Modifier.height(8.dp))

            val buttons:List<List<String>> = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("0","X","clear"))

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
private fun ServiceScreenPreview() {
    val navController = rememberNavController()
    ServiceKeyScreen(navController = navController)
}