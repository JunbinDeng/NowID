package com.nowid.safe.ui.feature.password.list

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.nowid.safe.ui.icon.NowIDIcons
import timber.log.Timber

/**
 * Composable screen for displaying a list of saved password titles.
 *
 * Each item requires biometric authentication before invoking the click callback.
 *
 * @param viewModel the [PasswordListViewModel] providing password items and authentication logic
 * @param onItemClick callback invoked with the password ID when an item is successfully authenticated
 * @param onAddClick callback invoked when the Add FloatingActionButton is tapped
 */
@Composable
fun PasswordListScreen(
    viewModel: PasswordListViewModel = hiltViewModel(),
    onItemClick: (id: String) -> Unit,
    onAddClick: () -> Unit
) {
    val activity =
        LocalActivity.current as? FragmentActivity ?: error("Must be hosted in an FragmentActivity")

    val items by viewModel.items.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(NowIDIcons.Add, contentDescription = "Add Password")
            }
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No passwords yet\nTap the + button to create one",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                viewModel.authenticateToViewPassword(activity, item.id) { result ->
                                    if (result.isSuccess) {
                                        onItemClick(item.id)
                                    } else {
                                        Toast.makeText(
                                            activity,
                                            result.exceptionOrNull()?.message,
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        Timber.e(result.exceptionOrNull())
                                    }
                                }
                            }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}