package net.invictusmanagement.invictuskiosk.presentation

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.LoginScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.NavGraph
import net.invictusmanagement.invictuskiosk.presentation.screen_saver.ScreenSaver
import net.invictusmanagement.invictuskiosk.presentation.screen_saver.ScreenSaverViewModel
import net.invictusmanagement.invictuskiosk.ui.theme.InvictusKioskTheme
import net.invictusmanagement.invictuskiosk.util.locale.AppLocale
import net.invictusmanagement.invictuskiosk.util.locale.LocaleHelper
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Composable
    fun MyApp() {
        val handler = remember { Handler(Looper.getMainLooper()) }
        val inactivityTimeout = 30_000L // 30 seconds
        var showScreenSaver by remember { mutableStateOf(false) }

        // Runnable to trigger screen saver after inactivity
        val showSaverRunnable = remember {
            Runnable {
                showScreenSaver = true
            }
        }

        fun resetTimer() {
            handler.removeCallbacks(showSaverRunnable)
            handler.postDelayed(showSaverRunnable, inactivityTimeout)
            showScreenSaver = false
        }

        // UI with pointer detection
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

        // Start the timer once on composition
        LaunchedEffect(Unit) {
            resetTimer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Composable
    fun MainContent() {
        val viewModel = hiltViewModel<ScreenSaverViewModel>()
        val tokenState by viewModel.accessToken.collectAsState(initial = null)
        var startDestination by remember { mutableStateOf<Any?>(null) }

        LaunchedEffect(tokenState) {
            tokenState?.let { token ->
                startDestination = if (token.isNotEmpty()) {
                    HomeScreen
                } else {
                    LoginScreen
                }
            }
        }

        InvictusKioskTheme {
            CompositionLocalProvider(
                LocalContext provides this@MainActivity
            ) {
                Scaffold{ innerPadding ->
                    startDestination?.let {
                        NavGraph(
                            innerPadding = innerPadding,
                            startDestination = it,
                        )
                    }
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

}