package net.invictusmanagement.invictuskiosk.presentation.coupon_detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion
import net.invictusmanagement.invictuskiosk.domain.model.Promotion
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.coupons.CouponsScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.CouponDetailsScreen


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CouponsDetailsScreen(
    modifier: Modifier = Modifier,
    promotionId: Int,
    businessPromotion: BusinessPromotion,
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    val selectedPromotion =
        businessPromotion.promotions.find { it.id == promotionId } ?: Promotion()

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
                text = selectedPromotion.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = 0.8f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorResource(R.color.btn_pin_code))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Expires\n${Constants.formatDateString(businessPromotion.promotions[0].toUtc)}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = colorResource(R.color.btn_text)
                        ),
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterStart)
                    ) {
                        Text(
                            text = businessPromotion.name,
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = colorResource(R.color.btn_text)
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = selectedPromotion.name,
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = colorResource(R.color.btn_text)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = selectedPromotion.description,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = colorResource(R.color.btn_text)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Column {
                            Text(
                                text = "${businessPromotion.address1}, ${businessPromotion.address2}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = colorResource(R.color.btn_text)
                                )
                            )
                            Text(
                                text = "${businessPromotion.city}, ${businessPromotion.state} ${businessPromotion.zip}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = colorResource(R.color.btn_text)
                                )
                            )
                            Text(
                                text = "Phone: ${businessPromotion.phone}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = colorResource(R.color.btn_text)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Take a picture of the coupon to show the vendor and let’s keep shopping.",
                    style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

        }


    }

}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun CouponDetailsScreenPreview() {
    val navController = rememberNavController()
    CouponsDetailsScreen(
        navController = navController,
        promotionId = 28,
        businessPromotion = BusinessPromotion(
            name = "Invictus Test Business",
            address1 = "1821 Pacific Coast Hwy",
            address2 = "",
            city = "Hermosa Beach",
            state = "CA",
            zip = "90254",
            phone = "(425) 545-4545",
            promotions = listOf(
                Promotion(
                    name = "Invictus Test Coupon",
                    description = "Test Description",
                    fromUtc = "2025-10-16T10:26:00",
                    toUtc = "2025-11-16T10:26:00",
                    planFromUtc = "0001-01-01T00:00:00",
                    planToUtc = "0001-01-01T00:00:00",
                    isApproved = true,
                    advertise = false,
                    approvedUtc = "2025-10-16T10:26:53.2063419",
                    numberOfUse = 0,
                    revenueTotal = 0.0,
                    businessId = 38,
                    isShowOnHome = false,
                    isAnytimeCoupon = false,
                    id = 28,
                    createdUtc = "2025-10-16T10:26:48.01",
                    deleted = false
                )
            )
        )
    )

}