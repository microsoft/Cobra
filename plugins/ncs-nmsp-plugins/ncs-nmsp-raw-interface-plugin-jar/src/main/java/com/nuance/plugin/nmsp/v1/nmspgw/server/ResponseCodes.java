package com.nuance.plugin.nmsp.v1.nmspgw.server;

import java.util.Arrays;

public class ResponseCodes {
    public enum StatusCode {
        SUCCESS(200),
        PARTIAL_SUCCESS(201),
        METHOD_NOT_ALLOWED(401),
        METHOD_NOT_VALID_IN_THIS_STATE(402),
        UNSUPPORTED_PARAMETER(403),
        ILLEGAL_VALUE_FOR_PARAMETER(404),
        NOT_FOUND(405),
        MANDATORY_PARAMETER_MISSING(406),
        OPERATION_FAILED(407),
        UNSUPPORTED_MESSAGE(408),
        UNSUPPORTED_PARAMETER_VALUE(409);

        private final int code;

        StatusCode(int c) {
            code = c;
        }

        public static StatusCode fromValue(int value) throws IllegalArgumentException {
            for (StatusCode status : StatusCode.values()) {
                if (status.getCode() == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status code: " + value);
        }

        public int getCode() {
            return code;
        }
    }

    public enum RequestState {
        COMPLETE(0),
        IN_PROGRESS(1),
        PENDING(2);

        private final int state;

        RequestState(int s) {
            state = s;
        }

        public static int fromString(String state) {
            if (state.equalsIgnoreCase("COMPLETE")) {
                return RequestState.COMPLETE.getState();
            } else if (state.equalsIgnoreCase("IN-PROGRESS") || state.equalsIgnoreCase("IN_PROGRESS")) {
                return RequestState.IN_PROGRESS.getState();
            } else if (state.equalsIgnoreCase("PENDING")) {
                return RequestState.PENDING.getState();
            } else {
                return -1;
            }
        }

        public int getState() {
            return state;
        }

        public String toString() {
            if (state == COMPLETE.state) {
                return "COMPLETE";
            } else if (state == IN_PROGRESS.state) {
                return "IN-PROGRESS";
            } else {
                return "PENDING";
            }
        }
    }

    public enum CompletionCause {
        SUCCESS(0),
        BARGEIN(1),
        PARSE_FAILURE(2),
        TTS_URI_FAILURE(3),
        TTS_ERROR(4),
        TTS_LANGUAGE_NOT_SUPPORTED(5),
        LEXICON_LOAD_FAILURE(6),
        TTS_RESOURCE_UNAVAILABLE(7),
        TTS_TEXT_TOO_LONG(8),
        NOMATCH(1),
        NOINPUT(2),
        RECOGNITION_TIMEOUT(3),
        GRAMMAR_LOAD_FAILURE(4),
        GRAMMAR_COMPILATION_FAILURE(5),
        REC_ERROR(6),
        SPEECH_TOO_EARLY(7),
        TOO_MUCH_SPEECH(8),
        REC_URI_FAILURE(9),
        REC_LANGUAGE_NOT_SUPPORTED(10),
        CANCELLED(11),
        SEMANTIC_FAILURE(12),
        NO_GRAMMAR_DEFINED(13),
        REC_RESOURCE_NOT_AVAILABLE(14),
        NO_MATCH_MAXTIME(15),
        GRAMMAR_DEFINITION_FAILURE(16),
        INVALID_REQUEST(600),
        RESOURCE_UNAVAILABLE(601),
        LOAD_RESOURCE_UNAVAILABLE(1),
        LOAD_RESOURCE_FAILED(2),
        LOAD_RESOURCE_INVALID_REQUEST(3),
        AR_ERROR(4),
        TTS_BPS_MUST_BE_USED_WITH_OPUS_CODEC(302),
        TTS_BPS_NOT_WITHIN_LIMITS(303),
        TTS_BPS_INVALID_FORMAT(304),
        INVALID_REQUEST_PARAMETER(305),
        INVALID_REQUEST_INPUT(306),

        METHOD_NOT_ALLOWED(401),
        METHOD_NOT_FOUND(404),
        OPERATION_FAILED(407),
        UNSUPPORTED_MESSAGE(408),
        UNSUPPORTED_PARAMETER_VALUE(409),
        PROFILE_NOT_FOUND(481),
        PROFILE_GET_ERROR(482),
        AUTHORIZE_ERROR_NMAID_INVALID(503),
        AUTHORIZE_ERROR_INVALID_DICT_TOPIC(507),
        AUTHORIZE_ERROR_INVALID_DICT_LANG(508),
        AUTHORIZE_ERROR_INVALID_DICT_LANG_TOPIC(509),
        AUTHORIZE_ERROR_INVALID_TTS_LANG(510),
        AUTHORIZE_ERROR_INVALID_TTS_VOICE(511),
        AUTHORIZE_ERROR_INVALID_UTTERRANCE_TYPE(512),

        UNSUPPORTED_CLIENT_VERSION(513);

        private final int cause;

        CompletionCause(int c) {
            cause = c;
        }

        public static boolean contains(int externalErrorCode) {
            return Arrays.stream(values()).anyMatch(completionCause -> completionCause.getCause() == externalErrorCode);
        }

        public int getCause() {
            return cause;
        }
    }

    public enum ResultType {
        NSS((short) 0), APP_SERVER((short) 1), MRCP_HEADERS((short) 2);

        private short value = 0;

        ResultType(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }
    }
}
