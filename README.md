# NowID

[![](https://jitpack.io/v/JunbinDeng/NowID.svg)](https://jitpack.io/#JunbinDeng/NowID)

NowID provides secure, decentralized identity solutions compatible with Verifiable Credentials (VC)
standards.

## Repositories

- [NowID SDK](./nowid-sdk): Kotlin Android SDK for NFC-based verifiable credential verification.
- [NowID Safe App](./nowid-safe): Secure password manager Android application built with Jetpack
  Compose, Android Keystore, and Biometric Authentication.

## Architecture

The NowID ecosystem integrates issuers, holders, and verifiers, using cryptographic algorithms to
securely issue, transmit, and verify mobile verifiable credentials.

<img src="https://github.com/user-attachments/assets/7f1fdd1f-0a59-48d6-9ee0-25814dfe35d8" alt="verifiable_credentials_ecosystem" width="600"/>

## Supported Algorithms

| Algorithm  | Full Name                            | Key System | Used In                                  |
|------------|--------------------------------------|------------|------------------------------------------|
| ES256      | ECDSA using P-256 and SHA-256        | Asymmetric | JWT, MATTR, **NowID**                    |
| RS256      | RSASSA-PKCS1-v1_5 using SHA-256      | Asymmetric | JWT, X.509 leaf certificates             |
| HS256      | HMAC using SHA-256                   | Symmetric  | JWT  (server-to-server, trusted domains) |
| COSE-EdDSA | COSE_Sign1 EdDSA (Ed25519 + SHA-512) | Asymmetric | mdoc holder-binding signature            |

| Underlying Algorithm | Full Name                                        | Type              | Used In                                          |
|----------------------|--------------------------------------------------|-------------------|--------------------------------------------------|
| ECDSA                | Elliptic Curve Digital Signature Algorithm       | Digital Signature | ES256, WebAuthn, TLS 1.3 certs, MATTR, **NowID** |
| RSASSA-PKCS1-v1_5    | RSA Signature Scheme with Appendix (PKCS#1 v1.5) | Digital Signature | RS256, legacy TLS cert chains                    |
| HMAC                 | Hash-based Message Authentication Code           | Message Auth Code | HS256 tokens, API key signing                    |
| EdDSA                | Edwards-curve Digital Signature Algorithm        | Digital Signature | COSE-EdDSA, mdoc holder binding                  |

| Elliptic Curve | Full Name                     | Used In                         |
|----------------|-------------------------------|---------------------------------|
| P-256          | NIST Curve P-256 (secp256r1)  | ECDSA (ES256), MATTR, **NowID** |
| Ed25519        | Edwards Curve over Curve25519 | EdDSA, mdoc holder binding      |

| Hash Algorithm | Full Name                 | Feature                                            | Used In                               |
|----------------|---------------------------|----------------------------------------------------|---------------------------------------|
| SHA-256        | Secure Hash Algorithm 256 | Balanced speed vs. security; hardware-wide support | ES256, RS256, HS256, MATTR, **NowID** |
| SHA-512        | Secure Hash Algorithm 512 | Higher collision resistance; 64-bit optimized      | EdDSA, mdoc holder binding            |

## Reference

- [MATTR Credential Formats Overview](https://learn.mattr.global/docs/formats-overview)
- [MATTR mdoc Implementation](https://learn.mattr.global/docs/mdocs/mattr)
- [RFC 7518 - JSON Web Algorithms](https://datatracker.ietf.org/doc/html/rfc7518#section-3.1)
- [RFC 8032 - Edwards-Curve Digital Signature Algorithm (EdDSA)](https://datatracker.ietf.org/doc/html/rfc8032)
- [NIST FIPS 180-4 - Secure Hash Standard (SHA)](https://nvlpubs.nist.gov/nistpubs/fips/nist.fips.180-4.pdf)

## Contributing

Contributions are welcome! Please submit issues or pull requests.

## License

MIT License. See [LICENSE](./LICENSE) for details.
