package com.example.demo.security;



import jakarta.persistence.AttributeConverter;

import jakarta.persistence.Converter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;



@Component

@Converter

// JPA hook — encrypts String fields automatically on write to MySQL, decrypts on read

public class EncryptedStringConverter implements AttributeConverter<String, String> {



    private static FieldEncryptionService encryptionService;



    /// spring sets the encryption service once at startup

    @Autowired

    public void setEncryptionService(FieldEncryptionService service) {

        EncryptedStringConverter.encryptionService = service;

    }



    /// called when saving entity — plain text -> ENC:... in database

    @Override

    public String convertToDatabaseColumn(String attribute) {

        if (attribute == null || encryptionService == null) {

            return attribute;

        }

        return encryptionService.encrypt(attribute);

    }



    /// called when loading entity — decrypt back for Java code

    @Override

    public String convertToEntityAttribute(String dbData) {

        if (dbData == null || encryptionService == null) {

            return dbData;

        }

        return encryptionService.decrypt(dbData);

    }

}


