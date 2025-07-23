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
import androidx.compose.ui.geometry.Offset
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
    padding: Int = 16,
    isDarkBackground: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if(isGradient && !isSelected) Brush.linearGradient(
                0.0f to colorResource(R.color.background),
                10.0f to colorResource(R.color.btn_gradient_end).copy(alpha = 0.4f),
                start = Offset(0f, Float.POSITIVE_INFINITY),
                end = Offset(Float.POSITIVE_INFINITY, 0f)
            ) else if(isSelected) Brush.linearGradient(
                0.0f to colorResource(id = R.color.btn_pin_code),
                10.0f to colorResource(id = R.color.btn_pin_code),
                start = Offset(0f, Float.POSITIVE_INFINITY),
                end = Offset(Float.POSITIVE_INFINITY, 0f)
            ) else Brush.linearGradient(
                0.0f to colorResource(id = if (isDarkBackground) R.color.background_dark else R.color.background),
                10.0f to colorResource(id = if (isDarkBackground) R.color.background_dark else R.color.background),
                start = Offset(0f, Float.POSITIVE_INFINITY),
                end = Offset(Float.POSITIVE_INFINITY, 0f)
            ))
            .border(1.dp, if(!isSelected)colorResource(R.color.btn_text) else colorResource(R.color.btn_pin_code), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(padding.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, textAlign = TextAlign.Center,style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text)))
    }
}