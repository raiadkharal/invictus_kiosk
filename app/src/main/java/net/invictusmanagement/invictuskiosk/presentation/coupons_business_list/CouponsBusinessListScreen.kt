package net.invictusmanagement.invictuskiosk.presentation.coupons_business_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.coupons.CouponsViewModel
import net.invictusmanagement.invictuskiosk.presentation.navigation.CouponListScreen
import net.invictusmanagement.invictuskiosk.util.locale.localizedString
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CouponsBusinessListScreen(
    modifier: Modifier = Modifier,
    selectedCouponId: String,
    navController: NavController,
    viewModel: CouponsViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }

    val isConnected by mainViewModel.isConnected.collectAsStateWithLifecycle()
    val categoryState by viewModel.state.collectAsStateWithLifecycle()
    val couponsBusinessState by viewModel.businessPromotions.collectAsStateWithLifecycle()
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    val filteredCoupons = categoryState.couponsCategories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    var selectedCoupon by remember { mutableStateOf<PromotionsCategory?>(null) }

    LaunchedEffect(categoryState) {
        selectedCoupon = categoryState.couponsCategories.find { it.id == selectedCouponId }
    }

    LaunchedEffect(Unit,isConnected) {
        if (isConnected) {
            viewModel.getPromotionsCategory()
            viewModel.getPromotionsByCategory(selectedCouponId)
        }
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
                text = localizedString(R.string.coupons),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )

            Spacer(Modifier.height(16.dp))

            Row {
                Column(
                    modifier = Modifier
                        .weight(6f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    //search bar
//                    SearchTextField(
//                        modifier = Modifier.fillMaxWidth(),
//                        searchQuery = searchQuery,
//                        placeholder = "Search Coupons",
//                        onValueChange = { searchQuery = it }
//                    )

//                    Spacer(Modifier.height(16.dp))

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = localizedString(R.string.category),
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

                    FlowRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Start,
                        verticalArrangement = Arrangement.Top
                    ) {
                        filteredCoupons?.forEach { coupon ->
                            CustomTextButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .padding(end = 16.dp, top = 16.dp),
                                text = coupon.name,
                                padding = 48,
                                isSelected = coupon == selectedCoupon,
                                isGradient = true,
                                onClick = {
                                    selectedCoupon = coupon
                                    viewModel.getPromotionsByCategory(coupon.id)
                                }
                            )
                        }
                    }
                }

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    thickness = 2.dp,
                    color = colorResource(R.color.divider_color)
                )

                Column(
                    modifier = Modifier
                        .weight(4f)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorResource(R.color.btn_pin_code))
                            .padding(8.dp),
                        text = selectedCoupon?.name ?: "",
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text))
                    )

                    when {
                        couponsBusinessState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        couponsBusinessState.businessPromotions.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = localizedString(R.string.no_coupons_available),
                                    textAlign = TextAlign.Start,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        color = colorResource(
                                            R.color.btn_text
                                        )
                                    )
                                )
                            }
                        }

                        else -> {
                            LazyColumn {
                                items(couponsBusinessState.businessPromotions) { businessPromotion ->
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable(onClick = {

                                                val json = Json.encodeToString<BusinessPromotion>(
                                                    businessPromotion
                                                )
                                                val businessPromotionJson = URLEncoder.encode(
                                                    json,
                                                    StandardCharsets.UTF_8.toString()
                                                )

                                                navController.navigate(
                                                    CouponListScreen(
                                                        businessPromotionJson = businessPromotionJson,
                                                        selectedCouponId = selectedCouponId
                                                    )
                                                )
                                            }),
                                        text = businessPromotion.name,
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = colorResource(
                                                R.color.btn_text
                                            )
                                        )
                                    )

                                }
                            }
                        }
                    }
                }
            }
        }


    }

}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun CouponsDetailScreenPreview() {
    val navController = rememberNavController()
    CouponsBusinessListScreen(
        navController = navController, viewModel = hiltViewModel(),
        selectedCouponId = ""
    )
}