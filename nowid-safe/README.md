# NowID Safe App

A secure password manager Android application built with Jetpack Compose, Android Keystore, and
Biometric Authentication. This project demonstrates best practices in Android application
development.

## Features

- Declarative UI with Jetpack Compose
- List of saved passwords with a fixed + Add Password entry
- Biometric authentication flow (Fingerprint/Face) via `androidx.biometric`
- AES-GCM encryption/decryption using Android Keystore
- Session-based unlock: after successful decryption, a grace period avoids repeated biometric
  prompts
- Hilt for dependency injection (Repository, Dispatchers)
- KDoc-formatted documentation with structured tags (`@param`, `@return`, `@throws`)
- Clear exception handling and entry-specific recovery

## Requirements Checklist

- [x] Secure password encryption with AES-GCM via Android Keystore
- [x] Generate and store AES key exclusively in Android Keystore (no plaintext keys)
- [x] Encrypt each password before saving to app storage
- [x] Decrypt and display passwords only after successful biometric authentication
- [x] Graceful fallback to PIN/password if biometrics unavailable
- [x] Compose-based main screen listing password titles (clickable)
- [x] Compose-based “Add Password” screen with input form and Save action
- [x] Full source code available in GitHub repository
- [x] README with run instructions and design notes

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **DI**: Hilt
- **Crypto**: Android Keystore (AES-GCM)
- **Biometric**: `androidx.biometric:biometric`
- **Coroutines**: `kotlinx.coroutines`
- **Logging**: Timber

## Technical Comparisons

### Data Storage

- **Android Keystore (selected)**: Secure key management; keys never leave hardware-backed storage;
  used to protect the AES encryption key.
- **Proto DataStore (selected)**: Schema-based storage using Protocol Buffers; type-safe and
  forward-compatible; configured to store initialization vectors (IVs) and encrypted payloads.
- **SharedPreferences (alternative)**: Simple synchronous key–value storage; blocking I/O and lacks
  encryption by default.
- **Preferences DataStore (alternative)**: Asynchronous key–value API with coroutines and Flow;
  type-unsafe but supports migration.
- **Room (alternative)**: Relational database ORM; powerful querying and structured data support,
  but heavier setup and boilerplate.

### Encryption

- **AES-GCM (selected)**: Symmetric authenticated encryption providing confidentiality and integrity
  with minimal configuration.
- **AES-CBC (alternative)**: Symmetric encryption requiring manual HMAC for integrity; more
  susceptible to padding oracle attacks.
- **RSA-ECB (alternative)**: Asymmetric deterministic encryption; suitable for small payloads (e.g.,
  encrypting keys) but leaks data patterns and lacks built-in integrity checks.

### Biometric API

- **androidx.biometric:biometric (selected)**: Uniform support back to API 23, simplified
  CryptoObject integration, and built-in flows for enrollment updates.
- **Platform BiometricPrompt API (alternative)**: Latest platform features but inconsistent
  behaviors across OEMs and varied API level support.
- **FingerprintManager (deprecated)**: Legacy fingerprint-only API; limited to fingerprint
  authentication, lacks unified support for other modalities, and has been superseded by
  BiometricPrompt.

### UI Framework

- **Jetpack Compose (selected)**: Declarative, concise, and easier state management; integrates
  seamlessly with Kotlin and offers live previews.
- **XML Layouts (alternative)**: Mature and verbose; requires boilerplate and is less suited to
  dynamic UI updates.

## Setup

### Download Release

1. Go to the [Releases page](https://github.com/JunbinDeng/NowID/releases).
    - **Download** the latest `nowid-safe-release.apk` asset directly from the Assets section.
    - **Scan** the QR code displayed on the release page to download and install the APK.

### From Source Prerequisites

- Android Studio Bumblebee or later
- JDK 11+
- Android device/emulator API Level **23+**

### Clone and Build

```bash
git clone https://github.com/JunbinDeng/NowID.git
cd NowID/nowid-safe
./gradlew clean assembleDebug
```

### Run

1. Open the `nowid-safe` module in Android Studio
2. Sync Gradle and build the project
3. Run on a connected device or emulator
4. Grant biometric and keystore permissions when prompted

## Usage

1. **Add Password**: Tap the **+ Add Password** entry
    - Enter a title and password (letters, digits, special symbols; limits enforced)
    - Authenticate via biometrics to encrypt and store
2. **View Password**: Tap an existing item
    - If within the unlock grace period, skip biometric prompt; otherwise, authenticate
    - View the password

## Architecture

- **MVVM** with `PasswordRepository` handling encryption, decryption, and storage
- **DispatcherModule** provides `@IoDispatcher` and `@DefaultDispatcher` for testable coroutine
  contexts
- **BiometricEncryptUseCase** encapsulates `Cipher` initialization with AES/GCM/NoPadding
- **AssistedInject** for `PasswordDetailViewModel` enables passing dynamic `id` parameters
- **KDoc** used across classes and methods for clear code documentation

## Challenges & Solutions

- **AEADBadTagException Handling**
    - *Challenge*: Authentication tag mismatches during decryption (e.g., due to tampered or stale
      IV).
    - *Solution*: Delete only the corrupted entry by ID, preserving other data.
- **KeyPermanentlyInvalidatedException Handling**
    - *Challenge*: Keystore keys become invalid after biometric changes (e.g., fingerprint
      re-enrollment).
    - *Solution*: Recreate the encryption key and delete only the affected entry by ID.
- **BiometricPrompt.CryptoObject Compatibility**
    - *Challenge*: Platform API varied across Android versions.
    - *Solution*: Adopted `androidx.biometric:biometric` to unify behavior back to API 23.
- **Hilt Injection Errors**
    - *Challenge*: Encountered `NoSuchMethodError` for generated components.
    - *Solution*: Aligned Hilt Gradle plugin and `kapt` versions, cleaned build caches.
- **Title/Password Input Constraints**
    - *Challenge*: Restrict valid characters and length without confusing users.
    - *Solution*: Applied input filters and `VisualTransformation` on `OutlinedTextField`.
- **Compose Theming & Layout**
    - *Challenge*: Achieving consistent padding and transparency.
    - *Solution*: Utilized `innerPadding` from `Scaffold` and `MaterialTheme` color schemes.
- **Material3 TopAppBar Experimental**
    - *Challenge*: The Material3 `TopAppBar` API is still marked experimental.
    - *Solution*: Fallback to Material2's `TopAppBar` and extracted into a custom `NowIDAppBar`
      composable for easier future migration.

## Future Improvements

- Add unit and instrumented tests with CI pipeline (lint, static analysis, test runs)
- Cloud backup/restore of encrypted vaults
- UI animations and smoother state transitions
- Dark mode support and dynamic theming
- Edit or delete existing passwords
- One-tap copy password to clipboard
- Accessibility support (TalkBack, large text)
- Comprehensive error analytics and crash reporting

## License

MIT License. See [LICENSE](../LICENSE) for details.
