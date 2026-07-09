package com.nntan041299.englishmasterservice.common.util;

public final class StringUtils {

    private StringUtils() {}

    public static String capitalizeFirst(String value) {
        if (value == null || value.isEmpty()) return value;
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
