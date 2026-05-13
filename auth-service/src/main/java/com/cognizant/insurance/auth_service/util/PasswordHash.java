package com.cognizant.insurance.auth_service.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHash {

    public static void main(String[] args) {
        String plain = args != null && args.length > 0 ? args[0] : "vadivel";
        var enc = new BCryptPasswordEncoder();
        System.out.println(enc.encode(plain));
    }
}
