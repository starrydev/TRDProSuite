package com.starryassociates.trdpro.util;

import com.starryassociates.trdpro.config.ConfigManager;

public class CryptoUtil {

    private static final KmsEncryptionHelper encryptionHelper = ConfigManager.getInstance().getEncryptionHelper();

    public static String encrypt(String data) {
        if (data == null) return null;
        try {
            return encryptionHelper.encrypt(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            return encryptionHelper.decrypt(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Decryption failed", e);
        }
    }
}


/*
public class CryptoUtilExample {

    public static void main(String[] args) {
        String plaintext = "Sensitive Data";

        // Encrypt the data
        String encryptedData = CryptoUtil.encrypt(plaintext);
        System.out.println("Encrypted Data: " + encryptedData);

        // Decrypt the data
        String decryptedData = CryptoUtil.decrypt(encryptedData);
        System.out.println("Decrypted Data: " + decryptedData);
    }
}

 */
