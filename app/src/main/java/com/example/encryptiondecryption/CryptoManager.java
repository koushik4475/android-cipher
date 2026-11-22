package com.example.encryptiondecryption;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
 * The password is never stored in the encrypted payload.
 */
public final class CryptoManager {
    private static final String PREFIX = "AC2:";
    private static final byte FORMAT_VERSION = 1;
    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BITS = 256;
    private static final int KDF_ITERATIONS = 210_000;
    private static final int SALT_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final byte[] AAD = "AndroidCipher|AC2|1".getBytes(StandardCharsets.UTF_8);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CryptoManager() { }

    public static String encrypt(String plaintext, char[] password)
            throws GeneralSecurityException {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Enter text to encrypt.");
        }
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Use a password with at least 12 characters.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        byte[] nonce = new byte[NONCE_LENGTH];
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] derivedKey = null;
        byte[] ciphertext = null;

        try {
            SECURE_RANDOM.nextBytes(salt);
            SECURE_RANDOM.nextBytes(nonce);
            derivedKey = deriveKey(password, salt);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(derivedKey, "AES"),
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            cipher.updateAAD(AAD);
            ciphertext = cipher.doFinal(plaintextBytes);

            ByteBuffer payload = ByteBuffer.allocate(1 + SALT_LENGTH + NONCE_LENGTH + ciphertext.length);
            payload.put(FORMAT_VERSION);
            payload.put(salt);
            payload.put(nonce);
            payload.put(ciphertext);

            return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.array());
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
            throw new IllegalArgumentException("This is not an Android Cipher 2 encrypted message.");
        }
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Enter the password used for encryption.");
        }

        final byte[] payload;
        try {
            payload = Base64.getUrlDecoder().decode(encryptedText.substring(PREFIX.length()).trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("The encrypted text is malformed.", exception);
        }

        final int minimumLength = 1 + SALT_LENGTH + NONCE_LENGTH + (TAG_LENGTH_BITS / 8);
        if (payload.length < minimumLength) {
            Arrays.fill(payload, (byte) 0);
            throw new IllegalArgumentException("The encrypted text is incomplete.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        byte[] nonce = new byte[NONCE_LENGTH];
        byte[] ciphertext = null;
        byte[] derivedKey = null;
        byte[] plaintextBytes = null;

        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte version = buffer.get();
            if (version != FORMAT_VERSION) {
                throw new IllegalArgumentException("This encryption format version is not supported.");
            }

            buffer.get(salt);
            buffer.get(nonce);
            ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            derivedKey = deriveKey(password, salt);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(derivedKey, "AES"),
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            cipher.updateAAD(AAD);
            plaintextBytes = cipher.doFinal(ciphertext);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } finally {
            Arrays.fill(payload, (byte) 0);
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(nonce, (byte) 0);
            if (ciphertext != null) Arrays.fill(ciphertext, (byte) 0);
            if (derivedKey != null) Arrays.fill(derivedKey, (byte) 0);
            if (plaintextBytes != null) Arrays.fill(plaintextBytes, (byte) 0);
        }
    }

    private static byte[] deriveKey(char[] password, byte[] salt)
            throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, KDF_ITERATIONS, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(KDF_ALGORITHM)
                    .generateSecret(keySpec).getEncoded();
        } finally {
            keySpec.clearPassword();
        }
    }
}
