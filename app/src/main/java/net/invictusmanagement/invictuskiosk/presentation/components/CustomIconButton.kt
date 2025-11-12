package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.navigation.ResidentsScreen

@Composable
fun CustomIconButton(
    modifier: Modifier = Modifier,
    icon: Int,
    iconSize: Int = 80,
    text: String,
    onClick:()->Unit = {}
) {

    val gradient = Brush.linearGradient(
        0.0f to colorResource(R.color.background),
        10.0f to colorResource(R.color.btn_gradient_end).copy(alpha = 0.4f),
        start = Offset(0f, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, 0f)
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .border(1.dp, colorResource(R.color.btn_text), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier
                .width(iconSize.dp)
                .height(iconSize.dp),
            painter = painterResource(icon),
            tint = colorResource(R.color.btn_text),
            contentDescription = "Directory"
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
        )
    }
}

@Preview
@Composable
private fun CustomIconButtonPreview() {
    CustomIconButton(
        icon = R.drawable.ic_directory,
        text = stringResource(R.string.directory),
        onClick = {

        })
}