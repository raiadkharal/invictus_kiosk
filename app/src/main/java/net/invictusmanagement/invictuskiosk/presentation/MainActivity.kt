package net.invictusmanagement.invictuskiosk.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.res.Configuration
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
import net.invictusmanagement.invictuskiosk.util.locale.AppLocale
import net.invictusmanagement.invictuskiosk.util.locale.LocaleHelper
import net.invictusmanagement.relaymanager.RelayManager
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor


    private lateinit var usbManager: UsbManager
    @Inject
    lateinit var globalLogger: GlobalLogger
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

//        detectRelayOnStartup()

        //Start network monitoring once here
        networkMonitor.startMonitoring()

        enableEdgeToEdge()
        setContent {
            val viewModel = hiltViewModel<ScreenSaverViewModel>()
            val tokenState by viewModel.accessToken.collectAsState(initial = null)
            tokenState?.let { token ->
                if (token.isNotEmpty()) {
                    MyApp()
                } else {
                    MainContent()
                }
            }

        }
    }

//    private fun detectRelayOnStartup() {
//        for (device in usbManager.deviceList.values) {
//            if (device.vendorId == 0x2A19) { // Numato Vendor ID (adjust if different)
//                if (!usbManager.hasPermission(device)) {
//                    val permissionIntent = PendingIntent.getBroadcast(
//                        this,
//                        0,
//                        Intent(UsbPermissionReceiver.ACTION_USB_PERMISSION),
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
//                    usbManager.requestPermission(device, permissionIntent)
//                } else {
//                    // Permission already granted — directly initialize
//                    lifecycleScope.launch {
//                        NumatoRelayManager.getInstance(this@MainActivity)
//                            .initializeDevice(device)
//                    }
//                }
//            }
//        }
//    }

//    private fun detectRelayOnStartup() {
//        lifecycleScope.launch {
//            try {
//                val delayMillis = 1000
//                val relayManager = NumatoRelayManager.getInstance(this@MainActivity)
//                val devices = relayManager.getDevices()
//
//                if (devices.isEmpty()) {
//                    Log.w(TAG, "WARNING: No relay devices detected.")
//                    return@launch
//                }
//
//                val targetDevice = devices.first()
//                Log.i(TAG, "Initializing relay device: ${targetDevice.name}")
//
//                relayManager.initializeDevice(targetDevice)
//
//                // Step 1: Open/close each relay one by one
//                for (i in 1..relayManager.relayCount) {
//                    Log.i(TAG, "Opening relay $i.")
//                    relayManager.openRelays(listOf(i))
//
//                    if (relayManager.isRelayOpen(i)) {
//                        Log.i(TAG, "Delaying for $delayMillis milliseconds.")
//                        delay(delayMillis.toLong())
//                        Log.i(TAG, "Closing relay $i.")
//                        relayManager.closeRelays(listOf(i))
//                    }
//                }
//
//                // Step 2: Open/close relay 1 & 3
//                Log.i(TAG, "Opening relay 1 & 3.")
//                relayManager.openRelays(listOf(1, 3))
//                if (relayManager.isRelayOpen(1) && relayManager.isRelayOpen(3)) {
//                    delay(delayMillis.toLong())
//                    Log.i(TAG, "Closing relay 1 & 3.")
//                    relayManager.closeRelays(listOf(1, 3))
//                }
//
//                // Step 3: Open/close relay 2 & 4
//                Log.i(TAG, "Opening relay 2 & 4.")
//                relayManager.openRelays(listOf(2, 4))
//                if (relayManager.isRelayOpen(2) && relayManager.isRelayOpen(4)) {
//                    delay(delayMillis.toLong())
//                    Log.i(TAG, "Closing relay 2 & 4.")
//                    relayManager.closeRelays(listOf(2, 4))
//                }
//
//                // Step 4: Open/close all relays
//                Log.i(TAG, "Opening all relays.")
//                relayManager.openAllRelays()
//                delay(delayMillis.toLong())
//                Log.i(TAG, "Closing all relays.")
//                relayManager.closeAllRelays()
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error while detecting or testing relay: ${e.message}", e)
//            }
//        }
//    }



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
        val isConnected by homeViewModel.isConnected.collectAsState(initial = true)
        val isInternetStable by homeViewModel.isInternetStable.collectAsState()
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
                        isConnected = isConnected,
                        isInternetStable = isInternetStable
                    )
                }
            }
        }
    }



    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocaleContext(newBase))
    }

    private fun updateLocaleContext(context: Context): Context {
        val locale = LocaleHelper.getCurrentLocale(context)
        AppLocale.updateLocale(locale)
        return contextWithUpdatedLocale(context, locale)
    }

    private fun contextWithUpdatedLocale(context: Context, locale: Locale): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
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

    override fun onDestroy() {
        super.onDestroy()
//        relayManager.disconnect()
        networkMonitor.stopMonitoring()
        unregisterReceiver(usbPermissionReceiver)
    }

}