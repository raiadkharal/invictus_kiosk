package net.invictusmanagement.invictuskiosk.presentation.vacancy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.Constants

@Composable
fun TableRow(
    vacancy: net.invictusmanagement.invictuskiosk.domain.model.Unit,
    isStriped: Boolean,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isStriped) colorResource(R.color.table_striped_background) else colorResource(
        R.color.background)
    Row(
        Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TableCell(modifier = Modifier.weight(1f),"${Constants.formatNumber(vacancy.bedrooms)}/${Constants.formatNumber(vacancy.bathrooms)}")
        TableCell(modifier = Modifier.weight(1f),"${Constants.formatNumber(vacancy.area)} sqft")
        TableCell(modifier = Modifier.weight(1f),"${vacancy.floor}")
        TableCell(modifier = Modifier.weight(1f),"$${Constants.formatNumber(vacancy.rent)}")
    }
}