package org.com.backend.model;

import java.util.Arrays;
import java.util.regex.Pattern;

public enum EscapeSequence {
    NEWLINE("\\\\n"),
    TAB("\\\\t"),
    BACKSPACE("\\\\b"),
    CARRIAGE_RETURN("\\\\r"),
    FORM_FEED("\\\\f"),
    DOUBLE_QUOTE("\\\\\""),
    BACKSLASH("\\\\\\\\"),
    FORWARDSLASH("\\\\/");

    public final String label;

    EscapeSequence(String label) {
        this.label = label;
    }

    public static String getEscapeSequencesAsString() {
        return String.join("|", Arrays.stream(EscapeSequence.values()).map(EscapeSequence::toString).toList());
    }

    @Override
    public String toString() {
        return this.label;
    }
}
