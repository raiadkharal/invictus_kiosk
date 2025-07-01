package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@Composable
fun CustomTextButton(
    modifier: Modifier = Modifier,
    text: String,
    isGradient: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(brush = if(isGradient) Brush.linearGradient(
                colors = listOf(
                    colorResource(id = R.color.btn_gradient_start),
                    colorResource(id = R.color.btn_gradient_end)
                )
            ) else Brush.linearGradient(
                colors = listOf(
                    colorResource(id = R.color.background),
                    colorResource(id = R.color.background)
                )
            ))
            .border(1.dp, colorResource(R.color.btn_text), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, textAlign = TextAlign.Center,style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text)))
    }
}