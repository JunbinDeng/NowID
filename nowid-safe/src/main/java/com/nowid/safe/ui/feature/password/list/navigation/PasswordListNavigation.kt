package com.nowid.safe.ui.feature.password.list.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.nowid.safe.ui.feature.password.add.navigation.AddPasswordRoute
import com.nowid.safe.ui.feature.password.detail.navigation.PasswordDetailRoute
import com.nowid.safe.ui.feature.password.list.PasswordListScreen
import kotlinx.serialization.Serializable

@Serializable
data object PasswordListRoute

fun NavController.navigateToAdd(navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = AddPasswordRoute) {
        navOptions()
    }
}

fun NavController.navigateToDetail(id: String, navOptions: NavOptionsBuilder.() -> Unit = {}) {
    navigate(route = PasswordDetailRoute(id)) {
        navOptions()
    }
}

fun NavGraphBuilder.passwordListScreen(onItemClick: (id: String) -> Unit, onAddClick: () -> Unit) {
    composable<PasswordListRoute> { entry ->
        PasswordListScreen(onItemClick = onItemClick, onAddClick = onAddClick)
    }
}