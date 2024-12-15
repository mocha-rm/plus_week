package com.example.demo.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;



class PasswordEncoderTest {
    @Test
    void passwordEncodingTest() {
        // Given
        String rawPassword = "abc123";

        // When
        String encodedPassword = PasswordEncoder.encode(rawPassword);

        // Then
        assertNotNull(encodedPassword);
        System.out.println("Encoded Password : " + encodedPassword);
    }

    @Test
    void isPasswordMatches() {
        // Given
        String rawPassword = "abc123";
        String encodedPassword = PasswordEncoder.encode(rawPassword);
        System.out.println("Encoded Password : " + encodedPassword);

        // When
        boolean isMatch = PasswordEncoder.matches(rawPassword, encodedPassword);

        // Then
        assertTrue(isMatch, "패스워드 일치");
    }

    @Test
    void isPasswordNotMatches() {
        // Given
        String rawPassword = "abc123";
        String wrongPassword = "hello123";
        String encodedPassword = PasswordEncoder.encode(rawPassword);

        System.out.println("Encoded Password : " + encodedPassword);

        // When
        boolean isMatch = PasswordEncoder.matches(wrongPassword, encodedPassword);

        // Then
        assertTrue(isMatch, "패스워드 불일치");
    }

}