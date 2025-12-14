package com.jrlabs.audiocity.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.jrlabs.audiocity.R
import com.jrlabs.audiocity.ui.viewmodel.RouteViewModel
import com.jrlabs.audiocity.ui.screens.explore.ExploreScreen
import com.jrlabs.audiocity.ui.screens.history.HistoryScreen
import com.jrlabs.audiocity.ui.screens.myroutes.MyRoutesScreen
import com.jrlabs.audiocity.ui.screens.profile.ProfileScreen
import com.jrlabs.audiocity.ui.screens.routes.RoutesListScreen

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(
    onRouteSelected: (String) -> Unit,
    onTripSelected: (String) -> Unit = {},
    onPlanTripClick: () -> Unit = {},
    onAllTripsClick: () -> Unit = {},
    onAllRoutesClick: () -> Unit = {},
    onCreateRoute: () -> Unit = {},
    onEditRoute: (String) -> Unit = {},
    routeViewModel: RouteViewModel
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) } // Start on Routes tab

    val items = listOf(
        BottomNavItem(
            title = stringResource(R.string.tab_routes),
            selectedIcon = Icons.Filled.Headphones,
            unselectedIcon = Icons.Outlined.Headphones
        ),
        BottomNavItem(
            title = stringResource(R.string.tab_explore),
            selectedIcon = Icons.Filled.Explore,
            unselectedIcon = Icons.Outlined.Explore
        ),
        BottomNavItem(
            title = stringResource(R.string.tab_create),
            selectedIcon = Icons.Filled.AddCircle,
            unselectedIcon = Icons.Outlined.AddCircle
        ),
        BottomNavItem(
            title = stringResource(R.string.tab_history),
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History
        ),
        BottomNavItem(
            title = stringResource(R.string.tab_profile),
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTabIndex == index) {
                                    item.selectedIcon
                                } else {
                                    item.unselectedIcon
                                },
                                contentDescription = item.title
                            )
                        },
                        label = { Text(text = item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTabIndex) {
            0 -> RoutesListScreen(
                modifier = Modifier.padding(paddingValues),
                onRouteSelected = onRouteSelected,
                onTripSelected = onTripSelected,
                onPlanTripClick = onPlanTripClick,
                onAllTripsClick = onAllTripsClick,
                onAllRoutesClick = onAllRoutesClick,
                viewModel = routeViewModel
            )
            1 -> ExploreScreen(
                modifier = Modifier.padding(paddingValues),
                routeViewModel = routeViewModel
            )
            2 -> MyRoutesScreen(
                modifier = Modifier.padding(paddingValues),
                onCreateRoute = onCreateRoute,
                onRouteSelected = { route -> onEditRoute(route.id) }
            )
            3 -> HistoryScreen(
                modifier = Modifier.padding(paddingValues)
            )
            4 -> ProfileScreen(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
