/**
 * Copyright 2006-2011, Nuance Communications Inc. All rights reserved.
 * <p>
 * Nuance Communications, Inc. provides this document without representation
 * or warranty of any kind. The information in this document is subject to
 * change without notice and does not represent a commitment by Nuance
 * Communications, Inc. The software and/or databases described in this
 * document are furnished under a license agreement and may be used or
 * copied only in accordance with the terms of such license agreement.
 * Without limiting the rights under copyright reserved herein, and except
 * as permitted by such license agreement, no part of this document may be
 * reproduced or transmitted in any form or by any means, including, without
 * limitation, electronic, mechanical, photocopying, recording, or otherwise,
 * or transferred to information storage and retrieval systems, without the
 * prior written permission of Nuance Communications, Inc.
 * <p>
 * Nuance, the Nuance logo, Nuance Recognizer, and Nuance Vocalizer are
 * trademarks or registered trademarks of Nuance Communications, Inc. or its
 * affiliates in the United States and/or other countries. All other
 * trademarks referenced herein are the property of their respective owners.
 **/

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * PDX dictionary representation
 *
 */
public class PDXDictionary extends PDXClass implements Dictionary {
    private static final Logger logger = LogManager.getLogger();
    private Hashtable dictionary;

    public PDXDictionary() {
        super(PDXClass.DICT);
        dictionary = new Hashtable();
    }

    public PDXDictionary(byte[] message, boolean isMessage) {
        super(PDXClass.DICT);
        dictionary = new Hashtable();
        if (message.length > 0) {
            int classType = message[0] & 0xFF;
            if (classType != PDXClass.DICT) {
                logger.error("PDXDictionary() Expected a dictionary.");
                return;
            }

            int length = getLength(message, 1);

            byte[] dictionaryContent = new byte[length];
            System.arraycopy(message, message.length - length, dictionaryContent, 0, dictionaryContent.length);
            setContent(dictionaryContent);
        }
    }

    public PDXDictionary(byte[] byteDictionary) {
        super(PDXClass.DICT);
        dictionary = new Hashtable();
        setContent(byteDictionary);
    }

    public void put(String name, String value, short type) {
        switch (type) {
            case PDXClass.UTF8:
                dictionary.put(name, new PDXUTF8String(value));
                break;
            case PDXClass.ASCII:
                dictionary.put(name, new PDXAsciiString(value));
                break;
            default:
                logger.error("PDXDictionary.put() type should be UTF8 or ASCII. ");
                break;
        }
    }

    protected void put(String name, boolean value, short type) {
        if (type == PDXClass.BOOLEAN) {
            dictionary.put(name, new PDXBoolean(value));
        } else {
            logger.error("PDXDictionary.put() type should be BOOLEAN. ");
        }
    }

    protected void put(String name, double value, short type) {
        if (type == PDXClass.DOUBLE) {
            dictionary.put(name, new PDXDouble(value));
        } else {
            logger.error("PDXDictionary.put() type should be DOUBLE. ");
        }
    }

    protected void put(String name, int value, short type) {
        if (type == PDXClass.INT) {
            dictionary.put(name, new PDXInteger(value));
        }
    }

    protected void put(String name, byte[] value, short type) {
        switch (type) {
            case PDXClass.BOOLEAN:
                dictionary.put(name, new PDXBoolean(value));
                break;
            case PDXClass.DOUBLE:
                dictionary.put(name, new PDXDouble(value));
                break;
            case PDXClass.INT:
                dictionary.put(name, new PDXInteger(value));
                break;
            case PDXClass.BYTES:
                dictionary.put(name, new PDXByteString(value));
                break;
            case PDXClass.UTF8:
                dictionary.put(name, new PDXUTF8String(value));
                break;
            case PDXClass.ASCII:
                dictionary.put(name, new PDXAsciiString(value));
                break;
            case PDXClass.NULL:
                dictionary.put(name, new PDXNull());
                break;
            case PDXClass.DICT:
                dictionary.put(name, new PDXDictionary(value));
                break;
            case PDXClass.SEQ:
                dictionary.put(name, new PDXSequence(value));
                break;
            default:
                break;
        }
    }

