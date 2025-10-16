package net.invictusmanagement.invictuskiosk.presentation.directory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.components.QRCodePanel
import net.invictusmanagement.invictuskiosk.presentation.components.SearchTextField
import net.invictusmanagement.invictuskiosk.presentation.directory.components.FilterDropdownButton
import net.invictusmanagement.invictuskiosk.presentation.directory.components.FilterListItem
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.QRScannerScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.ResidentsScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreenRoute
import net.invictusmanagement.invictuskiosk.ui.theme.InvictusKioskTheme
import net.invictusmanagement.invictuskiosk.util.FilterOption

@Composable
fun DirectoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: DirectoryViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val alphabets = listOf("Leasing Office / agents") + ('A'..'Z').map { it.toString() }


    val unitList by viewModel.unitList.collectAsStateWithLifecycle()
    val keyValidationState by viewModel.keyValidationState.collectAsStateWithLifecycle()
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()
    val isUnitFilterEnabled by mainViewModel.isUnitFilterEnabled.collectAsStateWithLifecycle()


    var searchQuery by remember { mutableStateOf("") }
    var list by remember { mutableStateOf(emptyList<String>()) }
    var selectedFilterOption by remember { mutableStateOf(FilterOption.FIRST_NAME) }
    var isUnitNumberSelected by remember { mutableStateOf(false) }
    var isFirstNameSelected by remember { mutableStateOf(false) }
    val filteredList = list.filter { it.contains(searchQuery, ignoreCase = true) }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }
    LaunchedEffect(unitList) {
        if (isUnitFilterEnabled) {
            isUnitNumberSelected = true
            selectedFilterOption = FilterOption.UNIT_NUMBER
            list = unitList?.map { it.unitNbr } ?: emptyList()
        }
    }
    LaunchedEffect(keyValidationState) {
        if (keyValidationState.digitalKey?.isValid == true) {
            navController.navigate(
                UnlockedScreenRoute(
                    unitId = keyValidationState.digitalKey?.unitId ?: 0,
                    mapId = keyValidationState.digitalKey?.mapId ?: 0,
                    toPackageCenter = keyValidationState.digitalKey?.toPackageCenter ?: false
                )
            ){
                popUpTo(HomeScreen)
            }
        }
    }
    LaunchedEffect(selectedFilterOption) {
        when (selectedFilterOption) {
            FilterOption.FIRST_NAME -> {
                isFirstNameSelected = true
                isUnitNumberSelected = false
                list = alphabets
            }

            FilterOption.LAST_NAME -> {
                isFirstNameSelected = false
                isUnitNumberSelected = false
                list = alphabets
            }

            FilterOption.UNIT_NUMBER -> {
                isUnitNumberSelected = true
                list = unitList?.map { it.unitNbr } ?: emptyList()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomToolbar(
            title = "$locationName - $kioskName",
            navController = navController
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.background))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(6f)
                    .fillMaxSize()
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //search bar
                    SearchTextField(
                        modifier = Modifier.weight(7f),
                        searchQuery = searchQuery,
                        onValueChange = { searchQuery = it }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    // Filter Button
                    FilterDropdownButton(
                        modifier = Modifier.weight(4f),
                        selectedOption = selectedFilterOption,
                        onOptionSelected = {
                            selectedFilterOption = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Residents List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredList) { item ->
                        FilterListItem(
                            name = item,
                            onClick = {
                                //navigate to residents screen
                                navController.navigate(
                                    ResidentsScreen(
                                        isLeasingOffice = item == "Leasing Office / agents",
                                        isUnitSelected = isUnitNumberSelected,
                                        unitNumber = if (isUnitNumberSelected) item else "",
                                        filter = if (!isUnitNumberSelected) item else "",
                                        byName = if (isFirstNameSelected) "f" else "l"
                                    )
                                )
                            }
                        )
                    }
                }

            }

            Spacer(Modifier.width(16.dp))
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 2.dp,
                color = colorResource(R.color.divider_color)
            )
            Spacer(Modifier.width(16.dp))

            // Pin or QR Code Section
            Column(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.qr_code_title_text),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
                )

                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    visible = true
                ) {
                    QRCodePanel(
                        modifier = Modifier
                            .fillMaxSize(),
                        imageWidth = 350.dp,
                        imageHeight = 350.dp,
                        onScanClick = { navController.navigate(QRScannerScreen) }
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun DirectoryScreenPreview() {
    InvictusKioskTheme {
        val navController = rememberNavController()
        DirectoryScreen(navController = navController)
    }
}