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

/**
 * Represents a Sequence object. The index of all Sequence 
 * objects is zero-based.<br />
 * Use method {@link NMASResource#createPDXSequence} to create an
 * object that implements the <code>Sequence</code> interface.
 *
 * @author Nuance Communication Inc.
 */
public interface Sequence {

    /**
     * This error message string is carried by a java.lang.RuntimeException.
     * <p>
     * A java.lang.RuntimeException with this error message string is thrown if
     * the element type, specified by the index of the retrieval operation,
     * is not the same as the actual element type in the Sequence.
     *
     */
    public static String INDEX_IS_OF_WRONG_TYPE = "index is of wrong type.";

    /**
     * Retrieves from the Sequence the type of the element at the location specified by the index. 
     *
     * @param index   index of the element whose type is to be retrieved
     *
     * @return type of the element specified by the index
     */
    public short getType(int index);

    /**
     * Retrieves from the Sequence a boolean element with the location specified by the index.
     *
     * @param index   index of the boolean element to retrieve
     *
     * @return boolean
     *
     * @throws IndexOutOfBoundsException  thrown when the value is outside the index range
     * @throws RuntimeException  thrown when there is a data-type mismatch 
     *                           between the element requested and the retrieved element 
     */
    public boolean getBoolean(int index);

    /**
     * Adds an boolean element to the Sequence.
     *
     * @param value   boolean element to add
     *
     */
    public void addBoolean(boolean value);

    /**
     * Retrieves from the Sequence a double element with the location specified by the index.
     *
     * @param index   index of the double element to retrieve
     *
     * @return double
     *
     * @throws IndexOutOfBoundsException  thrown when the value is outside the index range
     * @throws RuntimeException  thrown when there is a data-type mismatch 
     *                           between the element requested and the retrieved element 
     */
    public double getDouble(int index);

    /**
     * Adds a double element to the Sequence.
     *
     * @param value   double element to add
     *
     */
    public void addDouble(double value);

    /**
     * Retrieves from the Sequence an integer element with the location specified by the index.
     *
     * @param index   index of the integer element to retrieve
     *
     * @return int
     *
     * @throws IndexOutOfBoundsException  thrown when the value is outside the index range
     * @throws RuntimeException  thrown when there is a data-type mismatch 
     *                           between the element requested and the retrieved element 
     */
    public int getInteger(int index);

    /**
     * Adds an integer element to the Sequence.
     *
     * @param value   integer element to add
     *
     */
    public void addInteger(int value);

    /**
     * Retrieves a byte string element with the location specified by the index from the Sequence.
     *
     * @param index   index of the byte string element to retrieve
     *
     * @return byte[]
     *
     * @throws IndexOutOfBoundsException  thrown when the value is outside the index range
     * @throws RuntimeException  thrown when there is a data-type mismatch 
     *                           between the requested element and the retrieved element
     */
    public byte[] getByteString(int index);

    /**
     * Adds a byte string element to the Sequence.
     *
     * @param value   byte string element to add
     *
     */
    public void addByteString(byte[] value);

    /**
     * Retrieves a UTF8 string element with the location specified by the index from the Sequence.
     *
     * @param index   index of the UTF8 element to retrieve
     *
     * @return string
     *
     * @throws IndexOutOfBoundsException  thrown when the value is outside the index range
     * @throws RuntimeException  thrown when there is a data-type mismatch 
     *                           between the requested element and the retrieved element
     */
    public String getUTF8String(int index);

    /**
     * Adds a UTF8 string element to the Sequence.
     *
     * @param value   UTF8 string element to add
     */
    public void addUTF8String(String value);

    /**
     * Retrieves an ASCII string element with the location specified by the index from the Sequence.
     *
     * @param index   index of the ASCII string element to retrieve
     *
     * @return string
     *
     * @throws IndexOutOfBoundsException when the value is outside the index range
     * @throws RuntimeException thrown when there is a data-type mismatch 
     *                           between the requested element and the retrieved element
     */
    public String getAsciiString(int index);

    /**
     * Adds an ASCII string element to the Sequence.
     *
     * @param value   ASCII string element to add
     *
     */
    public void addAsciiString(String value);

    /**
     * Retrieves a dictionary element with the location specified by the index from the Sequence.
     *
     * @param index   element index
     *
     * @return index of the dictionary element to retrieve
     *
     * @throws IndexOutOfBoundsException  thrown when the value is outside the index range
     * @throws RuntimeException  thrown when there is a data-type mismatch 
     *                           between the requested element and the retrieved element  
     */
    public Dictionary getDictionary(int index);

    /**
     * Adds a dictionary element to the Sequence.
     *
     * @param value   dictionary element to add
     *
     */
    public void addDictionary(Dictionary value);

    /**
     * Retrieves a sequence element with the location specified by the index from the Sequence.
     *
     * @param index   index of the sequence element to retrieve
     *
     * @return sequence
     *
     * @throws IndexOutOfBoundsException  thrown when the value is outside the index range
     * @throws RuntimeException  thrown when there is a data-type mismatch 
     *                           between the requested element and the retrieved element
     */
    public Sequence getSequence(int index);

    /**
     * Adds a sequence element to the Sequence.
     *
     * @param value   sequence element to add
     *
     */
    public void addSequence(Sequence value);

    /**
     * Returns the number of elements of the Sequence object.
     *
     * @return number of elements in the Sequence
     */
    public int size();
}
