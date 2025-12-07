package com.jrlabs.audiocity.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.jrlabs.audiocity.ui.screens.routes.AllRoutesScreen
import com.jrlabs.audiocity.ui.screens.trips.AllTripsScreen
import com.jrlabs.audiocity.ui.screens.trips.TripDetailScreen
import com.jrlabs.audiocity.ui.screens.trips.TripOnboardingScreen
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel
import com.jrlabs.audiocity.ui.viewmodel.TripViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object RouteDetail : Screen("route_detail/{routeId}") {
        fun createRoute(routeId: String) = "route_detail/$routeId"
    }
    object ActiveRoute : Screen("active_route")
    object AllRoutes : Screen("all_routes")
    object AllTrips : Screen("all_trips")
    object TripOnboarding : Screen("trip_onboarding")
    object TripDetail : Screen("trip_detail/{tripId}") {
        fun createRoute(tripId: String) = "trip_detail/$tripId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    // Shared ViewModels across all screens
    val routeViewModel: RouteViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()

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
                onTripSelected = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                },
                onPlanTripClick = {
                    navController.navigate(Screen.TripOnboarding.route)
                },
                onAllTripsClick = {
                    navController.navigate(Screen.AllTrips.route)
                },
                onAllRoutesClick = {
                    navController.navigate(Screen.AllRoutes.route)
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

        composable(Screen.AllRoutes.route) {
            AllRoutesScreen(
                onRouteSelected = { routeId ->
                    navController.navigate(Screen.RouteDetail.createRoute(routeId))
                },
                onBackClick = { navController.popBackStack() },
                viewModel = routeViewModel
            )
        }

        composable(Screen.AllTrips.route) {
            val trips by tripViewModel.trips.collectAsState()

            AllTripsScreen(
                trips = trips,
                onBackClick = { navController.popBackStack() },
                onTripClick = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                },
                onPlanTripClick = {
                    navController.navigate(Screen.TripOnboarding.route)
                }
            )
        }

        composable(Screen.TripOnboarding.route) {
            val destinations by tripViewModel.availableDestinations.collectAsState()
            val routes by routeViewModel.availableRoutes.collectAsState()

            TripOnboardingScreen(
                availableDestinations = destinations,
                availableRoutes = routes,
                onDismiss = { navController.popBackStack() },
                onTripCreated = { city, routeIds, startDate, endDate, downloadOffline ->
                    tripViewModel.createTrip(city, routeIds, startDate, endDate, downloadOffline)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            val trip = tripViewModel.getTrip(tripId)
            val routes by routeViewModel.availableRoutes.collectAsState()

            trip?.let {
                TripDetailScreen(
                    trip = it,
                    routes = routes,
                    onBackClick = { navController.popBackStack() },
                    onStartRoute = { routeId ->
                        // Seleccionar la ruta y navegar al detalle
                        val selectedRoute = routes.find { r -> r.id == routeId }
                        selectedRoute?.let { r ->
                            routeViewModel.selectRoute(r)
                            navController.navigate(Screen.RouteDetail.createRoute(routeId))
                        }
                    },
                    onDeleteTrip = {
                        tripViewModel.deleteTrip(tripId)
                        navController.popBackStack()
                    },
                    onAddRoutes = {
                        // Navegar a selección de rutas (podría ser AllRoutes con filtro)
                        navController.navigate(Screen.AllRoutes.route)
                    }
                )
            } ?: run {
                // Trip no encontrado, volver
                navController.popBackStack()
            }
        }
    }
}
