package com.example.demo.security;



import com.example.demo.exception.BadRequestException;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;

import java.security.SecureRandom;

import java.util.Base64;

import javax.crypto.Cipher;

import javax.crypto.spec.GCMParameterSpec;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;



@Service

// AES encrypt for sensitive text fields (profile notes, clinician annotations) before MySQL

public class FieldEncryptionService {



    private static final String PREFIX = "ENC:";

    private static final int GCM_TAG_LENGTH = 128;

    private static final int IV_LENGTH = 12;



    private final SecretKeySpec secretKey;



    /// key comes from APP_ENCRYPTION_KEY env var, hashed to 256-bit AES key

    public FieldEncryptionService(@Value("${app.encryption.key:}") String encryptionKey) {

        if (encryptionKey == null || encryptionKey.isBlank()) {

            throw new BadRequestException("APP_ENCRYPTION_KEY must be configured.");

        }

        byte[] keyBytes = sha256(encryptionKey);

        this.secretKey = new SecretKeySpec(keyBytes, "AES");

    }



    /// encrypt plain text before saving to DB — stored as ENC:base64...

    public String encrypt(String plainText) {

        if (plainText == null || plainText.isBlank()) {

            return plainText;

        }

        if (plainText.startsWith(PREFIX)) {

            return plainText;

        }



        try {

            byte[] iv = new byte[IV_LENGTH];

            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return PREFIX + Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception ex) {

            throw new BadRequestException("Unable to encrypt sensitive field.");

        }

    }



    /// decrypt when reading from DB back to plain text for API response

    public String decrypt(String cipherText) {

        if (cipherText == null || cipherText.isBlank()) {

            return cipherText;

        }

        if (!cipherText.startsWith(PREFIX)) {

            return cipherText;

        }



        try {

            String payload = cipherText.substring(PREFIX.length());

            String[] parts = payload.split(":", 2);

            byte[] iv = Base64.getDecoder().decode(parts[0]);

            byte[] encrypted = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);

        } catch (Exception ex) {

            throw new BadRequestException("Unable to decrypt sensitive field.");

        }

    }



    /// hash the config string into proper AES key length

    private byte[] sha256(String value) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return digest.digest(value.getBytes(StandardCharsets.UTF_8));

        } catch (Exception ex) {

            throw new BadRequestException("Unable to initialize encryption key.");

        }

    }

}


