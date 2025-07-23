package net.invictusmanagement.invictuskiosk.presentation.error

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    errorMessage: String,
    navController: NavController
) {
   Box(
       modifier = modifier.fillMaxSize()
           .background(colorResource(R.color.background)),
       contentAlignment = Alignment.Center
   ){
       Column(
           modifier = Modifier.fillMaxSize()
               .padding(24.dp),
           horizontalAlignment = Alignment.CenterHorizontally,
           verticalArrangement = Arrangement.Center
       ) {

           Text(
               modifier = Modifier
                   .fillMaxWidth(),
               text = stringResource(R.string.notification),
               textAlign = TextAlign.Center,
               style = MaterialTheme.typography.displayMedium.copy(color = colorResource(R.color.btn_text), fontWeight = FontWeight.Bold)
           )
           Spacer(Modifier.height(16.dp))
           Text(
               modifier = Modifier
                   .fillMaxWidth(),
               text = errorMessage,
               textAlign = TextAlign.Center,
               style = MaterialTheme.typography.displaySmall.copy(color = colorResource(R.color.btn_text))
           )
           Spacer(Modifier.height(24.dp))
           CustomTextButton(
               modifier = Modifier.fillMaxWidth(0.3f),
               padding = 24,
               isGradient = true,
               text = stringResource(R.string.dismiss),
               onClick = {
                   navController.popBackStack()
               }
           )

       }
   }
}