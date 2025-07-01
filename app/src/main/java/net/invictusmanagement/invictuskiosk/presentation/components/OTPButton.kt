package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@Composable
fun OTPButton(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle=MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text)),
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(72.dp)
            .background(
                color = colorResource(R.color.btn_pin_code),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = textStyle,
            fontWeight = FontWeight.Bold
        )
    }
}