    /**
     * Retrieves an enumeration of the keys in this dictionary.
     *
     *
     * @return Enumeration
     */
    public Enumeration keys() {
        return dictionary.keys();
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new NullPointerException("PDXDictionary.containsKey key is null");
        }
        return dictionary.containsKey(key);
    }

    public PDXClass getClass(String key) {
        return (PDXClass) dictionary.get(key);
    }

    public short getType(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        return pdxClass.getType();
    }

    /**
     * Retrieves a dictionary boolean element.
     *
     * @param key
     *            element name
     *
     * @throws RuntimeException
     *
     * @return boolean
     */
    public boolean getBoolean(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        if (pdxClass == null) {
            throw new RuntimeException(Dictionary.KEY_DOES_NOT_EXIST);
        }
        if (pdxClass.getType() != PDXClass.BOOLEAN) {
            throw new RuntimeException(Dictionary.KEY_IS_OF_WRONG_TYPE);
        }

        return ((PDXBoolean) pdxClass).getValue();
    }

    /**
     * Retrieves a dictionary double element.
     *
     * @param key
     *            element name
     *
     * @throws RuntimeException
     *
     * @return double
     *
     */
    public double getDouble(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        if (pdxClass == null) {
            throw new RuntimeException("Key " + key + " not found.");
        }
        if (pdxClass.getType() != PDXClass.DOUBLE) {
            throw new RuntimeException("Key " + key + " is not of type boolean");
        }

        return ((PDXDouble) pdxClass).getValue();
    }

    /**
     * Retrieves a dictionary integer element.
     *
     * @param key
     *            element name
     *
     * @throws RuntimeException
     *
     * @return int
     *
     */
    public int getInteger(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        if (pdxClass == null) {
            throw new RuntimeException("Key " + key + " not found.");
        }
        if (pdxClass.getType() != PDXClass.INT) {
            throw new RuntimeException(Dictionary.KEY_IS_OF_WRONG_TYPE);
        }

        return ((PDXInteger) pdxClass).getValue();
    }

    /**
     * Retrieves a dictionary byte string element.
     *
     * @param key
     *            element name
     *
     * @throws RuntimeException
     *
     * @return byte[]
     */
    public byte[] getByteString(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        if (pdxClass == null) {
            throw new RuntimeException("Key " + key + " not found.");
        }
        if (pdxClass.getType() != PDXClass.BYTES) {
            throw new RuntimeException(Dictionary.KEY_IS_OF_WRONG_TYPE);
        }

        return ((PDXByteString) pdxClass).getValue();
    }

    /**
     * Retrieves a dictionary UTF8 String element.
     *
     * @param key
     *            element name
     *
     * @throws RuntimeException
     *
     * @return String
     */
    public String getUTF8String(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        if (pdxClass == null) {
            throw new RuntimeException("Key " + key + " not found.");
        }
        if (pdxClass.getType() != PDXClass.UTF8) {
            throw new RuntimeException(Dictionary.KEY_IS_OF_WRONG_TYPE);
        }

        return ((PDXUTF8String) pdxClass).getValue();
    }

    /**
     * Retrieves a dictionary ASCII String element.
     *
     * @param key
     *            element name
     *
     * @throws RuntimeException
     *
     * @return String
     */
    public String getAsciiString(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        if (pdxClass == null) {
            throw new RuntimeException("Key " + key + " not found.");
        }
        if (pdxClass.getType() != PDXClass.ASCII) {
            throw new RuntimeException(Dictionary.KEY_IS_OF_WRONG_TYPE);
        }

        return ((PDXAsciiString) pdxClass).getValue();
    }

    /**
     * Retrieves a dictionary Dictionary element.
     *
     * @param key
     *            element name
     *
     * @throws RuntimeException
     *
     * @return Dictionary
     */
    public Dictionary getDictionary(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        if (pdxClass == null) {
            throw new RuntimeException("Key " + key + " not found.");
        }
        if (pdxClass.getType() != PDXClass.DICT) {
            throw new RuntimeException(Dictionary.KEY_IS_OF_WRONG_TYPE);
        }

        return ((PDXDictionary) pdxClass);
    }

    /**
     * Retrieves a dictionary Sequence element.
     *
     * @param key
     *            element name
     *
     * @throws RuntimeException
     *
     * @return Sequence
     */
    public Sequence getSequence(String key) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXClass pdxClass = (PDXClass) dictionary.get(key);
        if (pdxClass == null) {
            throw new RuntimeException("Key " + key + " not found.");
        }
        if (pdxClass.getType() != PDXClass.SEQ) {
            throw new RuntimeException(Dictionary.KEY_IS_OF_WRONG_TYPE);
        }

        return ((PDXSequence) pdxClass);
    }

    public void addAsciiString(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key is : " + key + " value is : " + value);
        }
        PDXAsciiString string = new PDXAsciiString(value);
        dictionary.put(key, string);
    }

    public void addByteString(String key, byte[] value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key is : " + key + " value is : " + value);
        }
        PDXByteString string = new PDXByteString(value);
        dictionary.put(key, string);
    }

    public void addDictionary(String key, Dictionary value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key is : " + key + " value is : " + value);
        }
        if (((PDXClass) value).getType() != PDXClass.DICT) {
            throw new IllegalArgumentException("value is not a valid dictionary. ");
        }
        dictionary.put(key, value);
    }

    public void addBoolean(String key, boolean value) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXBoolean booleanValue = new PDXBoolean(value);
        dictionary.put(key, booleanValue);
    }

    public void addDouble(String key, double value) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXDouble doubleValue = new PDXDouble(value);
        dictionary.put(key, doubleValue);
    }


    public void addInteger(String key, int value) {
        if (key == null) throw new IllegalArgumentException("key is null.");
        PDXInteger integer = new PDXInteger(value);
        dictionary.put(key, integer);
    }

    public void addSequence(String key, Sequence value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key is : " + key + " value is : " + value);
        }
        if (((PDXClass) value).getType() != PDXClass.SEQ) {
            throw new IllegalArgumentException("value is not a valid sequence. ");
        }
        dictionary.put(key, value);
    }

    public void addUTF8String(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("key is : " + key + " value is : " + value);
        }
        PDXUTF8String string = new PDXUTF8String(value);
        dictionary.put(key, string);
    }

    public byte[] getContent() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Enumeration names = dictionary.keys();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            PDXAsciiString pdxKey = new PDXAsciiString(name);
            try {
                output.write(pdxKey.toByteArray());
                PDXClass pdxValue = (PDXClass) dictionary.get(name);
                switch (pdxValue.getType()) {
                    case PDXClass.BOOLEAN:
                        output.write(((PDXBoolean) pdxValue).toByteArray());
                        break;
                    case PDXClass.DOUBLE:
                        output.write(((PDXDouble) pdxValue).toByteArray());
                        break;
                    case PDXClass.INT:
                        output.write(((PDXInteger) pdxValue).toByteArray());
                        break;
                    case PDXClass.BYTES:
                        output.write(((PDXByteString) pdxValue).toByteArray());
                        break;
                    case PDXClass.UTF8:
                        output.write(((PDXUTF8String) pdxValue).toByteArray());
                        break;
                    case PDXClass.ASCII:
                        output.write(((PDXAsciiString) pdxValue).toByteArray());
                        break;
                    case PDXClass.NULL:
                        output.write(((PDXNull) pdxValue).toByteArray());
                        break;
                    case PDXClass.DICT:
                        output.write(((PDXDictionary) pdxValue).toByteArray());
                        break;
                    case PDXClass.SEQ:
                        output.write(((PDXSequence) pdxValue).toByteArray());
                        break;
                }
            } catch (java.io.IOException ioe) {
                //#ifdef NMSP_LOG
//@                log.error("PDXDictionary.getContent() " + ioe.toString() + ". ");
                //#endif
            }
        }
        return output.toByteArray();
    }

    private void setContent(byte[] byteDictionary) {
        int index = 0;
        int length;
        int classType;
        byte[] classContent;

        while (index < byteDictionary.length) {
            String name;
            // The key is always an ASCII string
            classType = byteDictionary[index++] & 0xFF;
            if (classType != PDXClass.ASCII) {
                //#ifdef NMSP_LOG
//@                log.error("PDXDictionary.setContent() Expected an ASCII string but got " + classType + ". ");
                //#endif
                return;
            }

            length = getLength(byteDictionary, index);
            index += getLengthSize(length);

            classContent = new byte[length];
            System.arraycopy(byteDictionary, index, classContent, 0, classContent.length);
            index += length;

            name = new String(classContent);

            // Get the value class type
            classType = byteDictionary[index++] & 0xFF;
            length = getLength(byteDictionary, index);
            index += getLengthSize(length);

            classContent = new byte[length];
            System.arraycopy(byteDictionary, index, classContent, 0, classContent.length);
            index += length;

            // Build the dictionary by adding a key/value
            switch (classType) {
                case PDXClass.BOOLEAN:
                    dictionary.put(name, new PDXBoolean(classContent));
                    break;
                case PDXClass.DOUBLE:
                    dictionary.put(name, new PDXDouble(classContent));
                    break;
                case PDXClass.INT:
                    dictionary.put(name, new PDXInteger(classContent));
                    break;
                case PDXClass.BYTES:
                    dictionary.put(name, new PDXByteString(classContent));
                    break;
                case PDXClass.UTF8:
                    dictionary.put(name, new PDXUTF8String(classContent));
                    break;
                case PDXClass.ASCII:
                    dictionary.put(name, new PDXAsciiString(classContent));
                    break;
                case PDXClass.NULL:
                    dictionary.put(name, new PDXNull());
                    break;
                case PDXClass.DICT:
                    dictionary.put(name, new PDXDictionary(classContent));
                    break;
                case PDXClass.SEQ:
                    dictionary.put(name, new PDXSequence(classContent));
                    break;
                default:
                    //#ifdef NMSP_LOG
//@                log.error("PDXDictionary.setContent() Unknown PDXClass type: " + classType + ". ");
                    //#endif
                    break;
            }
        }
    }

    public byte[] toByteArray() {
        //#ifdef NMSP_LOG2
//@        log.debug("PDXDictionary.toByteArray()");
        //#endif
        return super.toByteArray(getContent());
    }

    public String toString() {
        return this.toString(0);
    }

    protected String toString(int indentLevel) {
        StringBuilder stringDictionary = new StringBuilder();
        StringBuilder baseIndent = new StringBuilder();
        String indent = "";
        for (int i = 0; i < indentLevel - 1; i++) {
            baseIndent.append("    ");
        }
        if (indentLevel > 0) {
            indent = baseIndent + "    ";
        }

        Enumeration keys = dictionary.keys();
        if (indentLevel != 0) {
            stringDictionary.append("{ \n");
        }
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            PDXClass value = (PDXClass) dictionary.get(key);
            switch (value.getType()) {
                case PDXClass.BOOLEAN:
                    stringDictionary.append(indent).append(key).append(": <BOOLEAN> ").append(((PDXBoolean) value).getValue()).append("\n");
                    break;
                case PDXClass.DOUBLE:
                    stringDictionary.append(indent).append(key).append(": <DOUBLE> ").append(((PDXDouble) value).getValue()).append("\n");
                    break;
                case PDXClass.INT:
                    stringDictionary.append(indent).append(key).append(": <INT> ").append(((PDXInteger) value).getValue()).append("\n");
                    break;
                case PDXClass.BYTES:
                    stringDictionary.append(indent).append(key).append(": <BYTES> \"").append(((PDXByteString) value).getValue()).append("\"\n");
                    break;
                case PDXClass.UTF8:
                    stringDictionary.append(indent).append(key).append(": <UTF8> \"").append(((PDXUTF8String) value).getValue()).append("\"\n");
                    break;
                case PDXClass.ASCII:
                    stringDictionary.append(indent).append(key).append(": <ASCII> \"").append(((PDXAsciiString) value).getValue()).append("\"\n");
                    break;
                case PDXClass.NULL:
                    stringDictionary.append(indent).append(key).append(": <NULL> \n");
                    break;
                case PDXClass.DICT:
                    stringDictionary.append(indent).append(key).append(": ").append(((PDXDictionary) value).toString(indentLevel + 1)).append("\n");
                    break;
                case PDXClass.SEQ:
                    stringDictionary.append(indent).append(key).append(": ").append(((PDXSequence) value).toString(indentLevel + 1)).append("\n");
                    break;
            }
        }
        if (indentLevel != 0) {
            stringDictionary.append(baseIndent).append("} ");
        }
        //#ifdef NMSP_LOG2
//@        log.debug("PDXDictionary.toString()\n" + stringDictionary);
        //#endif
        return stringDictionary.toString();
    }
}
