package com.example.piCloud;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Key;

@SpringBootApplication
public class PiCloudApplication {

    /**
     * Generates a secure secret key for JWT authentication.
     * The generated key is BASE64 encoded and compatible with HMAC-SHA256 signing.
     * Run this method once and save the output in your .env file.
     * @return BASE64 encoded secret key string
     */
    public static String generateSecretKey() {
        byte[] keyBytes = new byte[32]; // 256 bits for HS256
        new java.security.SecureRandom().nextBytes(keyBytes);
        return java.util.Base64.getEncoder().encodeToString(keyBytes);
    }

    /**Comment in the SpringApplication.run line after generating the key once
     * And comment out the rest of the code within the main method*/
	public static void main(String[] args) {

        //System.out.println("Generated JWT Secret Key: " + generateSecretKey());

		SpringApplication.run(PiCloudApplication.class, args);
	}

}


