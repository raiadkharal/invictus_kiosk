package net.invictusmanagement.invictuskiosk.presentation.coupons_business_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
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
import net.invictusmanagement.invictuskiosk.presentation.components.EmptyStateMessage
import net.invictusmanagement.invictuskiosk.presentation.components.LoadingIndicator
import net.invictusmanagement.invictuskiosk.presentation.coupons.CouponsViewModel
import net.invictusmanagement.invictuskiosk.presentation.navigation.CouponListScreen
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

    val categoryState by viewModel.state.collectAsStateWithLifecycle()
    val couponsBusinessState by viewModel.businessPromotions.collectAsStateWithLifecycle()
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    val filteredCategories =
        categoryState.couponsCategories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    var selectedCategory by remember { mutableStateOf<PromotionsCategory?>(null) }

    LaunchedEffect(categoryState) {
        selectedCategory = categoryState.couponsCategories.find { it.id == selectedCouponId }
    }

    LaunchedEffect(Unit) {
        viewModel.getPromotionsCategory()
        viewModel.getPromotionsByCategory(selectedCouponId)
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

            Row {
                CouponsCategorySection(
                    modifier = Modifier
                        .weight(6f),
                    filteredCategories = filteredCategories,
                    selectedCoupon = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        viewModel.getPromotionsByCategory(category.id)
                    }
                )

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    thickness = 2.dp,
                    color = colorResource(R.color.divider_color)
                )

                PromotionsSection(
                    modifier = Modifier.weight(4f),
                    selectedCategory = selectedCategory,
                    state = couponsBusinessState,
                    selectedCouponId = selectedCouponId,
                    navController = navController
                )

//                Column(
//                    modifier = Modifier
//                        .weight(4f)
//                ) {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(colorResource(R.color.btn_pin_code))
//                            .padding(8.dp),
//                        text = selectedCategory?.name ?: "",
//                        textAlign = TextAlign.Start,
//                        style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text))
//                    )
//
//                    when {
//                        couponsBusinessState.isLoading -> LoadingIndicator()
//
//                        couponsBusinessState.businessPromotions.isEmpty() -> EmptyStateMessage(
//                            messageResId = R.string.no_coupons_available
//                        )
//
//                        else -> BusinessPromotionsList(
//                            promotions = couponsBusinessState.businessPromotions,
//                            onPromotionClick = { promotion ->
//                                val json = Json.encodeToString(promotion)
//                                val encodedJson =
//                                    URLEncoder.encode(json, StandardCharsets.UTF_8.toString())
//
//                                navController.navigate(
//                                    CouponListScreen(
//                                        businessPromotionJson = encodedJson,
//                                        selectedCouponId = selectedCouponId
//                                    )
//                                )
//                            }
//                        )
//                    }
//                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CouponsCategorySection(
    modifier: Modifier = Modifier,
    filteredCategories: List<PromotionsCategory>,
    selectedCoupon: PromotionsCategory?,
    onCategorySelected: (PromotionsCategory) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.category),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colorResource(R.color.btn_text)
                )
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                thickness = 2.dp,
                color = colorResource(R.color.btn_text)
            )

            Spacer(Modifier.height(8.dp))
        }

        CategoryGrid(
            categories = filteredCategories,
            selectedCategory = selectedCoupon,
            onCategoryClick = onCategorySelected
        )
    }
}

@Composable
private fun PromotionsSection(
    modifier: Modifier = Modifier,
    selectedCategory: PromotionsCategory?,
    state: CouponsBusinessState,
    selectedCouponId: String,
    navController: NavController
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        // Category title header
        Text(
            text = selectedCategory?.name.orEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colorResource(R.color.btn_pin_code))
                .padding(8.dp),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = colorResource(R.color.btn_text)
            )
        )

        when {
            state.isLoading -> LoadingIndicator()

            state.businessPromotions.isEmpty() -> EmptyStateMessage(
                messageResId = R.string.no_coupons_available
            )

            else -> BusinessPromotionsList(
                promotions = state.businessPromotions,
                onPromotionClick = { promotion ->
                    val json = Json.encodeToString(promotion)
                    val encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8.toString())

                    navController.navigate(
                        CouponListScreen(
                            businessPromotionJson = encodedJson,
                            selectedCouponId = selectedCouponId
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryGrid(
    categories: List<PromotionsCategory>,
    selectedCategory: PromotionsCategory?,
    onCategoryClick: (PromotionsCategory) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Top
    ) {
        categories.forEach { category ->
            CustomTextButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp, top = 16.dp)
                    .height(180.dp),
                text = category.name,
                padding = 48,
                isSelected = category == selectedCategory,
                isGradient = true,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}


@Composable
private fun BusinessPromotionsList(
    promotions: List<BusinessPromotion>,
    onPromotionClick: (BusinessPromotion) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(promotions) { promotion ->
            PromotionItem(
                promotion = promotion,
                onClick = { onPromotionClick(promotion) }
            )
        }
    }
}

@Composable
private fun PromotionItem(
    promotion: BusinessPromotion,
    onClick: () -> Unit
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        text = promotion.name,
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.headlineSmall.copy(
            color = colorResource(
                R.color.btn_text
            )
        )
    )
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