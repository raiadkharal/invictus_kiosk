package net.invictusmanagement.invictuskiosk.presentation.coupons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import net.invictusmanagement.invictuskiosk.presentation.navigation.CouponsDetailScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CouponsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: CouponsViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val couponsList by viewModel.state.collectAsState()
    val locationName by mainViewModel.locationName.collectAsState()
    val kioskName by mainViewModel.kioskName.collectAsState()

    LaunchedEffect (Unit){
        viewModel.getPromotionsCategory()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomToolbar(
            title = "$locationName - $kioskName",
            navController = navController
        )

        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.coupons),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )

            Spacer(Modifier.height(16.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.category),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text))
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                thickness = 2.dp,
                color = colorResource(R.color.btn_text)
            )
            Spacer(Modifier.height(8.dp))

            if(couponsList.isEmpty()){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.no_coupons_available),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text))
                    )
                }
            }else{
                FlowRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    couponsList.forEach { coupon ->
                        CustomTextButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(170.dp)
                                .width(300.dp)
                                .padding(end = 16.dp, top = 16.dp),
                            text = coupon.name,
                            padding = 48,
                            isGradient = true,
                            onClick = { navController.navigate(CouponsDetailScreen(coupon.id)) }
                        )
                    }
                }
            }
        }

    }

}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun CouponsScreenPreview() {
    val navController = rememberNavController()
    CouponsScreen(navController = navController)
}