package com.example.encryptiondecryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

public class CryptoManagerTest {
    private static final char[] PASSWORD = "correct horse battery staple".toCharArray();

    @Test
    public void encryptAndDecryptUnicodeTextWithCurrentFormat() throws Exception {
        String original = "Hello, Android Cipher 🔐\nनमस्ते — encryption test";
        String encrypted = CryptoManager.encrypt(original, PASSWORD);

        assertTrue(encrypted.startsWith("AC2:"));
        byte[] payload = Base64.getUrlDecoder().decode(encrypted.substring(4));
        assertEquals(2, payload[0]);
        assertEquals(original, CryptoManager.decrypt(encrypted, PASSWORD));
    }

    @Test
    public void decryptsVersionOneMessagesForBackwardsCompatibility() throws Exception {
        String legacyMessage = createLegacyVersionOneMessage(
                "Old Android Cipher message");
        assertEquals("Old Android Cipher message",
                CryptoManager.decrypt(legacyMessage, PASSWORD));
    }

    @Test
    public void eachEncryptionUsesFreshRandomness() throws Exception {
        String first = CryptoManager.encrypt("same message", PASSWORD);
        String second = CryptoManager.encrypt("same message", PASSWORD);

        assertNotEquals(first, second);
        assertEquals("same message", CryptoManager.decrypt(first, PASSWORD));
        assertEquals("same message", CryptoManager.decrypt(second, PASSWORD));
    }

    @Test(expected = GeneralSecurityException.class)
    public void wrongPasswordCannotDecrypt() throws Exception {
        String encrypted = CryptoManager.encrypt("secret", PASSWORD);
        CryptoManager.decrypt(encrypted, "not the correct password".toCharArray());
    }

    @Test(expected = GeneralSecurityException.class)
    public void modifiedCiphertextIsRejected() throws Exception {
        String encrypted = CryptoManager.encrypt("tamper test", PASSWORD);
        int index = 4 + 24;
        char replacement = encrypted.charAt(index) == 'A' ? 'B' : 'A';
        String modified = encrypted.substring(0, index) + replacement
                + encrypted.substring(index + 1);

        CryptoManager.decrypt(modified, PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void malformedEnvelopeIsRejected() throws Exception {
        CryptoManager.decrypt("not-an-android-cipher-message", PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortEncryptionPasswordIsRejected() throws Exception {
        CryptoManager.encrypt("secret", "short".toCharArray());
    }


    @Test(expected = IllegalArgumentException.class)
    public void rejectsPlaintextLargerThanOneMiB() throws Exception {
        CryptoManager.encrypt("a".repeat(CryptoManager.MAX_TEXT_BYTES + 1), PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUtf8PayloadLargerThanOneMiB() throws Exception {
        CryptoManager.encrypt("界".repeat(400_000), PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOversizedEnvelopeBeforeBase64Decoding() throws Exception {
        CryptoManager.decrypt("AC2:" + "A".repeat(1_500_000), PASSWORD);
    }

    private static String createLegacyVersionOneMessage(String plaintext)
            throws Exception {
        byte[] salt = new byte[16];
        byte[] nonce = new byte[12];
        java.util.Arrays.fill(salt, (byte) 0x11);
        java.util.Arrays.fill(nonce, (byte) 0x22);

        PBEKeySpec keySpec = new PBEKeySpec(PASSWORD, salt, 210_000, 256);
        byte[] key;
        try {
            key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec).getEncoded();
        } finally {
            keySpec.clearPassword();
        }

        byte[] ciphertext;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, nonce));
            cipher.updateAAD("AndroidCipher|AC2|1".getBytes(StandardCharsets.UTF_8));
            ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        } finally {
            java.util.Arrays.fill(key, (byte) 0);
        }

        ByteBuffer payload = ByteBuffer.allocate(1 + salt.length + nonce.length
                + ciphertext.length);
        payload.put((byte) 1);
        payload.put(salt);
        payload.put(nonce);
        payload.put(ciphertext);
        return "AC2:" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.array());
    }
}
