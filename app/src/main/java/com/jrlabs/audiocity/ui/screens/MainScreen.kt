package com.jrlabs.audiocity.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
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
    routeViewModel: RouteViewModel
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(1) } // Start on Routes tab

    val items = listOf(
        BottomNavItem(
            title = stringResource(R.string.tab_explore),
            selectedIcon = Icons.Filled.Explore,
            unselectedIcon = Icons.Outlined.Explore
        ),
        BottomNavItem(
            title = stringResource(R.string.tab_routes),
            selectedIcon = Icons.Filled.Route,
            unselectedIcon = Icons.Outlined.Route
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
            0 -> ExploreScreen(
                modifier = Modifier.padding(paddingValues)
            )
            1 -> RoutesListScreen(
                modifier = Modifier.padding(paddingValues),
                onRouteSelected = onRouteSelected,
                viewModel = routeViewModel
            )
            2 -> ProfileScreen(
                modifier = Modifier.padding(paddingValues),
                viewModel = routeViewModel
            )
        }
    }
}
