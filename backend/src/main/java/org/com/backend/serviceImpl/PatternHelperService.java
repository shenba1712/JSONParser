package org.com.backend.serviceImpl;

import java.util.regex.Pattern;

public class PatternHelperService {

    public static String unicodePatternString() {
        return "(\\\\u[0-9a-fA-F]{4})";
    }

    public static Pattern getUnicodeEscapeSequencePattern() {
        return Pattern.compile(unicodePatternString());
    }

    public static String octalPatternString() {
        return "(\\\\o[0-7]{2})";
    }

    public static Pattern getOctalEscapeSequencePattern() {
        return Pattern.compile(octalPatternString());
    }

    public static String evenSlashesPatternString() {
        return "^[^\\\\]*((\\\\\\\\)+[^\\\\]*)$";
    }

    public static String oddSlashPatternString() {
        return "^[^\\\\]*((\\\\)+[^\\\\]*)$";
    }

    public static String doubleSlashesPatternString() {
        return "(\\\\\\\\)+";
    }

    public static boolean containsTabs(String value) {
        return value.contains("\t");
    }

    public static boolean containsLineBreaks(String value) {
        return value.contains(System.lineSeparator());
    }
}
