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
import com.google.android.gms.maps.model.LatLng

@Composable
fun NavHostSetup(
    navController: NavHostController,
    signInScreenContent: @Composable (NavHostController) -> Unit,
    registerScreenContent: @Composable (NavHostController) -> Unit,
    mainScreen: @Composable (NavHostController) -> Unit,
    mapScreen: @Composable () -> Unit,
    recommendationScreen: @Composable () -> Unit,
    profileScreen: @Composable (NavHostController) -> Unit,
    searchFriendsScreen: @Composable (NavHostController) -> Unit,
    friendsListScreen: @Composable (NavHostController) -> Unit,
    animationScreen: @Composable (
        userLocation: LatLng,
        restaurantLatLng: LatLng,
    ) -> Unit,
) {
    Scaffold(
        bottomBar = {
            if (currentRoute(navController) in listOf(
                    Screen.Map.route,
                    Screen.Recommendations.route,
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
            composable(Screen.Recommendations.route) { recommendationScreen() }
            composable(Screen.Profile.route) { profileScreen(navController) }
            composable("friends_list") { friendsListScreen(navController) }
            composable("animation/{userLocation}/{restaurantLatLng}") { backStackEntry ->
                val userLocation = backStackEntry.arguments?.getString("userLocation")
                val restaurantLatLng = backStackEntry.arguments?.getString("restaurantLatLng")
                val latLng = userLocation?.split(",")?.let {
                    LatLng(it[0].toDouble(), it[1].toDouble())
                }
                val restaurant = restaurantLatLng?.split(",")?.let {
                    LatLng(it[0].toDouble(), it[1].toDouble())
                }
                animationScreen(latLng!!, restaurant!!)
            }
        }
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
