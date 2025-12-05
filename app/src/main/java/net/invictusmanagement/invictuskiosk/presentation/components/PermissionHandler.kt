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
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun CameraAndAudioPermission(
    onGranted: () -> Unit
) {
    val context = LocalContext.current
    var showRetryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    fun isPermissionPermanentlyDenied(permission: String): Boolean {
        return !ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity,
            permission
        ) && ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (cameraGranted && audioGranted) {
            onGranted()
        } else {
            val permanentlyDenied =
                isPermissionPermanentlyDenied(Manifest.permission.CAMERA) ||
                        isPermissionPermanentlyDenied(Manifest.permission.RECORD_AUDIO)

            if (permanentlyDenied) {
                showSettingsDialog = true
            } else {
                showRetryDialog = true
            }
        }
    }

    // Automatically launch permissions once
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    // ⛔ Non-cancelable Retry Dialog (first deny)
    if (showRetryDialog) {
        AlertDialog(
            onDismissRequest = {}, // non-cancelable
            title = { Text("Permissions Required") },
            text = { Text("Camera and Microphone permissions are required to continue.") },
            confirmButton = {
                TextButton(onClick = {
                    showRetryDialog = false
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        )
                    )
                }) {
                    Text("Grant Permissions")
                }
            }
        )
    }

    // ⚠ Settings Dialog (permanent deny)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = {}, // non-cancelable
            title = { Text("Permissions Permanently Denied") },
            text = { Text("Please enable Camera and Microphone permissions from Settings to continue.") },
            confirmButton = {
                TextButton(onClick = {
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

