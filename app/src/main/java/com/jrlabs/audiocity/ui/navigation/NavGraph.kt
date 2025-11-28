package com.jrlabs.audiocity.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jrlabs.audiocity.ui.screens.MainScreen
import com.jrlabs.audiocity.ui.screens.RouteDetailScreen
import com.jrlabs.audiocity.ui.screens.ActiveRouteScreen
import com.jrlabs.audiocity.ui.screens.SplashScreen
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object RouteDetail : Screen("route_detail/{routeId}") {
        fun createRoute(routeId: String) = "route_detail/$routeId"
    }
    object ActiveRoute : Screen("active_route")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    // Shared ViewModel across all screens
    val routeViewModel: RouteViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onRouteSelected = { routeId ->
                    navController.navigate(Screen.RouteDetail.createRoute(routeId))
                },
                routeViewModel = routeViewModel
            )
        }

        composable(
            route = Screen.RouteDetail.route,
            arguments = listOf(
                navArgument("routeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            RouteDetailScreen(
                routeId = routeId,
                onBack = { navController.popBackStack() },
                onStartRoute = {
                    navController.navigate(Screen.ActiveRoute.route)
                },
                viewModel = routeViewModel
            )
        }

        composable(Screen.ActiveRoute.route) {
            ActiveRouteScreen(
                onRouteEnded = {
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                },
                viewModel = routeViewModel
            )
        }
    }
}
