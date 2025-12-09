package net.invictusmanagement.invictuskiosk.presentation.vacancy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.vacancy.components.ApartmentInfoDialog
import net.invictusmanagement.invictuskiosk.presentation.vacancy.components.ContactRequestDialog
import net.invictusmanagement.invictuskiosk.presentation.vacancy.components.ResponseDialog
import net.invictusmanagement.invictuskiosk.presentation.vacancy.components.TableHeader
import net.invictusmanagement.invictuskiosk.presentation.vacancy.components.TableRow
import net.invictusmanagement.invictuskiosk.util.locale.localizedString

@Composable
fun VacancyScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: VacancyViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val vacanciesState by viewModel.unitList.collectAsStateWithLifecycle()
    val contactRequestState by viewModel.contactRequestState.collectAsStateWithLifecycle()
    var showVacancyInfoDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var contactRequestSuccess by remember { mutableStateOf(false) }
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    var selectedRow by remember {
        mutableStateOf<net.invictusmanagement.invictuskiosk.domain.model.Unit?>(
            null
        )
    }

    LaunchedEffect(Unit, isConnected) {
        viewModel.getUnits()
    }

    LaunchedEffect(contactRequestState) {
        if (contactRequestState.contactRequest != null) {
            showContactDialog = false
        }
        contactRequestSuccess = contactRequestState.contactRequest != null
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
                text = localizedString(R.string.vacancies),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )

            Spacer(Modifier.height(8.dp))

            TableHeader()
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = colorResource(R.color.btn_text),
                thickness = 2.dp
            )

            if (vacanciesState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.btn_text))
                }
            } else {
                if (vacanciesState.vacancies.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = localizedString(R.string.no_vacancies),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = colorResource(
                                    R.color.btn_text
                                )
                            )
                        )
                    }
                } else {
                    LazyColumn {
                        itemsIndexed(vacanciesState.vacancies) { index, item ->
                            TableRow(
                                item,
                                index % 2 == 0,
                                onClick = {
                                    selectedRow = item
                                    showVacancyInfoDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

    }

    if (contactRequestSuccess) {
        ResponseDialog {
            contactRequestSuccess = false
        }
    }
    if (showContactDialog) {
        selectedRow?.let {
            ContactRequestDialog(
                onDismiss = { showContactDialog = false },
                requestState = contactRequestState,
                vacancy = it,
                onSend = { contactRequest ->
                    viewModel.sendContactRequest(contactRequest)
                }
            )
        }
    }
    if (showVacancyInfoDialog) {
        selectedRow?.let {
            ApartmentInfoDialog(
                vacancy = it,
                onDismiss = { showVacancyInfoDialog = false },
                onContactClick = {
                    showVacancyInfoDialog = false
                    showContactDialog = true
                }
            )
        }
    }
}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun VacancyScreenPreview() {
    val navController = rememberNavController()
    VacancyScreen(navController = navController)
}