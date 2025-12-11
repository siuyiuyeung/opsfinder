package com.igsl.opsfinder.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility tool to encrypt passwords using BCrypt.
 * Same encryption method as used in the application.
 *
 * Usage:
 *   Run this class with password as argument to generate BCrypt hash
 *
 * Example:
 *   java PasswordEncryptionTool admin123
 */
public class PasswordEncryptionTool {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║           OpsFinder Password Encryption Tool                   ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Usage: java PasswordEncryptionTool <password>");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  java PasswordEncryptionTool admin123");
            System.out.println();
            System.out.println("Or run from Gradle:");
            System.out.println("  ./gradlew runPasswordTool --args=\"admin123\"");
            System.out.println();
            System.exit(1);
        }

        String password = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           Password Encrypted Successfully                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Plain Text Password: " + password);
        System.out.println();
        System.out.println("BCrypt Hash:");
        System.out.println(hash);
        System.out.println();
        System.out.println("Hash Length: " + hash.length() + " characters");
        System.out.println();
        System.out.println("You can use this hash in:");
        System.out.println("  - Liquibase changelog files");
        System.out.println("  - Direct SQL INSERT/UPDATE statements");
        System.out.println("  - Application configuration");
        System.out.println();
    }
}
