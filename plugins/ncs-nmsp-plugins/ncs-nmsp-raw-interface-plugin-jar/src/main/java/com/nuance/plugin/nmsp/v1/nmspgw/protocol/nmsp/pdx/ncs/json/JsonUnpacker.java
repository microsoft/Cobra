/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved.
 * <p>
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 */

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.json;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs.PdxDictionary;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// Decodes a block of Json encoded datums
/// </summary>
/// 
public class JsonUnpacker {
    // / <summary>
    // / Unpacks a single JSON datum (string, List, Dictionary, number, byte[]) from the tip of the block
    // / </summary>
    // / <param name="buffer"></param>
    // / <returns></returns>
    public static Object unpack(String s) throws JsonException, JsonUnsupportedDataTypeException {
        if (s == null || s.length() == 0) {
            return null;
        }
        JsonStringReader sr = new JsonStringReader(s);
        Object rv = popDatum(sr);
        sr.close();
        return rv;
    }

    private static void eat(JsonStringReader sr) throws JsonException {
        while (true) {
            int v = sr.peek();
            if (v == ' ' || v == '\r' || v == '\t' || v == '\n') {
                sr.readChar();
            } else if (v == '#') {
                sr.readChar();
                while (true) {
                    v = sr.peek();
                    if (v == '\r' || v == '\n' || v == -1) {
                        break;
                    } else {
                        sr.readChar();
                    }
                }
                eat(sr);
                return;
            } else {
                return;
            }
        }
    }

    private static JsonTokenType peekToken(JsonStringReader sr) throws JsonException, JsonUnsupportedDataTypeException {
        eat(sr);

        int v = sr.peek();
        if (v == '{') {
            return JsonTokenType.OpenCurly;
        }
        if (v == '}') {
            return JsonTokenType.CloseCurly;
        }
        if (v == '[') {
            return JsonTokenType.OpenBlock;
        }
        if (v == ']') {
            return JsonTokenType.CloseBlock;
        }
        if (v == ':') {
            return JsonTokenType.PairSep;
        }
        if (v == ',') {
            return JsonTokenType.Sep;
        }
        if (v == '"') {
            return JsonTokenType.String;
        }
        if ((v >= 'a' && v <= 'z') || (v >= 'A' && v <= 'Z')) {
            return JsonTokenType.Const;
        }
        if (v == '-' || v == '0' || v == '1' || v == '2' || v == '3' || v == '4' || v == '5' || v == '6' || v == '7' || v == '8' || v == '9') {
            return JsonTokenType.Number;
        }

        throw new JsonUnsupportedDataTypeException(String.valueOf(v));
    }

    private static JsonToken popToken(JsonStringReader sr) throws JsonException, JsonUnsupportedDataTypeException {
        Object popped;
        popped = null;

        eat(sr);

        JsonTokenType tt = peekToken(sr);
        if (tt == JsonTokenType.OpenCurly || tt == JsonTokenType.CloseCurly || tt == JsonTokenType.OpenBlock || tt == JsonTokenType.CloseBlock || tt == JsonTokenType.Sep || tt == JsonTokenType.PairSep) {
            sr.readChar();
        } else if (tt == JsonTokenType.String) {
            popped = popString(sr);
        } else if (tt == JsonTokenType.Const) {
            popped = popConst(sr);
        } else if (tt == JsonTokenType.Number) {
            popped = popNumber(sr);
        } else {
            throw new JsonException();
        }

        return new JsonToken(tt, popped);
    }

    private static Object popDatum(JsonStringReader sr) throws JsonException, JsonUnsupportedDataTypeException {
        Object popped = null;

        eat(sr);

        JsonTokenType tt = peekToken(sr);
        if (tt == JsonTokenType.OpenCurly) {
            popped = popDictionary(sr);
        } else if (tt == JsonTokenType.OpenBlock) {
            popped = popList(sr);
        } else if (tt == JsonTokenType.String) {
            popped = popString(sr);
        } else if (tt == JsonTokenType.Const) {
            popped = popConst(sr);
            String s = popped.toString();

            // Translate built-in constants to check
            // NOTE: Only null, true or false values are supported for now
            if (s == null || (!(s.equalsIgnoreCase("null") || Boolean.TRUE.toString().equalsIgnoreCase(s) || Boolean.FALSE.toString().equalsIgnoreCase(s)))) {
                throw new JsonException("Unsupported JSON const value " + s);
            }
            if (Boolean.TRUE.toString().equalsIgnoreCase(s)) {
                popped = Boolean.TRUE;
            } else if (Boolean.FALSE.toString().equalsIgnoreCase(s)) {
                popped = Boolean.FALSE;
            } else {
                popped = null;
            }
        } else if (tt == JsonTokenType.Number) {
            popped = popNumber(sr);
        } else {
            throw new JsonException();
        }

        return popped;
    }

