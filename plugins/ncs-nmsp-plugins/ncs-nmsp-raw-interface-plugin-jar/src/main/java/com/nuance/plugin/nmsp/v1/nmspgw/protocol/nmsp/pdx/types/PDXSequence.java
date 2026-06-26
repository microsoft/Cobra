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

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * PDX sequence representation
 */
public class PDXSequence extends PDXClass implements Sequence {
    private final Vector sequence;

    public PDXSequence() {
        super(PDXClass.SEQ);
        sequence = new Vector();
    }

    public PDXSequence(byte[] byteSequence) {
        super(PDXClass.SEQ);
        sequence = new Vector();
        int index = 0;
        int classType;
        int length;
        byte[] classContent;
        while (index < byteSequence.length) {
            classType = byteSequence[index++] & 0xFF;
            length = getLength(byteSequence, index);
            index += getLengthSize(length);
            classContent = new byte[length];
            System.arraycopy(byteSequence, index, classContent, 0, classContent.length);
            index += length;
            switch (classType) {
                case PDXClass.BOOLEAN:
                    sequence.addElement(new PDXBoolean(classContent));
                    break;
                case PDXClass.DOUBLE:
                    sequence.addElement(new PDXDouble(classContent));
                    break;
                case PDXClass.INT:
                    sequence.addElement(new PDXInteger(classContent));
                    break;
                case PDXClass.BYTES:
                    sequence.addElement(new PDXByteString(classContent));
                    break;
                case PDXClass.UTF8:
                    sequence.addElement(new PDXUTF8String(classContent));
                    break;
                case PDXClass.ASCII:
                    sequence.addElement(new PDXAsciiString(classContent));
                    break;
                case PDXClass.NULL:
                    sequence.addElement(new PDXNull());
                    break;
                case PDXClass.DICT:
                    sequence.addElement(new PDXDictionary(classContent));
                    break;
                case PDXClass.SEQ:
                    sequence.addElement(new PDXSequence(classContent));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Retrieves the number of elements in a sequence.
     *
     * @return int
     */
    public int size() {
        return sequence.size();
    }
    /**
     * Retrieves an Enumeration of elements in a sequence.
     *
     * @return Enumeration
     */
    /**
     * Adds a sequence element.
     *
     * @param pdxClass element
     * @return
     */
    public short getType(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        return pdxClass.getType();
    }

    /**
     * Retrieves a sequence boolean element.
     *
     * @param index element index
     * @return boolean
     * @throws IndexOutOfBoundsException, RuntimeException
     */
    public boolean getBoolean(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        if (pdxClass.getType() != PDXClass.BOOLEAN) {
            throw new RuntimeException(Sequence.INDEX_IS_OF_WRONG_TYPE);
        }
        return ((PDXBoolean) pdxClass).getValue();
    }

    /**
     * Retrieves a sequence double element.
     *
     * @param index element index
     * @return double
     * @throws IndexOutOfBoundsException, RuntimeException
     */
    public double getDouble(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        if (pdxClass.getType() != PDXClass.DOUBLE) {
            throw new RuntimeException(Sequence.INDEX_IS_OF_WRONG_TYPE);
        }
        return ((PDXDouble) pdxClass).getValue();
    }

    /**
     * Retrieves a sequence integer element.
     *
     * @param index element index
     * @return int
     * @throws IndexOutOfBoundsException, RuntimeException
     */
    public int getInteger(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        if (pdxClass.getType() != PDXClass.INT) {
            throw new RuntimeException(Sequence.INDEX_IS_OF_WRONG_TYPE);
        }
        return ((PDXInteger) pdxClass).getValue();
    }

    /**
     * Retrieves a sequence byte string element.
     *
     * @param index element index
     * @return byte[]
     * @throws IndexOutOfBoundsException, RuntimeException
     */
    public byte[] getByteString(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        if (pdxClass.getType() != PDXClass.BYTES) {
            throw new RuntimeException(Sequence.INDEX_IS_OF_WRONG_TYPE);
        }
        return ((PDXByteString) pdxClass).getValue();
    }

    /**
     * Retrieves a sequence UTF8 string element.
     *
     * @param index element index
     * @return String
     * @throws IndexOutOfBoundsException, RuntimeException
     */
    public String getUTF8String(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        if (pdxClass.getType() != PDXClass.UTF8) {
            throw new RuntimeException(Sequence.INDEX_IS_OF_WRONG_TYPE);
        }
        return ((PDXUTF8String) pdxClass).getValue();
    }

    /**
     * Retrieves a sequence ASCII string element.
     *
     * @param index element index
     * @return String
     * @throws IndexOutOfBoundsException, RuntimeException
     */
    public String getAsciiString(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        if (pdxClass.getType() != PDXClass.ASCII) {
            throw new RuntimeException(Sequence.INDEX_IS_OF_WRONG_TYPE);
        }
        return ((PDXAsciiString) pdxClass).getValue();
    }

    /**
     * Retrieves a sequence Dictionary element.
     *
     * @param index element index
     * @return Dictionary
     * @throws IndexOutOfBoundsException, RuntimeException
     */
    public Dictionary getDictionary(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        if (pdxClass.getType() != PDXClass.DICT) {
            throw new RuntimeException(Sequence.INDEX_IS_OF_WRONG_TYPE);
        }
        return ((PDXDictionary) pdxClass);
    }

    /**
     * Retrieves a sequence Sequence element.
     *
     * @param index element index
     * @return Sequence
     * @throws IndexOutOfBoundsException, RuntimeException
     */
    public Sequence getSequence(int index) {
        if (index >= sequence.size()) {
            throw new IndexOutOfBoundsException();
        }
        PDXClass pdxClass = (PDXClass) sequence.elementAt(index);
        if (pdxClass.getType() != PDXClass.SEQ) {
            throw new RuntimeException(Sequence.INDEX_IS_OF_WRONG_TYPE);
        }
        return ((PDXSequence) pdxClass);
    }

    public void addBoolean(boolean value) {
        PDXBoolean pdxBoolean = new PDXBoolean(value);
        sequence.addElement(pdxBoolean);
    }

    public void addDouble(double value) {
        PDXDouble pdxDouble = new PDXDouble(value);
        sequence.addElement(pdxDouble);
    }

    public void addAsciiString(String value) {
        if (value == null) throw new IllegalArgumentException("value is null.");
        PDXAsciiString string = new PDXAsciiString(value);
        sequence.addElement(string);
    }

    public void addByteString(byte[] value) {
        if (value == null) throw new IllegalArgumentException("value is null.");
        PDXByteString string = new PDXByteString(value);
        sequence.addElement(string);
    }

    public void addDictionary(Dictionary value) {
        if (value == null) throw new IllegalArgumentException("value is null.");
        if (((PDXClass) value).getType() != PDXClass.DICT) {
            throw new IllegalArgumentException("value is not a valid dictionary. ");
        }
        sequence.addElement(value);
    }

    public void addInteger(int value) {
        PDXInteger integer = new PDXInteger(value);
        sequence.addElement(integer);
    }

    public void addSequence(Sequence value) {
        if (value == null) throw new IllegalArgumentException("value is null.");
        if (((PDXClass) value).getType() != PDXClass.SEQ) {
            throw new IllegalArgumentException("value is not a valid sequence. ");
        }
        sequence.addElement(value);
    }

    public void addUTF8String(String value) {
        if (value == null) throw new IllegalArgumentException("value is null.");
        PDXUTF8String string = new PDXUTF8String(value);
        sequence.addElement(string);
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Enumeration elements = sequence.elements();
        while (elements.hasMoreElements()) {
            PDXClass pdxElement = (PDXClass) elements.nextElement();
            try {
                switch (pdxElement.getType()) {
                    case PDXClass.BOOLEAN:
                        output.write(((PDXBoolean) pdxElement).toByteArray());
                        break;
                    case PDXClass.DOUBLE:
                        output.write(((PDXDouble) pdxElement).toByteArray());
                        break;
                    case PDXClass.INT:
                        output.write(((PDXInteger) pdxElement).toByteArray());
                        break;
                    case PDXClass.BYTES:
                        output.write(((PDXByteString) pdxElement).toByteArray());
                        break;
                    case PDXClass.UTF8:
                        output.write(((PDXUTF8String) pdxElement).toByteArray());
                        break;
                    case PDXClass.ASCII:
                        output.write(((PDXAsciiString) pdxElement).toByteArray());
                        break;
                    case PDXClass.NULL:
                        output.write(((PDXNull) pdxElement).toByteArray());
                        break;
                    case PDXClass.DICT:
                        output.write(((PDXDictionary) pdxElement).toByteArray());
                        break;
                    case PDXClass.SEQ:
                        output.write(((PDXSequence) pdxElement).toByteArray());
                        break;
                }
            } catch (java.io.IOException ioe) {
            }
        }
        return super.toByteArray(output.toByteArray());
    }

    public String toString() {
        return this.toString(0);
    }

    protected String toString(int indentLevel) {
        StringBuilder stringSequence = new StringBuilder();
        StringBuilder baseIndent = new StringBuilder();
        String indent = "";
        for (int i = 0; i < indentLevel - 1; i++) {
            baseIndent.append("    ");
        }
        if (indentLevel > 0) {
            indent = baseIndent + "    ";
        }
        Enumeration elements = sequence.elements();
        stringSequence.append("[ \n");
        while (elements.hasMoreElements()) {
            PDXClass value = (PDXClass) elements.nextElement();
            switch (value.getType()) {
                case PDXClass.BOOLEAN:
                    stringSequence.append(indent).append("<BOOLEAN> ").append(((PDXBoolean) value).getValue());
                    break;
                case PDXClass.DOUBLE:
                    stringSequence.append(indent).append("<DOUBLE> ").append(((PDXDouble) value).getValue());
                    break;
                case PDXClass.INT:
                    stringSequence.append(indent).append("<INT> ").append(((PDXInteger) value).getValue());
                    break;
                case PDXClass.BYTES:
                    stringSequence.append(indent).append("<BYTES> \"").append(((PDXByteString) value).getValue()).append("\" ");
                    break;
                case PDXClass.UTF8:
                    stringSequence.append(indent).append("<UTF8> \"").append(((PDXUTF8String) value).getValue()).append("\" ");
                    break;
                case PDXClass.ASCII:
                    stringSequence.append(indent).append("<ASCII> \"").append(((PDXAsciiString) value).getValue()).append("\" ");
                    break;
                case PDXClass.NULL:
                    stringSequence.append(indent).append("<NULL> ");
                    break;
                case PDXClass.DICT:
                    stringSequence.append(indent).append(((PDXDictionary) value).toString(indentLevel + 1));
                    break;
                case PDXClass.SEQ:
                    stringSequence.append(indent).append(((PDXSequence) value).toString(indentLevel + 1));
                    break;
            }
            if (elements.hasMoreElements()) {
                stringSequence.append(",");
            }
            stringSequence.append("\n");
        }
        stringSequence.append(baseIndent).append("] ");
        return stringSequence.toString();
    }
}
