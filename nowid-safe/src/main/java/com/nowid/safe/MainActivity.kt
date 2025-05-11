package com.nowid.safe

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nowid.safe.ui.component.NowIDAppBar
import com.nowid.safe.ui.icon.NowIDIcons
import com.nowid.safe.ui.theme.NowIDTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

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
        PasswordListScreen(onItemClick, onAddClick)
    }
}

@Composable
fun PasswordListScreen(onItemClick: (id: String) -> Unit, onAddClick: () -> Unit) {
    val mockItems = listOf(
        "github",
        "google",
        "mattr",
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(NowIDIcons.Add, contentDescription = "Add Password")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            itemsIndexed(mockItems, key = { _, item -> item }) { index, item ->
                ListItem(
                    headlineContent = { Text(item) },
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onItemClick(item) })
                HorizontalDivider()
            }
        }
    }
}

@Serializable
data class PasswordDetailRoute(val id: String)

fun NavGraphBuilder.passwordDetailScreen(navController: NavController) {
    composable<PasswordDetailRoute> { entry ->
        PasswordDetailScreen(
            onBackClick = { navController.popBackStack() })
    }
}

@Composable
fun PasswordDetailScreen(
    onBackClick: () -> Unit
) {
    val title = "GitHub"
    val password = "ghp_xxxxxx"
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NowIDAppBar(
                onNavigationClick = onBackClick, navigationIcon = NowIDIcons.ArrowBack
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = title, style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = if (passwordVisible) password else "••••••••",
                onValueChange = {},
                readOnly = true,
                label = { Text("Password") },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Password Visibility"
                        )
                    }
                })
        }
    }
}

@Serializable
data object AddPasswordRoute

fun NavGraphBuilder.addPasswordScreen(navController: NavController) {
    composable<AddPasswordRoute> { entry ->
        AddPasswordScreen(
            onBackClick = { navController.popBackStack() })
    }
}

@Composable
fun AddPasswordScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var titleText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            NowIDAppBar(
                onNavigationClick = onBackClick,
                navigationIcon = NowIDIcons.ArrowBack,
                actionIcon = NowIDIcons.Save,
                onActionClick = {
                    Toast.makeText(context, "Password saved", Toast.LENGTH_SHORT).show()
                    onBackClick()
                }
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Title") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }
    }
}