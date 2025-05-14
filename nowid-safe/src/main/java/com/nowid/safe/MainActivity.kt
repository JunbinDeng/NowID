package com.nowid.safe

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.nowid.safe.ui.feature.password.add.navigation.addPasswordScreen
import com.nowid.safe.ui.feature.password.detail.navigation.passwordDetailScreen
import com.nowid.safe.ui.feature.password.list.navigation.PasswordListRoute
import com.nowid.safe.ui.feature.password.list.navigation.navigateToAdd
import com.nowid.safe.ui.feature.password.list.navigation.navigateToDetail
import com.nowid.safe.ui.feature.password.list.navigation.passwordListScreen
import com.nowid.safe.ui.theme.NowIDTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NowIDTheme {
                NowIDSafeApp()
            }
        }
    }
}

@Composable
fun NowIDSafeApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PasswordListRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            passwordListScreen(
                onItemClick = navController::navigateToDetail,
                onAddClick = navController::navigateToAdd
            )
            passwordDetailScreen(navController)
            addPasswordScreen(navController)
        }
    }
}