package net.invictusmanagement.invictuskiosk.presentation.vacancy.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TableHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TableCell(modifier = Modifier.weight(1f),"Bed / Bath", isHeader = true)
        TableCell(modifier = Modifier.weight(1f),"Square Feet", isHeader = true)
        TableCell(modifier = Modifier.weight(1f),"Floor", isHeader = true)
        TableCell(modifier = Modifier.weight(1f),"Price", isHeader = true)
    }
}