    private static Object popDictionary(JsonStringReader sr) throws JsonException, JsonUnsupportedDataTypeException {
        PdxDictionary dict = new PdxDictionary();

        Object popped = null;
        JsonToken token = popToken(sr);

        if (token.getType() != JsonTokenType.OpenCurly) {
            throw new JsonException("Expected open curly");
        }
        while (true) {
            token = popToken(sr);

            // handle empty dictionaries
            if (token.getType() == JsonTokenType.CloseCurly) {
                return dict;
            }

            if (token.getType() != JsonTokenType.String) {
                throw new JsonException("We only support string keys!");
            }

            String key = (String) token.getObject();

            token = popToken(sr);

            if (token.getType() != JsonTokenType.PairSep) {
                throw new JsonException("Invalid key/value pair");
            }

            popped = popDatum(sr);
            dict.put(key, popped);

            token = popToken(sr);

            if (token.getType() == JsonTokenType.CloseCurly) {
                return dict;
            }
            if (token.getType() != JsonTokenType.Sep) {
                throw new JsonException("Unexpected token parsing dictionary");
            }
        }
    }

    private static Object popList(JsonStringReader sr) throws JsonException, JsonUnsupportedDataTypeException {
        List<Object> list = new ArrayList<Object>();

        JsonToken token = popToken(sr);

        if (token.getType() != JsonTokenType.OpenBlock) {
            throw new JsonException("Expected open block");
        }
        while (true) {
            JsonTokenType tokenType = peekToken(sr);

            // handle empty lists
            if (tokenType == JsonTokenType.CloseBlock) {
                popToken(sr);
                return list;
            }

            if (tokenType == JsonTokenType.Sep) {
                throw new JsonException("Unexpected token parsing: No value");
            }

            Object popped = popDatum(sr);
            list.add(popped);

            token = popToken(sr);

            if (token.getType() == JsonTokenType.CloseBlock) {
                return list;
            }
            if (token.getType() != JsonTokenType.Sep) {
                throw new JsonException("Unexpected token parsing list: No seperator");
            }
        }
    }

    private static JsonConst popConst(JsonStringReader sr) throws JsonException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int v = sr.readChar();
            if (v == ' ' || v == '\t' || v == '\r' || v == '\n' || v == '}' || v == ']' || v == ',' || v == '"' || v == -1) {
                if (v != -1) {
                    sr.push(v);
                }
                return new JsonConst(sb.toString());
            } else {
                sb.append((char) v);
            }
        }
    }

    private static Object popString(JsonStringReader sr) throws JsonException {
        int v = sr.readChar();

        StringBuilder sb = new StringBuilder();

        if (v != '"') {
            throw new JsonException("Unexpected character");
        }
        while (true) {
            v = sr.readChar();
            if (v == -1) {
                throw new JsonException("Unexpected end of input");
            }
            if (v == '"') {
                return sb.toString();
            } else if (v == '\\') {
                v = sr.readChar();
                if (v == 'u') {
                    StringBuilder internalSb = new StringBuilder();
                    internalSb.append((char) sr.readChar());
                    internalSb.append((char) sr.readChar());
                    internalSb.append((char) sr.readChar());
                    internalSb.append((char) sr.readChar());
                    int v2 = Integer.parseInt(internalSb.toString(), 16);
                    sb.append((char) v2);
                } else if (v == 't') {
                    sb.append('\t');
                } else if (v == 'b') {
                    sb.append('\b');
                } else if (v == 'r') {
                    sb.append('\r');
                } else if (v == 'n') {
                    sb.append('\n');
                } else {
                    sb.append((char) v);
                }
            } else {
                sb.append((char) v);
            }
        }
    }

    private static Object popNumber(JsonStringReader sr) throws JsonException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int v = sr.readChar();
            if (v == -1) {
                throw new JsonException("Unexpected end of input");
            }
            if (v == ']' || v == '}' || v == ' ' || v == '\r' || v == '\t' || v == '\n' || v == ',') {
                sr.push(v);
                break;
            }
            sb.append((char) v);
        }

        final String s = sb.toString();
        Object result = null;
        // Take it easy and assume it is an integer, if not try as a double.
        try {
            result = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            // last ditch effort in case the integer is greater than Integer.MAXIMUM or smaller than Integer.MINIMUM
            result = Double.parseDouble(s);
        }
        return result;
    }
}
