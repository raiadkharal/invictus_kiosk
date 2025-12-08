package net.invictusmanagement.invictuskiosk.presentation.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun CameraAndAudioPermission(
    onGranted: () -> Unit
) {
    val context = LocalContext.current

    var showPermissionDialog by remember { mutableStateOf(false) }

    // Re-check permission when user returns from settings
    fun checkPermissions(): Boolean {
        val camera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val audio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        return camera == PackageManager.PERMISSION_GRANTED &&
                audio == PackageManager.PERMISSION_GRANTED
    }

    // 🔄 1. Check permissions on resume (when returning from Settings)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (checkPermissions()) {
                    showPermissionDialog = false
                    onGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val camera = permissions[Manifest.permission.CAMERA] ?: false
        val audio = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (camera && audio) {
            showPermissionDialog = false
            onGranted()
        } else {
            showPermissionDialog = true
        }
    }

    // Ask permission once
    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {}, // cannot dismiss
            title = { Text("Permissions Required") },
            text = { Text("Please enable Camera and Microphone permissions to continue.") },
            confirmButton = {
                TextButton(onClick = {
                    // Open app settings
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            }
        )
    }
}

