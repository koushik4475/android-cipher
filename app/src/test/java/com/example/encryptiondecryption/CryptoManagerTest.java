package com.example.encryptiondecryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.security.GeneralSecurityException;

import org.junit.Test;

public class CryptoManagerTest {
    private static final char[] PASSWORD = "correct horse battery staple".toCharArray();

    @Test
    public void encryptAndDecryptUnicodeText() throws Exception {
        String original = "Hello, Android Cipher 🔐\nनमस्ते — encryption test";
        String encrypted = CryptoManager.encrypt(original, PASSWORD);

        assertTrue(encrypted.startsWith("AC2:"));
        assertEquals(original, CryptoManager.decrypt(encrypted, PASSWORD));
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
        int index = CryptoManagerTestPrefix.LENGTH + 24;
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

    private static final class CryptoManagerTestPrefix {
        private static final int LENGTH = 4;
    }
}
