NowID SDK (`nowid-sdk`) offers easy-to-use tools for credential verification using COSE_Sign1 and
CBOR encoding over NFC. The current version is implemented in Kotlin, tailored specifically for
Android applications.

## Features

* **COSE\_Sign1 Verification**: Supports verification of credentials using COSE\_Sign1.
* **CBOR Encoding**: Efficient handling of credential data using CBOR.
* **NFC Integration**: Seamlessly receive and process credential data via NFC intents.

## Quick Start

### Installation

Add the JitPack repository to your root `build.gradle`:

```gradle
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Then include the library in your module's `build.gradle`:

```gradle
implementation 'com.github.JunbinDeng:NowID:Tag'
```

### Basic Usage

#### 1. NFC Integration (Android Activity)

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    val publicKey: PublicKey = loadTrustedKey()

    val result = MdocProcessor.process(
        transport = NfcTransportStrategy(intent),
        publicKey = publicKey
    )

    result.onSuccess {
        Log.d("Verifier", "Decoded payload: $it")
    }.onFailure {
        Log.e("Verifier", "Verification failed", it)
    }
}
```

Make sure your `AndroidManifest.xml` declares the intent filter:

```xml

<activity android:name=".VerifierActivity">
    <intent-filter>
        <action android:name="android.nfc.action.NDEF_DISCOVERED" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="application/cose-sign1+cbor" />
    </intent-filter>
</activity>
```

#### 2. Testing Without NFC

```kotlin
val publicKey: PublicKey = loadTrustedKey()

val payload = buildSignedCosePayload()
val record = NdefRecord.createMime("application/cose-sign1+cbor", payload)
val ndef = NdefMessage(arrayOf(record))
val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
    putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndef))
}

val result = MdocProcessor.process(NfcTransportStrategy(intent), publicKey)
```

## License

MIT License. See [LICENSE](../LICENSE) for details.