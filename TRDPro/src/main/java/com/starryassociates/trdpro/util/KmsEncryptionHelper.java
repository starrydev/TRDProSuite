/*
Custom KMS encryption to align all services with AWS V2
 */
package com.starryassociates.trdpro.util;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class KmsEncryptionHelper {

    private final KmsClient kmsClient;
    private final String keyId;

    public KmsEncryptionHelper(KmsClient kmsClient, String keyId) {
        this.kmsClient = kmsClient;
        this.keyId = keyId;
    }

    public String encrypt(String plaintext) throws Exception {
        // Generate a data key
        GenerateDataKeyRequest dataKeyRequest = GenerateDataKeyRequest.builder()
                .keyId(keyId)
                .keySpec("AES_256")
                .build();

        GenerateDataKeyResponse dataKeyResponse = kmsClient.generateDataKey(dataKeyRequest);

        // Extract the plaintext key
        byte[] plaintextKey = dataKeyResponse.plaintext().asByteArray();

        // Encrypt the data using the plaintext key
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(plaintextKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encryptedData = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Base64 encode the encrypted data
        String encryptedDataString = Base64.getEncoder().encodeToString(encryptedData);

        // Combine the encrypted data and the encrypted key
        String encryptedKeyString = Base64.getEncoder().encodeToString(dataKeyResponse.ciphertextBlob().asByteArray());

        return encryptedKeyString + ":" + encryptedDataString;
    }

    public String decrypt(String encryptedDataWithKey) throws Exception {
        // Split the encrypted key and encrypted data
        String[] parts = encryptedDataWithKey.split(":");
        String encryptedKeyString = parts[0];
        String encryptedDataString = parts[1];

        byte[] encryptedKey = Base64.getDecoder().decode(encryptedKeyString);

        // Decrypt the data key using KMS
        DecryptRequest decryptRequest = DecryptRequest.builder()
                .ciphertextBlob(SdkBytes.fromByteArray(encryptedKey))
                .build();

        byte[] plaintextKey = kmsClient.decrypt(decryptRequest).plaintext().asByteArray();

        // Decrypt the data using the plaintext key
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(plaintextKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedDataString));

        return new String(decryptedData, "UTF-8");
    }
}
