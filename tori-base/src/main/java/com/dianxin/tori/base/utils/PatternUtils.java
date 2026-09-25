package com.dianxin.tori.base.utils;

import com.dianxin.tori.base.annotations.ReleasedSince;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Utility class containing pre-compiled regex patterns for string validation checks.
 */
@ReleasedSince("26.8.301")
@SuppressWarnings("unused")
public final class PatternUtils {

    private PatternUtils() {
        throw new AssertionError("Utility class cannot be instantiated.");
    }

    private static final Pattern DIGITS_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern LETTERS_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern LETTERS_AND_DIGITS_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    /**
     * Verifies whether the string consists strictly of numeric digits (0-9).
     *
     * @param ctx the input string to test
     * @return {@code true} if non-null and containing only digits, otherwise {@code false}
     */
    public static boolean isOnlyDigits(@Nullable String ctx) {
        return ctx != null && DIGITS_PATTERN.matcher(ctx).matches();
    }

    /**
     * Verifies whether the string consists strictly of English alphabetic characters (a-z, A-Z).
     *
     * @param ctx the input string to test
     * @return {@code true} if non-null and containing only letters, otherwise {@code false}
     */
    public static boolean isOnlyLetters(@Nullable String ctx) {
        return ctx != null && LETTERS_PATTERN.matcher(ctx).matches();
    }

    /**
     * Verifies whether the string constitutes a valid numeric scalar (supports negative signs and decimals).
     *
     * @param ctx the input string to test
     * @return {@code true} if the input parses as a numeric value, otherwise {@code false}
     */
    public static boolean isOnlyNumbers(@Nullable String ctx) {
        return ctx != null && NUMBERS_PATTERN.matcher(ctx).matches();
    }

    /**
     * Verifies whether the string consists strictly of English letters and digits (a-z, A-Z, 0-9).
     *
     * @param ctx the input string to test
     * @return {@code true} if non-null and containing alphanumeric characters only, otherwise {@code false}
     */
    public static boolean isOnlyLettersAndDigits(@Nullable String ctx) {
        return ctx != null && LETTERS_AND_DIGITS_PATTERN.matcher(ctx).matches();
    }
}