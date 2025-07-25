package net.invictusmanagement.invictuskiosk.presentation.residents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@Composable
fun ResidentListItem(
    residentName: String,
    onItemClick: () -> Unit = {},
    isSelected: Boolean = false,
    onCallClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) colorResource(R.color.btn_pin_code) else colorResource(R.color.background))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = residentName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineLarge.copy(color = colorResource(R.color.btn_text))
        )

        if (isSelected) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorResource(R.color.btn_text))
                    .clickable { onCallClick() }
                    .padding(vertical = 16.dp,horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier
                        .size(20.dp),
                    painter = painterResource(R.drawable.ic_video),
                    contentDescription = "Call",
                    tint = colorResource(R.color.btn_pin_code),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Video Call",
                    style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.btn_pin_code), fontWeight = FontWeight.Bold)
                )
            }
        }

    }
}

@Preview
@Composable
private fun ResidentListItemPreview() {
    ResidentListItem("James Bell")
}