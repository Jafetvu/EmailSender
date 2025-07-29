package com.example.emailsender.utils.validation;

import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    public static boolean isValid(String email) {
        return email != null && EMAIL_REGEX.matcher(email).find();
    }
}