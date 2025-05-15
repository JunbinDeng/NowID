package com.nowid.safe.ui.feature.password.list

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.nowid.safe.R
import com.nowid.safe.ui.icon.NowIDIcons
import com.nowid.safe.util.performBiometricEncryption
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.crypto.AEADBadTagException

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

    val coroutineScope = rememberCoroutineScope()

    val promptTitle = stringResource(R.string.biometric_prompt_decrypt_title)
    val promptSubtitle = stringResource(R.string.biometric_prompt_decrypt_subtitle)

    fun handlePasswordItemClick(id: String) {
        coroutineScope.launch {
            val encrypted = viewModel.getEncryptedData(id)
            val iv = encrypted?.iv

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(promptTitle)
                .setSubtitle(promptSubtitle)
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()

            performBiometricEncryption(
                activity = activity,
                promptInfo = promptInfo,
                tryGetCrypto = { viewModel.tryGetCrypto(iv) },
                onSuccess = { crypto ->
                    viewModel.loadPasswordWithCrypto(id, crypto) { result ->
                        if (result.isSuccess) {
                            onItemClick(id)
                        } else {
                            val exception = result.exceptionOrNull()
                            Timber.e(exception)

                            if (exception is AEADBadTagException) {
                                Toast.makeText(
                                    activity,
                                    "Biometric authentication failed. Your device credentials may have changed.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                Toast.makeText(
                                    activity,
                                    exception?.message ?: "Load password failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                onError = { e ->
                    Timber.e(e)

                    if (e is KeyPermanentlyInvalidatedException) {
                        Toast.makeText(
                            activity,
                            "Biometric settings have changed. Please reauthenticate to continue.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        Toast.makeText(
                            activity,
                            e.message ?: "Biometric authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
            )
        }
    }

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
                    text = "Tap the + button to create a password.\n\n" +
                            "Note: Changing biometric settings may clear your stored passwords.",
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
                                handlePasswordItemClick(item.id)
                            }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}