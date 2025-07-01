package net.invictusmanagement.invictuskiosk.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.presentation.home.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.NavGraph
import net.invictusmanagement.invictuskiosk.presentation.navigation.ResidentsScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.ServiceKeyScreen
import net.invictusmanagement.invictuskiosk.presentation.residents.ResidentsScreen
import net.invictusmanagement.invictuskiosk.ui.theme.InvictusKioskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InvictusKioskTheme {
                Scaffold { innerPadding->
                    NavGraph(innerPadding = innerPadding, startDestination = HomeScreen)
                }
            }
        }
    }
}