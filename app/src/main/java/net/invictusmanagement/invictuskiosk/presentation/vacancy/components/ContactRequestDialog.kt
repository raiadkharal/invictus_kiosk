package net.invictusmanagement.invictuskiosk.presentation.vacancy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardViewModel
import net.invictusmanagement.invictuskiosk.presentation.vacancy.ContactRequestState

@Composable
fun ContactRequestDialog(
    onDismiss: () -> Unit,
    requestState: ContactRequestState,
    vacancy: net.invictusmanagement.invictuskiosk.domain.model.Unit,
    onSend: (ContactRequest) -> Unit = {},
    keyboardVM: KeyboardViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) {} // block touches behind
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            color = colorResource(R.color.background_dark),
            tonalElevation = 8.dp
        ) {
            ContactRequestContent(
                onDismiss = onDismiss,
                requestState = requestState,
                vacancy = vacancy,
                onSend = onSend,
                keyboardVM = keyboardVM
            )
        }
    }
}
