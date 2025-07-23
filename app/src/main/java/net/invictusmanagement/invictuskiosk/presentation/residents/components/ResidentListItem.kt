package net.invictusmanagement.invictuskiosk.presentation.residents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@Composable
fun ResidentListItem(
    residentName: String,
    showCallButton: Boolean = false,
    onCallClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = residentName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineLarge.copy(color = colorResource(R.color.btn_text))
        )

        if (showCallButton) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorResource(R.color.btn_pin_code))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Call",
                    style = MaterialTheme.typography.bodyLarge.copy(color = colorResource(R.color.btn_text))
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    modifier = Modifier.clickable { onCallClick() },
                    imageVector = Icons.Filled.Phone,
                    contentDescription = "Call",
                    tint = colorResource(R.color.btn_text)
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