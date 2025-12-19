package net.invictusmanagement.invictuskiosk.presentation.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.R

@Composable
fun QwertyKeyboard(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onKeyboardSwitch: () -> Unit,
    onDone: () -> Unit
) {

    var isUpperCase by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.3f)
            .background(colorResource(R.color.background))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        KeyboardRow("qwertyuiop", isUpperCase, onKeyPress)

        KeyboardRow(
            "asdfghjkl",
            isUpperCase,
            onKeyPress,
            horizontalPadding = 20.dp
        )

        KeyboardRow(
            "zxcvbnm",
            isUpperCase,
            onKeyPress,
            horizontalPadding = 40.dp
        )

        BottomActionRow(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onKeyPress = onKeyPress,
            onSpace = { onKeyPress(" ") },
            onBackspace = onBackspace,
            onKeyboardSwitch = onKeyboardSwitch,
            onDone = onDone,
            isUpperCase = isUpperCase,
            onToggleCase = { isUpperCase = !isUpperCase }
        )
    }
}

@Composable
private fun KeyboardRow(
    keys: String,
    isUpperCase: Boolean,
    onKeyPress: (String) -> Unit,
    horizontalPadding: Dp = 0.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { char ->
            val displayChar =
                if (isUpperCase) char.uppercaseChar() else char.lowercaseChar()
            KeyboardKey(
                text = displayChar.toString(),
                modifier = Modifier.weight(1f),
                onClick = { onKeyPress(displayChar.toString()) }
            )
        }
    }
}

@Composable
private fun BottomActionRow(
    modifier: Modifier = Modifier,
    onKeyPress: (String) -> Unit,
    onSpace: () -> Unit,
    onBackspace: () -> Unit,
    onKeyboardSwitch: () -> Unit,
    onDone: () -> Unit,
    isUpperCase: Boolean,
    onToggleCase: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var backspaceJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            backspaceJob?.cancel()
        }
    }

    Row(
        modifier = modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        KeyboardIconKey(
            icon = "⇧",
            modifier = Modifier.weight(0.5f),
            background = if (isUpperCase) Color(0xFF555555) else Color(0xFF333333),
            onClick = onToggleCase
        )

        KeyboardKey(
            text = "123",
            modifier = Modifier.weight(0.5f),
            background = Color(0xFF3A3A3A),
            onClick = onKeyboardSwitch
        )

        KeyboardKey(
            text = ".",
            modifier = Modifier.weight(0.5f),
            background = Color(0xFF3A3A3A),
            onClick = { onKeyPress(".") }
        )

        KeyboardKey(
            text = "Space",
            modifier = Modifier.weight(3f),
            background = Color(0xFF3A3A3A),
            onClick = onSpace
        )

        KeyboardKey(
            text = "@",
            modifier = Modifier.weight(0.5f),
            background = Color(0xFF3A3A3A),
            onClick = { onKeyPress("@") }
        )

        KeyboardIconKey(
            icon = "⌫",
            modifier = Modifier.weight(0.5f),
            background = Color(0xFF444444),
            onClick = {
                backspaceJob?.cancel()
                onBackspace()
            },
            onLongPress = {
                backspaceJob?.cancel()
                backspaceJob = scope.launch {
                    while (true) {
                        onBackspace()
                        delay(80) // repeat speed
                    }
                }
            },
            onRelease = {
                backspaceJob?.cancel() // STOP IMMEDIATELY
            }
        )

        KeyboardIconKey(
            icon = "✓",
            modifier = Modifier.weight(0.5f),
            background = colorResource(R.color.btn_pin_code), // green confirm
            onClick = onDone
        )
    }
}

@Composable
private fun KeyboardKey(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = Color(0xFF2F2F2F),
    onClick: () -> Unit
) {
    val haptic = keyboardHaptic()

    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable {
                haptic()
                onClick()
            },
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 4.dp,
        color = background
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colorResource(R.color.btn_text),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun KeyboardIconKey(
    icon: String,
    modifier: Modifier = Modifier,
    background: Color = Color(0xFF2F2F2F),
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    onRelease: (() -> Unit)? = null
) {
    val haptic = keyboardHaptic()

    Surface(
        modifier = modifier
            .height(56.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        haptic()
                        onRelease?.invoke()
                        onClick()
                    },
                    onLongPress = {
                        haptic()
                        onLongPress?.invoke()
                    },
                    onPress = {
                        try {
                            awaitRelease()
                        } finally {
                            onRelease?.invoke() //STOP on finger up / cancel
                        }
                    }
                )
            },
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 4.dp,
        color = background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colorResource(R.color.btn_text),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

