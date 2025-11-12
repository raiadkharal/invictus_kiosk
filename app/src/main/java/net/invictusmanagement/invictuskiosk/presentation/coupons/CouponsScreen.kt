package net.invictusmanagement.invictuskiosk.presentation.coupons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.components.EmptyStateMessage
import net.invictusmanagement.invictuskiosk.presentation.components.LoadingIndicator
import net.invictusmanagement.invictuskiosk.presentation.navigation.CouponsBusinessListScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CouponsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: CouponsViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val categoryState by viewModel.state.collectAsStateWithLifecycle()
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
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

            when {
                categoryState.isLoading -> {
                    LoadingIndicator()
                }

                categoryState.couponsCategories.isEmpty() -> {
                    EmptyStateMessage(messageResId = R.string.no_coupons_available)
                }

                else -> {
                    CouponsGrid(
                        categories = categoryState.couponsCategories,
                        onCategoryClick = { coupon ->
                            navController.navigate(CouponsBusinessListScreen(coupon.id))
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CouponsGrid(
    categories: List<PromotionsCategory>,
    onCategoryClick: (PromotionsCategory) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Top
    ) {
        categories.forEach { coupon ->
            CustomTextButton(
                modifier = Modifier
                    .weight(1f)
                    .height(170.dp)
                    .width(300.dp)
                    .padding(end = 16.dp, top = 16.dp),
                text = coupon.name,
                padding = 48,
                isGradient = true,
                onClick = { onCategoryClick(coupon) }
            )
        }
    }
}

@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun CouponsScreenPreview() {
    val navController = rememberNavController()
    CouponsScreen(navController = navController)
}