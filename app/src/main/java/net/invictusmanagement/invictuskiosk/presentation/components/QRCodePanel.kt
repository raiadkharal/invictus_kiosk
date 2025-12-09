package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.util.locale.localizedString


@Composable
fun QRCodePanel(
    modifier: Modifier = Modifier,
    imageWidth: Dp = 250.dp,
    imageHeight: Dp = 250.dp,
    onScanClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .border(width = 2.dp, color = colorResource(R.color.btn_text),shape = MaterialTheme.shapes.large)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .width(imageWidth)
                .height(imageHeight),
            painter = painterResource(R.drawable.qr_code_mobile),
            contentDescription = "QR Code"
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text =localizedString(R.string.qr_code_instructions),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = colorResource(R.color.btn_text)),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))
        OTPButton(
            modifier = Modifier.width(230.dp),
            backgroundColor = R.color.btn_text,
            text = localizedString(R.string.scan_qr_code),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = colorResource(R.color.btn_pin_code)
            ),
            onClick = onScanClick
        )
    }
}


@Preview
@Composable
private fun QRCodePanelPreview() {
    QRCodePanel()
}