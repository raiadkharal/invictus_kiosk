package net.invictusmanagement.invictuskiosk.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.domain.model.Resident
import net.invictusmanagement.invictuskiosk.presentation.components.CustomIconButton
import net.invictusmanagement.invictuskiosk.presentation.home.HomeViewModel
import net.invictusmanagement.invictuskiosk.presentation.residents.components.ResidentListItem

@Composable
fun HomeBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onResidentClick: (Resident) -> Unit = {},
    onQrCodeClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onCallBtnClick: (Resident) -> Unit = {}
) {

    val residentsState by viewModel.residentState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.getAllResidents()
    }
    Row(
        modifier = modifier
            .fillMaxSize()
            .heightIn(max = 400.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 30.dp)
    ) {
        Column(
            modifier = Modifier.weight(10f)
                .fillMaxSize()
        )      {
            if(residentsState.isLoading){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            }else{
                if(residentsState.residents.isNullOrEmpty()){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center,
                    ){
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.no_residents_found),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
                        )
                    }
                }else{
                    // Residents List
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(residentsState.residents!!) { resident ->
                            ResidentListItem(
                                residentName = resident.displayName,
                                onCallClick = {onCallBtnClick(resident)},
                                onItemClick = {onResidentClick(resident)}
                            )
                        }
                    }
                }
            }

        }

        Spacer(Modifier.width(16.dp))
        VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 2.dp, color = colorResource(
            R.color.divider_color)
        )
        Spacer(Modifier.width(16.dp))

        // Pin or QR Code Section
        Column(
            modifier = Modifier.weight(2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ){
                IconButton(
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color = colorResource(R.color.btn_text))
                        .padding(vertical = 20.dp),
                    onClick = onBackClick
                ) {
                    Icon(
                        modifier = Modifier.width(50.dp).height(50.dp),
                       imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = colorResource(R.color.btn_pin_code)
                    )
                }
                Spacer(Modifier.width(16.dp))
                IconButton(
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color = colorResource(R.color.btn_pin_code))
                        .padding(vertical = 20.dp),
                    onClick = onHomeClick
                ) {
                    Icon(
                        modifier = Modifier.width(50.dp).height(50.dp),
                       imageVector =  Icons.Default.Home,
                        contentDescription = "Home",
                        tint = colorResource(R.color.btn_text)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Row (
                modifier = Modifier.weight(1f)
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ){
                CustomIconButton(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = R.drawable.ic_qr_code,
                    iconSize = 100,
                    text = stringResource(R.string.qr_code),
                    onClick = onQrCodeClick
                )
            }
        }
    }
}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun BottomSheetHomePreview() {
    HomeBottomSheet(viewModel = hiltViewModel())
}