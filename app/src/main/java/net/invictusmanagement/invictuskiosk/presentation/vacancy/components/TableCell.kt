package net.invictusmanagement.invictuskiosk.presentation.vacancy.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@Composable
fun TableCell(
    modifier: Modifier = Modifier,
    text: String,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier
            .padding(horizontal = 8.dp),
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text))
    )
}
