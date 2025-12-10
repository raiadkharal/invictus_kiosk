package net.invictusmanagement.invictuskiosk.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.LocalUserInteractionReset
import net.invictusmanagement.invictuskiosk.presentation.components.NetworkStatusBar
import net.invictusmanagement.invictuskiosk.presentation.home.HomeViewModel
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.LoginScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.NavGraph
import net.invictusmanagement.invictuskiosk.presentation.screen_saver.ScreenSaver
import net.invictusmanagement.invictuskiosk.presentation.screen_saver.ScreenSaverViewModel
import net.invictusmanagement.invictuskiosk.ui.theme.InvictusKioskTheme
import net.invictusmanagement.invictuskiosk.usb.UsbPermissionReceiver
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import net.invictusmanagement.invictuskiosk.util.locale.AppLocaleManager
import net.invictusmanagement.invictuskiosk.util.locale.LocalAppLocale
import net.invictusmanagement.invictuskiosk.util.locale.LocaleHelper
import net.invictusmanagement.relaymanager.RelayManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var usbManager: UsbManager
    @Inject
    lateinit var globalLogger: GlobalLogger
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    private lateinit var relayManager : RelayManager
    private val TAG = "detectRelayOnStartup"

    private val usbPermissionReceiver = UsbPermissionReceiver { device ->
        lifecycleScope.launch {
            try {
                relayManager.initializeDevice(device.deviceName)
            } catch (e: Exception) {
                Log.d(TAG, "error: initializing device after permission granted: ${e.message}")
            }
        }
    }


    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        usbManager = getSystemService(USB_SERVICE) as UsbManager
        relayManager = RelayManager(this@MainActivity,globalLogger)

        AppLocaleManager.currentLocale.value =
            LocaleHelper.getCurrentLocale(this)

        enableEdgeToEdge()
        setContent {
            val viewModel = hiltViewModel<ScreenSaverViewModel>()
            val tokenState by viewModel.accessToken.collectAsState(initial = null)
            CompositionLocalProvider(
                LocalAppLocale provides AppLocaleManager.currentLocale.value
            ) {
                tokenState?.let { token ->
                    if (token.isNotEmpty()) {
                        MyApp()
                    } else {
                        MainContent()
                    }
                }
            }

        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Composable
    fun MyApp() {
        val screenSaverViewModel = hiltViewModel<ScreenSaverViewModel>()
        val isPaused by screenSaverViewModel.isPaused.collectAsState()

        val handler = remember { Handler(Looper.getMainLooper()) }
        val inactivityTimeout = 30_000L // 30 seconds
        var showScreenSaver by remember { mutableStateOf(false) }

        // Runnable to trigger screen saver after inactivity
        val showSaverRunnable = remember {
            Runnable {
                if(!isPaused) showScreenSaver = true
            }
        }

        fun resetTimer() {
            handler.removeCallbacks(showSaverRunnable)
            handler.postDelayed(showSaverRunnable, inactivityTimeout)
            showScreenSaver = false
        }

        // UI with pointer detection
        CompositionLocalProvider(LocalUserInteractionReset provides ::resetTimer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                                resetTimer()
                            }
                        }
                    }
                    .background(Color.Black)
            ) {
                // Main Content
                AnimatedVisibility(
                    visible = !showScreenSaver,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    MainContent()
                }

                // Screen Saver
                AnimatedVisibility(
                    visible = showScreenSaver,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    ScreenSaver()
                }
            }
        }

        LaunchedEffect(isPaused) {
            if (isPaused) {
                handler.removeCallbacks(showSaverRunnable)
                showScreenSaver = false
            } else {
                resetTimer()
            }
        }

        // Start the timer once on composition
        LaunchedEffect(Unit) {
            resetTimer()
        }
    }


    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Composable
    fun MainContent() {
        val viewModel = hiltViewModel<ScreenSaverViewModel>()
        val homeViewModel = hiltViewModel<HomeViewModel>()
        val tokenState by viewModel.accessToken.collectAsState(initial = null)
        val isConnected by homeViewModel.isConnected.filterNotNull().collectAsState(initial = true)
        var startDestination by remember { mutableStateOf<Any?>(null) }

        LaunchedEffect(tokenState) {
            tokenState?.let { token ->
                startDestination = if (token.isNotEmpty()) HomeScreen else LoginScreen
            }
        }

        InvictusKioskTheme {
            CompositionLocalProvider(LocalContext provides this@MainActivity) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold { innerPadding ->
                        startDestination?.let {
                            NavGraph(
                                innerPadding = innerPadding,
                                startDestination = it,
                            )
                        }
                    }
                    // Overlay the status bar at the very top (above toolbar)
                    NetworkStatusBar(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding(),
                        isConnected = isConnected
                    )
                }
            }
        }
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        networkMonitor.startMonitoring()

        super.onResume()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    usbPermissionReceiver,
                    IntentFilter(UsbPermissionReceiver.ACTION_USB_PERMISSION),
                    RECEIVER_NOT_EXPORTED
                )
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(
                    usbPermissionReceiver,
                    IntentFilter(UsbPermissionReceiver.ACTION_USB_PERMISSION)
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "onResume: error registering usb permission receiver: ${e.message}")
        }

    }

    override fun onStop() {
        networkMonitor.stopMonitoring()
        super.onStop()
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbPermissionReceiver)
    }

}