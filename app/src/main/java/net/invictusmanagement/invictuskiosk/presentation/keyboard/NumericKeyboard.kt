package net.invictusmanagement.invictuskiosk.presentation.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import net.invictusmanagement.invictuskiosk.R

@Composable
fun NumericKeyboard(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onKeyboardSwitch: () -> Unit,
    onDone: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.4f)
            .background(colorResource(R.color.background))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { key ->
                    KeyboardKey(
                        text = key,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(key) }
                    )
                }
            }
        }

        BottomActionRow(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onKeyPress = onKeyPress,
            onBackspace = onBackspace,
            onKeyboardSwitch = onKeyboardSwitch,
            onDone = onDone
        )
    }
}

@Composable
private fun BottomActionRow(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBackspace: () -> Unit,
    onKeyboardSwitch: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        KeyboardActionKey(
            text = "ABC",
            modifier = Modifier.weight(1f),
            background = Color(0xFF444444),
            onClick = onKeyboardSwitch
        )

        KeyboardKey(
            text = ".",
            modifier = Modifier.weight(1f),
            onClick = { onKeyPress(".") }
        )

        KeyboardKey(
            text = "-",
            modifier = Modifier.weight(1f),
            onClick = { onKeyPress("-") }
        )

        KeyboardKey(
            text = "0",
            modifier = Modifier.weight(3f),
            onClick = { onKeyPress("0") }
        )

        KeyboardActionKey(
            text = "⌫",
            modifier = Modifier.weight(1.5f),
            background = Color(0xFF3A3A3A),
            onClick = onBackspace
        )

        KeyboardActionKey(
            text = "✓",
            modifier = Modifier.weight(1.5f),
            background = colorResource(R.color.btn_pin_code),
            onClick = onDone
        )
    }
}

@Composable
private fun KeyboardKey(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = keyboardHaptic()

    Box(
        modifier = modifier
            .height(88.dp)
            .background(
                color = Color(0xFF2A2A2A),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                haptic()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
    }
}

@Composable
private fun KeyboardActionKey(
    text: String,
    modifier: Modifier = Modifier,
    background: Color,
    onClick: () -> Unit
) {
    val haptic = keyboardHaptic()

    Box(
        modifier = modifier
            .height(88.dp)
            .background(
                color = background,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                haptic()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
    }
}



