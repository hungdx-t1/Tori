package com.dianxin.tori.base.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Random;

/**
 * Utility class for color operations, hex transformations, and randomized palettes.
 */
@SuppressWarnings("unused")
public final class ColorUtils {
    private static final Random RANDOM = new Random();

    private ColorUtils() {
        throw new AssertionError("Utility class cannot be instantiated.");
    }

    /**
     * Generates a completely randomized RGB {@link Color}.
     *
     * @return a random color instance
     */
    @NotNull
    public static Color random() {
        return new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    /**
     * Generates a randomized uppercase Hex string representation (e.g., "#FFAABB").
     *
     * @return a 7-character hex color string
     */
    @NotNull
    public static String randomHexString() {
        Color color = random();
        return toHexString(color);
    }

    /**
     * Generates a randomized color bounded between two hexadecimal thresholds per channel.
     * Useful for constrained palettes and gradient stepping.
     *
     * @param minHex the starting hex string (e.g., "#000000" or "000000")
     * @param maxHex the ending hex string (e.g., "#101010" or "101010")
     * @return a randomized color bounded within the designated range
     * @throws IllegalArgumentException if the hex string format or length is invalid
     */
    @NotNull
    public static Color randomColorRestricted(@NotNull String minHex, @NotNull String maxHex) {
        String cleanMin = minHex.startsWith("#") ? minHex.substring(1) : minHex;
        String cleanMax = maxHex.startsWith("#") ? maxHex.substring(1) : maxHex;

        if (cleanMin.length() != 6 || cleanMax.length() != 6) {
            throw new IllegalArgumentException("Hex color code must contain 6 hexadecimal characters. Found: min=" + minHex + ", max=" + maxHex);
        }

        try {
            int minR = Integer.parseInt(cleanMin.substring(0, 2), 16);
            int minG = Integer.parseInt(cleanMin.substring(2, 4), 16);
            int minB = Integer.parseInt(cleanMin.substring(4, 6), 16);

            int maxR = Integer.parseInt(cleanMax.substring(0, 2), 16);
            int maxG = Integer.parseInt(cleanMax.substring(2, 4), 16);
            int maxB = Integer.parseInt(cleanMax.substring(4, 6), 16);

            int r = randomInRange(Math.min(minR, maxR), Math.max(minR, maxR));
            int g = randomInRange(Math.min(minG, maxG), Math.max(minG, maxG));
            int b = randomInRange(Math.min(minB, maxB), Math.max(minB, maxB));

            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Hex string contains invalid characters.", e);
        }
    }

    /**
     * Converts a {@link Color} object into an uppercase Hex string (#RRGGBB).
     *
     * @param color the color to convert
     * @return formatted hex color string
     */
    @NotNull
    public static String toHexString(@NotNull Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Parses a hexadecimal string into a {@link Color} object.
     *
     * @param hexStr the hex string (e.g., "#FF0000" or "FF0000")
     * @return the resolved {@link Color}
     * @throws IllegalArgumentException if the string is null, empty, or improperly formatted
     */
    @NotNull
    public static Color fromHex(@Nullable String hexStr) {
        if (hexStr == null || hexStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Hex color string cannot be null or empty.");
        }

        String cleanHex = hexStr.trim();
        if (cleanHex.startsWith("#")) {
            cleanHex = cleanHex.substring(1);
        }

        if (cleanHex.length() != 6) {
            throw new IllegalArgumentException("Hex color code must contain 6 hexadecimal characters. Found: " + hexStr);
        }

        try {
            return new Color(Integer.parseInt(cleanHex, 16));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Hex string contains non-hexadecimal characters: " + hexStr, e);
        }
    }

    private static int randomInRange(int min, int max) {
        if (min == max) return min;
        return RANDOM.nextInt((max - min) + 1) + min;
    }
}