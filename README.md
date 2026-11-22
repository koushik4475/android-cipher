# Android Cipher 2.1

A lightweight, offline-first Android text encryption app built with Java and Material Components.

> **Security note:** Android Cipher 2.1 uses password-based AES-256-GCM for new messages. It is a small utility project, not a professionally audited password manager, secure messenger, or substitute for a reviewed security product. Do not reuse important account passwords as encryption passwords.

## Features

- Password-based AES-256-GCM authenticated encryption.
- Fresh 128-bit random salt and 96-bit nonce for every encryption.
- PBKDF2-HMAC-SHA-256 key derivation (600,000 iterations for new messages).
- Versioned, URL-safe Base64 text envelope with authentication.
- Detects wrong passwords and modified ciphertext.
- Runs locally; no network permission or account required.
- Refined Material interface with light/dark themes, edge-to-edge system bars, custom launcher icon/splash screen and centered content on wide windows.
- Paste, copy, share, clear-all, character count and operation progress/feedback.
- Sensitive clipboard preview marking on Android 13+.
- Sensitive form fields are excluded from autofill and view-state restoration; the result is read-only and selectable.
- Automated tests for Unicode, randomness, tampering, invalid input and old-format compatibility.

## Compatibility

- Android Studio with Android Gradle Plugin 8.13.2 support.
- JDK 17.
- Gradle 8.13 (wrapper).
- Android SDK 36.
- Minimum Android version: API 26.
- Java source compatibility: Java 17.

## Build

1. Clone this repository.
2. Open the project folder in Android Studio and allow Gradle sync.
3. Install Android SDK Platform 36 if prompted.
4. Run the app on an emulator or device.

From a terminal with JDK 17 and Android SDK configured:

```bash
./gradlew clean testDebugUnitTest assembleDebug
```

On Windows:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions runs the unit tests and assembles a debug APK for pushes and pull requests.

## Encrypt and decrypt

1. Enter the text you want to protect.
2. Enter a strong password of at least 12 characters.
3. Select **Encrypt** and copy the resulting `AC2:` message.
4. To decrypt, paste the complete `AC2:` message into the input field, enter the same password, and select **Decrypt**.

**Keep the password safe.** It is not embedded in the encrypted message and cannot be recovered if lost. Use a unique passphrase. A password manager can help store it.

## Encryption format

The text envelope is:

```text
AC2: Base64URL(version || salt || nonce || ciphertext || GCM-tag)
```

- Version: 1 byte. New encryption writes version 2; version 1 messages remain decryptable.
- Salt: 16 random bytes per message.
- Nonce: 12 random bytes per message.
- Cipher: AES/GCM/NoPadding with a 128-bit authentication tag.
- Key derivation: PBKDF2WithHmacSHA256, 600,000 iterations for version 2 (256-bit output).
- Version 1 compatibility: PBKDF2-HMAC-SHA-256 at 210,000 iterations.
- Associated authenticated data binds the envelope to its format version.

The format version identifies the cryptographic parameters. The app retains version 1 decryption support so existing Android Cipher 2.0 messages remain readable. The 600,000-iteration work factor uses OWASP's current PBKDF2-HMAC-SHA-256 password-storage recommendation as a conservative reference; actual responsiveness still needs benchmarking on supported physical devices.

## Important migration note

The original app used Blowfish-CBC with a hardcoded key and fixed IV. That design is unauthenticated and unsuitable for new data. Version 2 does not generate legacy Blowfish ciphertext and does not automatically import old ciphertext. Keep a backup of any old data you still need; do not assume old messages can be opened by the new version.

## Privacy and limitations

- The app requests no internet permission and does not upload message contents.
- The encryption password is required for every operation and is not saved by the app.
- Clipboard contents may be visible to other apps or system clipboard history. Android Cipher marks copied content as sensitive on Android 13+, but only copy sensitive output when necessary.
- The password-based format is portable: the same password can decrypt the message on another compatible device.
- This release focuses on text. File encryption, secure vault storage, biometric unlock, and encrypted history are not implemented.
- Cryptographic code should receive independent security review before being used for high-value or regulated data.

## License

This project is distributed under the GNU General Public License v3.0. See [LICENSE](LICENSE).

## Contributing

Issues and pull requests are welcome. Please include reproducible build steps and tests for changes to cryptographic formats.
