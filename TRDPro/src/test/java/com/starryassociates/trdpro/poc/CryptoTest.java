package com.starryassociates.trdpro.poc;

import com.starryassociates.trdpro.util.CryptoUtil;

public class CryptoTest {
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

