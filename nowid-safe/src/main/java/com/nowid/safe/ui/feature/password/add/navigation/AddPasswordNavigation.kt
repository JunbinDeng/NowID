package com.nowid.safe.ui.feature.password.add.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.nowid.safe.ui.feature.password.add.AddPasswordScreen
import kotlinx.serialization.Serializable

@Serializable
data object AddPasswordRoute

fun NavGraphBuilder.addPasswordScreen(navController: NavController) {
    composable<AddPasswordRoute> { entry ->
        AddPasswordScreen(onBackClick = { navController.popBackStack() })
    }
}