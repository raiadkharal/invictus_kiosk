package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.LoginScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.QRScannerScreen
import kotlin.math.log

@Composable
fun CustomToolbar(
    modifier: Modifier = Modifier,
    title: String,
    navController: NavController,
    showBackArrow: Boolean = true,
    onBack: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_dark))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackArrow) {
            Image(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clickable {
                        if (navController.previousBackStackEntry != null) {
                            onBack()
                            navController.popBackStack()
                        }
                    },
                painter = painterResource(R.drawable.angle_left),
                contentDescription = "Back arrow icon"
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.btn_text)
            )
        )
    }
}
