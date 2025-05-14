package com.nowid.safe.ui.feature.password.add

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.nowid.safe.R
import com.nowid.safe.ui.component.NowIDAppBar
import com.nowid.safe.ui.icon.NowIDIcons
import com.nowid.safe.util.performBiometricEncryption
import timber.log.Timber

/**
 * Maximum allowed length for title and password input fields.
 */
private const val MAX_INPUT_LENGTH = 50

/**
 * Composable screen for adding a new password entry.
 *
 * @param viewModel the [AddPasswordViewModel] handling state and add operations
 * @param onBackClick callback invoked when the user navigates back or after saving
 */
@Composable
fun AddPasswordScreen(
    viewModel: AddPasswordViewModel = hiltViewModel(), onBackClick: () -> Unit
) {
    val activity =
        LocalActivity.current as? FragmentActivity ?: error("Must be hosted in an FragmentActivity")

    var titleText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    val titleValid = titleText.trim().length <= MAX_INPUT_LENGTH
    val passwordValid = passwordText.length <= MAX_INPUT_LENGTH

    var passwordVisible by remember { mutableStateOf(false) }

    val promptTitle = stringResource(R.string.biometric_prompt_encrypt_title)
    val promptSubtitle = stringResource(R.string.biometric_prompt_encrypt_subtitle)

    Scaffold(
        topBar = {
            NowIDAppBar(
                onNavigationClick = onBackClick,
                navigationIcon = NowIDIcons.ArrowBack,
                actionIcon = NowIDIcons.Save,
                onActionClick = {
                    if (titleText.isBlank() || passwordText.isBlank()) {
                        Toast.makeText(
                            activity, "Please enter both title and password", Toast.LENGTH_SHORT
                        ).show()
                        return@NowIDAppBar
                    }

                    if (!titleValid || !passwordValid) {
                        Toast.makeText(
                            activity,
                            "Title and password must be ≤${MAX_INPUT_LENGTH} characters",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@NowIDAppBar
                    }

                    performBiometricEncryption(
                        activity = activity,
                        promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle(promptTitle)
                            .setSubtitle(promptSubtitle)
                            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                            .build(),
                        tryGetCrypto = { viewModel.tryGetCrypto() },
                        onSuccess = {
                            viewModel.addPassword(
                                titleText, passwordText
                            ) { result ->
                                if (result.isSuccess) {
                                    Toast.makeText(
                                        activity, "Password saved", Toast.LENGTH_SHORT
                                    ).show()
                                    onBackClick()
                                    return@addPassword
                                } else {
                                    Timber.e(result.exceptionOrNull())
                                    Toast.makeText(
                                        activity,
                                        result.exceptionOrNull()?.message ?: "Password save failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        onError = { msg ->
                            Timber.e(msg)
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                        },
                    )
                })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = titleText,
                onValueChange = { new ->
                    // Allow only ASCII letters, digits, and punctuation; disallow other non-ASCII
                    titleText = new.filter { char ->
                        char in ' '..'~'
                    }
                },
                isError = !titleValid,
                label = { Text("Title") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = passwordText,
                onValueChange = { new ->
                    // Allow only ASCII letters, digits, and punctuation; disallow other non-ASCII
                    passwordText = new.filter { char ->
                        char in ' '..'~'
                    }
                },
                isError = !passwordValid,
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = {
                        passwordVisible = !passwordVisible
                    }) {
                        Icon(
                            imageVector = if (passwordVisible) NowIDIcons.Visibility else NowIDIcons.VisibilityOff,
                            contentDescription = "Password Visibility"
                        )
                    }
                }
            )
        }
    }
}
