package com.example.mappi.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mappi.presentation.ui.main.composables.BottomNavigationBar
import com.example.mappi.presentation.ui.main.composables.Screen

@Composable
fun NavHostSetup(
    navController: NavHostController,
    signInScreenContent: @Composable (NavHostController) -> Unit,
    registerScreenContent: @Composable (NavHostController) -> Unit,
    mainScreen: @Composable (NavHostController) -> Unit,
    mapScreen: @Composable () -> Unit,
    chatScreen: @Composable () -> Unit,
    profileScreen: @Composable (NavHostController) -> Unit,
    searchFriendsScreen: @Composable (NavHostController) -> Unit,
    friendsListScreen: @Composable (NavHostController) -> Unit
) {
    Scaffold(
        bottomBar = {
            if (currentRoute(navController) in listOf(
                    Screen.Map.route,
                    Screen.Chat.route,
                    Screen.Profile.route
                )
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "sign_in",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("sign_in") { signInScreenContent(navController) }
            composable("register") { registerScreenContent(navController) }
            composable("main") { mainScreen(navController) }
            composable("search_friends") { searchFriendsScreen(navController) }
            composable(Screen.Map.route) { mapScreen() }
            composable(Screen.Chat.route) { chatScreen() }
            composable(Screen.Profile.route) { profileScreen(navController) }
            composable("friends_list") { friendsListScreen(navController) }
        }
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
