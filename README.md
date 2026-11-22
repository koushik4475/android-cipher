# Android Cipher 2.0

A lightweight, offline-first Android text encryption app built with Java and Material Components.

> **Security note:** Android Cipher 2.0 uses password-based AES-256-GCM for new messages. It is a small utility project, not a professionally audited password manager, secure messenger, or substitute for a reviewed security product. Do not reuse important account passwords as encryption passwords.

## Features

- Password-based AES-256-GCM authenticated encryption.
- Fresh 128-bit random salt and 96-bit nonce for every encryption.
- PBKDF2-HMAC-SHA-256 key derivation (210,000 iterations).
- Versioned, URL-safe Base64 text envelope with authentication.
- Detects wrong passwords and modified ciphertext.
- Runs locally; no network permission or account required.
- Material Components interface with light/dark system themes.
- Copy and clear controls, validation and background crypto execution.
- Unit tests for round trips, Unicode, randomness, tampering and invalid input.

## Compatibility

- Android Studio with Android Gradle Plugin 8.13.2 support.
- JDK 17.
- Gradle 8.13 (wrapper).
- Android SDK 36.
- Minimum Android version: API 23.
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

- Version: 1 byte.
- Salt: 16 random bytes.
- Nonce: 12 random bytes.
- Cipher: AES/GCM/NoPadding with a 128-bit authentication tag.
- Key derivation: PBKDF2WithHmacSHA256, 210,000 iterations, 256-bit output.
- Associated authenticated data: `AndroidCipher|AC2|1`.

The format version identifies the cryptographic parameters. Future parameter changes must use a new format version rather than silently changing the interpretation of existing messages.

## Important migration note

The original app used Blowfish-CBC with a hardcoded key and fixed IV. That design is unauthenticated and unsuitable for new data. Version 2 does not generate legacy Blowfish ciphertext and does not automatically import old ciphertext. Keep a backup of any old data you still need; do not assume old messages can be opened by the new version.

## Privacy and limitations

- The app requests no internet permission and does not upload message contents.
- The encryption password is required for every operation and is not saved by the app.
- Clipboard contents may be visible to other apps or system clipboard history. Only copy sensitive output when necessary and clear it from the clipboard yourself.
- The password-based format is portable: the same password can decrypt the message on another compatible device.
- This first upgrade focuses on text. File encryption, secure vault storage, biometric unlock, and encrypted history are not implemented yet.
- Cryptographic code should receive independent security review before being used for high-value or regulated data.

## License

This project is distributed under the GNU General Public License v3.0. See [LICENSE](LICENSE).

## Contributing

Issues and pull requests are welcome. Please include reproducible build steps and tests for changes to cryptographic formats.
