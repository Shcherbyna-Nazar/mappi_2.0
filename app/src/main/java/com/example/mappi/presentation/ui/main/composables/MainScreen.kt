package com.example.mappi.presentation.ui.main.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(
    navController: NavHostController,
    mapScreen: @Composable () -> Unit,
    decisionScreen: @Composable () -> Unit,
    profileScreen: @Composable (NavHostController) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        val mainNavController = rememberNavController()
        NavHost(
            navController = mainNavController,
            startDestination = Screen.Profile.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Map.route) { mapScreen() }
            composable(Screen.Recommendations.route) { decisionScreen() }
            composable(Screen.Profile.route) { profileScreen(navController) }
        }
    }
}
