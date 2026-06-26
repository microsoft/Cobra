package com.nuance.plugin.v1.shareable;


import javax.annotation.Nullable;

public interface IErrorCode {

    /**
     * Based on NAGIOS severity (according to previous comments). Needs to verify:
     * - is this comment is still relevant..
     * - ERROR = CRITICAL?
     * - Is this the best definition or reference  for our errors severities?
     *
     * Brainstorming:
     * OK... well not an error in the first place :-) Remove? Could be changed to INFO? Use to leverage
     *   the (minimal) error capabilities.
     * WARNING: could cause future problems
     * ERROR: causes a request/transaction to fail.
     * UNKNOWN: is this useful or does it just bring confusion?
     *
     */
    enum Severity {

        OK(0),
        WARNING(1),
        ERROR(2),
        UNKNOWN(3);

        Severity(int value) {
            this.value = value;
        }

        public int getIntValue() {
            return value;
        }

        private final int value;
    }

    /**
     * Returns the numerical error code.
     * @return Error code.
     */
    int getValue();

    /**
     * External message, will be used in external responses. Can include formatters.
     * @return message
     */
    String getMessage();

    /**
     * Returns the documentation for a error code. Returns null by default.
     */
    default @Nullable  String getDocumentation() { return null; }

    /**
     * Returns the NOC recommendation for a error code. Returns null by default.
     */
    default @Nullable  String getNOCRecommendation() { return null; }

    /**
     * Returns the NOC troubleshooting tips for a error code. Returns null by default.
     */
    default @Nullable  String getTroubleshoot() { return null; }

    /**
     * Returns the version of webapi when the error code was introduced. Returns null by default.
     */
    default @Nullable  String getVersion() { return null; }

    /**
     * Returns the area which might be causing the error. Returns null by default.
     */
    default @Nullable  String getAreaOfIssue() { return null; }

    /**
     * Returns the error severity. See severity enum above. Numerical values used here for backward compatibility.
     * @return int value for severity.
     */
    int getSeverity();

    /**
     * Internal error message, used to format messages in logs, can include format specifiers (e.g. %)
     * @return message
     */
    String getInternalErrorMessage();

    /**
     *
     * @return HTTP status code corresponding to this error, or 0 if no mapping exists.
     */
    default int getHttpStatusCode()  { return 0; }


    /**
     * Return the name of the error. Will return null by default to preserve backward compatibility.
     * @return Enum value name for this error code.
     */
    default String getEnumValueName() {
        return null;
    }

    /**
     * Return whether an error is a potential security threat. Will return false by default to preserve backward compatibility.
     * @return Boolean value.
     */
    default boolean isPotentialSecurityThreat() {
        return false;
    }


    /**
     * Formats the internal message. If the internal message format is null, external message will be used as a fallback.
     * Catches the error if the format does not match the provided arguments.
     *
     * @param args Arguments
     * @return formatted message.
     */
    static String formatInternalMessage(IErrorCode errorCode, Object... args) {
        final String FORMATTING_FAILED_MESSAGE = "[Message formatting failed, defaulting to external message. Check varargs.]";

        String internalMessageFormat = errorCode.getInternalErrorMessage();
        if (internalMessageFormat == null) {
            internalMessageFormat = errorCode.getMessage();
            if (args.length > 0 && !internalMessageFormat.contains("%")) {
                // If at least one argument was passed but no formatter found in the string, generates a format string based on the external message.
                internalMessageFormat = internalMessageFormat.replaceAll("[.]$", "");
                internalMessageFormat += ": %s";
            }
        }

        try {
            return String.format(internalMessageFormat, args);
        }
        catch (IllegalArgumentException e) {
            return errorCode.getMessage() + " " + FORMATTING_FAILED_MESSAGE;
        }
    }
}
