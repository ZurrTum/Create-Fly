package com.zurrtum.create.client.flywheel.lib.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StringUtil {
    private static final NumberFormat THREE_DECIMAL_PLACES = new DecimalFormat("#0.000");

    private StringUtil() {
    }

    public static int countLines(String s) {
        int lines = 1;
        int length = s.length();

        for (int i = 0; i < length; ++i) {
            char c = s.charAt(i);
            if (c == '\n') {
                ++lines;
            } else if (c == '\r') {
                ++lines;
                if (i + 1 < length && s.charAt(i + 1) == '\n') {
                    ++i;
                }
            }
        }

        return lines;
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        } else if (bytes < 1048576L) {
            return THREE_DECIMAL_PLACES.format((double) ((float) bytes / 1024.0F)) + " KiB";
        } else {
            return bytes < 1073741824L ? THREE_DECIMAL_PLACES.format((double) ((float) bytes / 1024.0F / 1024.0F)) + " MiB" : THREE_DECIMAL_PLACES.format(
                (double) ((float) bytes / 1024.0F / 1024.0F / 1024.0F)) + " GiB";
        }
    }

    public static String formatTime(long ns) {
        if (ns < 1000L) {
            return ns + " ns";
        } else if (ns < 1000000L) {
            return THREE_DECIMAL_PLACES.format((double) ((float) ns / 1000.0F)) + " μs";
        } else {
            return ns < 1000000000L ? THREE_DECIMAL_PLACES.format((double) ((float) ns / 1000000.0F)) + " ms" : THREE_DECIMAL_PLACES.format((double) ((float) ns / 1.0E9F)) + " s";
        }
    }

    public static String formatAddress(long address) {
        return "0x" + Long.toHexString(address);
    }

    public static String trimPrefix(String s, String prefix) {
        return s.startsWith(prefix) ? s.substring(prefix.length()) : s;
    }

    public static String trimSuffix(String s, String prefix) {
        return s.endsWith(prefix) ? s.substring(0, s.length() - prefix.length()) : s;
    }

    public static String indent(String str, int n) {
        if (str.isEmpty()) {
            return "";
        } else {
            Stream<String> stream = str.lines();
            if (n > 0) {
                String spaces = repeatChar(' ', n);
                stream = stream.map((s) -> spaces + s);
            } else if (n == Integer.MIN_VALUE) {
                stream = stream.map(String::stripLeading);
            } else if (n < 0) {
                throw new IllegalArgumentException("Requested indentation (" + n + ") is unsupported");
            }

            return (String) stream.collect(Collectors.joining("\n"));
        }
    }

    public static String repeatChar(char c, int n) {
        char[] arr = new char[n];
        Arrays.fill(arr, c);
        return new String(arr);
    }
}
