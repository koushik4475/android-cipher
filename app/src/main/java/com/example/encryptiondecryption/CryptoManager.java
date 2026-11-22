package com.example.encryptiondecryption;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Password-based, authenticated text encryption.
 *
 * Format: AC2:<base64url(version || salt || nonce || ciphertext || GCM tag)>
 * Version 1 remains readable for backwards compatibility. New messages use
 * version 2 with a stronger PBKDF2 work factor.
 */
public final class CryptoManager {
    private static final String PREFIX = "AC2:";
    private static final byte LEGACY_FORMAT_VERSION = 1;
    private static final byte FORMAT_VERSION = 2;
    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int LEGACY_KDF_ITERATIONS = 210_000;
    private static final int KDF_ITERATIONS = 600_000;
    private static final int SALT_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 1024;
    public static final int MAX_TEXT_BYTES = 1_048_576;
    private static final int MAX_PAYLOAD_BYTES = 1 + SALT_LENGTH + NONCE_LENGTH
            + MAX_TEXT_BYTES + (TAG_LENGTH_BITS / 8);
    private static final int MAX_ENVELOPE_CHARS = PREFIX.length()
            + ((MAX_PAYLOAD_BYTES * 4 + 2) / 3);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CryptoManager() { }

    public static String encrypt(String plaintext, char[] password)
            throws GeneralSecurityException {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Enter text to encrypt.");
        }
        if (plaintext.length() > MAX_TEXT_BYTES) {
            throw new IllegalArgumentException("Text must be 1 MiB or smaller.");
        }
        validatePassword(password, true);

        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        if (plaintextBytes.length > MAX_TEXT_BYTES) {
            Arrays.fill(plaintextBytes, (byte) 0);
            throw new IllegalArgumentException("Text must be 1 MiB or smaller when encoded as UTF-8.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        byte[] nonce = new byte[NONCE_LENGTH];
        byte[] derivedKey = null;
        byte[] ciphertext = null;

        try {
            SECURE_RANDOM.nextBytes(salt);
            SECURE_RANDOM.nextBytes(nonce);
            derivedKey = deriveKey(password, salt, KDF_ITERATIONS);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(derivedKey, "AES"),
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            cipher.updateAAD(associatedData(FORMAT_VERSION));
            ciphertext = cipher.doFinal(plaintextBytes);

            ByteBuffer payload = ByteBuffer.allocate(
                    1 + SALT_LENGTH + NONCE_LENGTH + ciphertext.length);
            payload.put(FORMAT_VERSION);
            payload.put(salt);
            payload.put(nonce);
            payload.put(ciphertext);

            return PREFIX + Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.array());
        } finally {
            Arrays.fill(plaintextBytes, (byte) 0);
            if (derivedKey != null) Arrays.fill(derivedKey, (byte) 0);
            if (ciphertext != null) Arrays.fill(ciphertext, (byte) 0);
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(nonce, (byte) 0);
        }
    }

    public static String decrypt(String encryptedText, char[] password)
            throws GeneralSecurityException {
        if (encryptedText == null || !encryptedText.startsWith(PREFIX)) {
            throw new IllegalArgumentException(
                    "This is not an Android Cipher encrypted message.");
        }
        if (encryptedText.length() > MAX_ENVELOPE_CHARS) {
            throw new IllegalArgumentException("Encrypted text exceeds the 1 MiB message limit.");
        }
        validatePassword(password, false);

        final byte[] payload;
        try {
            String encodedPayload = encryptedText.substring(PREFIX.length()).trim();
            if (encodedPayload.length() > MAX_ENVELOPE_CHARS - PREFIX.length()) {
                throw new IllegalArgumentException("Encrypted text exceeds the 1 MiB message limit.");
            }
            payload = Base64.getUrlDecoder().decode(encodedPayload);
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage() != null
                    && exception.getMessage().contains("1 MiB")) {
                throw exception;
            }
            throw new IllegalArgumentException("The encrypted text is malformed.", exception);
        }

        final int minimumLength = 1 + SALT_LENGTH + NONCE_LENGTH + (TAG_LENGTH_BITS / 8);
        if (payload.length < minimumLength) {
            Arrays.fill(payload, (byte) 0);
            throw new IllegalArgumentException("The encrypted text is incomplete.");
        }
        if (payload.length > MAX_PAYLOAD_BYTES) {
            Arrays.fill(payload, (byte) 0);
            throw new IllegalArgumentException("Encrypted text exceeds the 1 MiB message limit.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        byte[] nonce = new byte[NONCE_LENGTH];
        byte[] ciphertext = null;
        byte[] derivedKey = null;
        byte[] plaintextBytes = null;

        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte version = buffer.get();
            final int iterations;
            if (version == LEGACY_FORMAT_VERSION) {
                iterations = LEGACY_KDF_ITERATIONS;
            } else if (version == FORMAT_VERSION) {
                iterations = KDF_ITERATIONS;
            } else {
                throw new IllegalArgumentException(
                        "This encryption format version is not supported.");
            }

            buffer.get(salt);
            buffer.get(nonce);
            ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            derivedKey = deriveKey(password, salt, iterations);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(derivedKey, "AES"),
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            cipher.updateAAD(associatedData(version));
            plaintextBytes = cipher.doFinal(ciphertext);
            if (plaintextBytes.length > MAX_TEXT_BYTES) {
                throw new IllegalArgumentException("Decrypted text exceeds the 1 MiB message limit.");
            }
            try {
                return StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(plaintextBytes))
                        .toString();
            } catch (CharacterCodingException exception) {
                throw new IllegalArgumentException(
                        "Decrypted content is not valid UTF-8 text.", exception);
            }
        } finally {
            Arrays.fill(payload, (byte) 0);
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(nonce, (byte) 0);
            if (ciphertext != null) Arrays.fill(ciphertext, (byte) 0);
            if (derivedKey != null) Arrays.fill(derivedKey, (byte) 0);
            if (plaintextBytes != null) Arrays.fill(plaintextBytes, (byte) 0);
        }
    }

    private static void validatePassword(char[] password, boolean forEncryption) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Enter your encryption password.");
        }
        if (password.length > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be 1024 characters or fewer.");
        }
        if (forEncryption && password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Use a password with at least 12 characters.");
        }
    }

    private static byte[] associatedData(byte version) {
        return ("AndroidCipher|AC2|" + version).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] deriveKey(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(
                password, salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(KDF_ALGORITHM)
                    .generateSecret(keySpec).getEncoded();
        } finally {
            keySpec.clearPassword();
        }
    }
}
