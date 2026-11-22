# Android Cipher 2.2

A lightweight, offline-first Android text encryption app built with Java and Material 3 Expressive.

> **Security note:** Android Cipher is a focused text-encryption utility, not a professionally audited password manager, secure messenger, or substitute for a reviewed security product. Do not reuse account passwords as encryption passwords.

## Features

- Password-based AES-256-GCM authenticated encryption.
- Fresh 128-bit random salt and 96-bit nonce for every encryption.
- PBKDF2-HMAC-SHA-256 key derivation (600,000 iterations for new messages).
- Versioned, URL-safe Base64 text envelope; version 1 messages remain decryptable.
- Detects wrong passwords and modified ciphertext.
- Encrypted/decrypted message limit of 1 MiB to bound memory and decoding work.
- Offline operation; no account, Internet permission, analytics SDK or remote service.
- Material 3 Expressive UI with light/dark themes, edge-to-edge insets and adaptive centered content.
- Paste, copy, share, clear-all, Unicode-aware character count and progress/error feedback.
- Clears the password field when an operation begins; clear-all removes only clipboard content copied by Android Cipher.
- Espresso UI smoke tests and cryptographic regression tests.

## Compatibility and toolchain

- Android Studio with Android Gradle Plugin 9.5.2.
- JDK 17.
- Gradle 9.9.1 (wrapper).
- Android SDK Platform 36 for compilation (Android 16 SDK).
- Minimum Android version: API 26 (Android 8.0).
- Target SDK: API 36 (Android 16), matching the current Google Play submission requirement.
- Java source compatibility: Java 17.

## Build and test

From a terminal with JDK 17 and Android SDK 36 configured:

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease
```

On Windows:

```powershell
.\gradlew.bat clean testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

Release APK: `app/build/outputs/apk/release/app-release.apk`

Android App Bundle: `app/build/outputs/bundle/release/app-release.aab`

Without a configured upload keystore, the local release APK/AAB is unsigned and is not suitable for Play Console submission. Debug builds use a separate `.debug` application ID.

GitHub Actions runs unit tests, lint, debug/release APK and AAB builds, and Espresso smoke tests on Android API 26 and 36. Dependabot checks Gradle and GitHub Actions dependencies weekly.

## Signed GitHub and Play release

The signed-release workflow creates a GitHub Release containing a signed APK and AAB when the app version changes on `main`. It deliberately refuses to publish an unsigned or debug-signed APK as a stable release.

To enable it, add these repository Actions secrets:

- `ANDROID_KEYSTORE_BASE64`: base64 encoding of your private Android upload keystore.
- `ANDROID_KEYSTORE_PASSWORD`: keystore password.
- `ANDROID_KEY_ALIAS`: key alias.
- `ANDROID_KEY_PASSWORD`: key password.

Keep the keystore and passwords private; never commit them. Once these four secrets are configured, the release workflow can publish signed APK and AAB assets automatically after a versioned merge to `main`. The workflow does not submit to Google Play; Play Console review and store listing work remain separate.

## Encrypt and decrypt

1. Enter the text you want to protect (up to 1 MiB after UTF-8 encoding).
2. Enter a strong, unique password of at least 12 characters.
3. Select **Encrypt** and copy the resulting `AC2:` message.
4. Paste the full message back into the input, enter the same password and select **Decrypt**.

The password is not embedded in the encrypted message and cannot be recovered if lost. The password field is cleared when an operation begins.

## Encryption format

The envelope is:

```text
AC2: Base64URL(version || salt || nonce || ciphertext || GCM-tag)
```

- Version: 1 byte. New encryption writes version 2; version 1 remains supported for decryption.
- Salt: 16 random bytes per message.
- Nonce: 12 random bytes per message.
- Cipher: AES/GCM/NoPadding with a 128-bit authentication tag.
- Version 2 key derivation: PBKDF2WithHmacSHA256, 600,000 iterations, 256-bit output.
- Version 1 key derivation: PBKDF2-HMAC-SHA-256, 210,000 iterations.
- Associated authenticated data binds ciphertext to the format version.

The PBKDF2 work factor uses OWASP's 600,000-iteration PBKDF2-HMAC-SHA-256 recommendation as a conservative reference. KDF latency still requires benchmarking on representative devices; a high iteration count is not a substitute for a strong passphrase or an independent security review.

## Important migration note

The original app used Blowfish-CBC with a hardcoded key and fixed IV. That design was unauthenticated and is not supported for new messages. Android Cipher 2.x does not automatically import old Blowfish ciphertext. Preserve backups of any legacy data you still need.

## Privacy and limitations

- The app requests no Internet permission and does not upload message contents.
- Encryption passwords are not saved to preferences or included in ciphertext.
- The app limits messages to 1 MiB to bound work on untrusted pasted input.
- Clipboard contents can be visible to the keyboard or other system surfaces. Android Cipher marks copied output as sensitive for modern clipboard previews and clear-all removes its own copied clip when it is still the active clip.
- Sharing a decrypted result deliberately sends that text to the app selected in Android's Sharesheet.
- This release focuses on text. File encryption, encrypted vault storage, biometric unlock and encrypted history are not implemented.
- Cryptographic code has not had an independent audit. Do not use it for high-value or regulated data without professional review.

## License

This project is distributed under the GNU General Public License v3.0. See [LICENSE](LICENSE).

## Contributing

Issues and pull requests are welcome. Changes to cryptographic formats must include compatibility tests and must not silently change the interpretation of existing envelopes.
