package com.mattmx.nametags.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TextFormatterTest {

    @ParameterizedTest
    @CsvSource({
        "&x&c&7&0&0&3&9[Admin],&#c70039[Admin]",
        "&x&9&0&0&c&3&fCoyotea&r,&#900c3fCoyotea&r",
        "&x&9&0&0&c&3&fCoyotea,&#900c3fCoyotea",
        "&x&c&7&0&0&3&9[Admin] &x&9&0&0&c&3&fCoyotea&r,&#c70039[Admin] &#900c3fCoyotea&r"
    })
    public void test_convertLegacyHex(String input, String expectedOut) {
        final String out = TextFormatter.convertLegacyHex(input);

        Assertions.assertEquals(expectedOut, out);
    }

}