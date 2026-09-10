package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class PatternUtils {
    private PatternUtils() {}

    private static final Pattern DIGITS_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern LETTERS_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern LETTERS_AND_DIGITS_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    /**
     * Kiểm tra chuỗi chỉ chứa các chữ số thuần (0-9).
     */
    public static boolean isOnlyDigits(@Nullable String ctx) {
        return ctx != null && DIGITS_PATTERN.matcher(ctx).matches();
    }

    /**
     * Kiểm tra chuỗi chỉ chứa chữ cái tiếng Anh (a-z, A-Z).
     */
    public static boolean isOnlyLetters(@Nullable String ctx) {
        return ctx != null && LETTERS_PATTERN.matcher(ctx).matches();
    }

    /**
     * Kiểm tra chuỗi có phải là một số hợp lệ (hỗ trợ số nguyên, số âm, số thập phân như: 123, -45, 3.14).
     */
    public static boolean isOnlyNumbers(@Nullable String ctx) {
        return ctx != null && NUMBERS_PATTERN.matcher(ctx).matches();
    }

    /**
     * Kiểm tra chuỗi chỉ chứa chữ cái tiếng Anh và chữ số (a-z, A-Z, 0-9).
     */
    public static boolean isOnlyLettersAndDigits(@Nullable String ctx) {
        return ctx != null && LETTERS_AND_DIGITS_PATTERN.matcher(ctx).matches();
    }
}