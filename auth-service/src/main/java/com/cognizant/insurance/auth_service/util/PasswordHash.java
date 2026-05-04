package com.cognizant.insurance.auth_service.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Small utility with a main method to generate BCrypt password hashes. Run with
 * Maven or from your IDE to print a hash for a plaintext password.
 */
public class PasswordHash {

    public static void main(String[] args) {
        String plain = args != null && args.length > 0 ? args[0] : "Revanth";
        var enc = new BCryptPasswordEncoder();
        System.out.println(enc.encode(plain));
    }
}
