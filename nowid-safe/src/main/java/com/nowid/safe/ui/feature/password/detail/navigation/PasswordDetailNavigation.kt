package com.nowid.safe.ui.feature.password.detail.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.nowid.safe.ui.feature.password.detail.PasswordDetailScreen
import com.nowid.safe.ui.feature.password.detail.PasswordDetailViewModel
import kotlinx.serialization.Serializable

@Serializable
data class PasswordDetailRoute(val id: String)

fun NavGraphBuilder.passwordDetailScreen(navController: NavController) {
    composable<PasswordDetailRoute> { entry ->
        val id = entry.toRoute<PasswordDetailRoute>().id
        PasswordDetailScreen(
            viewModel = hiltViewModel<PasswordDetailViewModel, PasswordDetailViewModel.Factory>(
                // Use the password ID as the ViewModel key to scope the ViewModel instance
                // to this specific password detail screen, ensuring each ID gets its own state.
                key = id
            ) { factory ->
                factory.create(id)
            },
            onBackClick = { navController.popBackStack() })
    }
}