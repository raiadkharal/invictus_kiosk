package net.invictusmanagement.invictuskiosk.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.invictusmanagement.invictuskiosk.presentation.home.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.residents.ResidentsScreen
import net.invictusmanagement.invictuskiosk.presentation.service_key.ServiceKeyScreen

@Composable
fun NavGraph(
    startDestination: Any,
    innerPadding:PaddingValues
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable<HomeScreen> {
            HomeScreen(modifier = Modifier.padding(innerPadding),navController=navController)
        }
        composable<ResidentsScreen> {
            ResidentsScreen(modifier = Modifier.padding(innerPadding),navController = navController)
        }
        composable<ServiceKeyScreen> {
            ServiceKeyScreen(modifier = Modifier.padding(innerPadding),navController = navController)
        }
    }
}