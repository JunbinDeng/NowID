package com.nowid.safe.ui.feature.password.detail

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.nowid.safe.R
import com.nowid.safe.ui.component.NowIDAppBar
import com.nowid.safe.ui.icon.NowIDIcons
import com.nowid.safe.util.performBiometricEncryption
import timber.log.Timber

/**
 * Composable screen for displaying the details of a password entry.
 *
 * @param viewModel the [PasswordDetailViewModel] that loads and holds password data
 * @param onBackClick callback invoked when the back navigation icon is tapped
 */
@Composable
fun PasswordDetailScreen(
    viewModel: PasswordDetailViewModel = hiltViewModel(), onBackClick: () -> Unit
) {
    val activity =
        LocalActivity.current as? FragmentActivity ?: error("Must be hosted in an FragmentActivity")

    val title = viewModel.title.collectAsState().value
    val decrypted = viewModel.decrypted.collectAsState().value

    val authError = viewModel.authError.collectAsState().value

    var passwordVisible by remember { mutableStateOf(false) }

    val promptTitle = stringResource(R.string.biometric_prompt_decrypt_title)
    val promptSubtitle = stringResource(R.string.biometric_prompt_decrypt_subtitle)

    LaunchedEffect(viewModel.id) {
        val encrypted = viewModel.getEncryptedData()
        val iv = encrypted?.iv

        performBiometricEncryption(
            activity = activity,
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(promptTitle)
                .setSubtitle(promptSubtitle)
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build(),
            tryGetCrypto = {
                viewModel.tryGetCrypto(iv)
            },
            onSuccess = { crypto ->
                viewModel.loadPasswordWithCrypto(crypto)
            },
            onError = { e ->
                Timber.e(e)
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            },
        )
    }

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
                text = title ?: "", style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = if (passwordVisible) decrypted ?: authError
                ?: "Error decrypting" else "••••••••",
                isError = passwordVisible && authError != null,
                onValueChange = {},
                readOnly = true,
                label = { Text("Password") },
                trailingIcon = {
                    IconButton(onClick = {
                        passwordVisible = !passwordVisible
                    }) {
                        Icon(
                            imageVector = if (passwordVisible) NowIDIcons.Visibility else NowIDIcons.VisibilityOff,
                            contentDescription = "Password Visibility"
                        )
                    }
                },
            )
        }
    }
}