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

import java.util.Enumeration;

/**
 * Represents a Dictionary object. 
 * To create an object that implements the Dictionary interface, use 
 * <code>NMASResource.createPDXDictionary()</code>.
 *
 * @author Nuance Communication Inc.
 */
public interface Dictionary {

    /**
     * This error message string is carried by java.lang.RuntimeException  
     * if the key does not exist. 
     */
    public static String KEY_DOES_NOT_EXIST = "key does not exist. ";

    /**
     * This error message string is carried by java.lang.RuntimeException  
     * if the key is of the wrong type. 
     */
    public static String KEY_IS_OF_WRONG_TYPE = "key is of wrong type. ";

    /**
     * Retrieves an enumeration of the keys in this dictionary.
     *
     * @return enumeration
     */
    public Enumeration keys();

    /**
     * Determines if the specified string is a key in this <code>Dictionary</code>. 
     *
     * @param key the key string to test
     * @return true if the provided string is a key in this <code>Dictionary</code>; otherwise, false
     */
    public boolean containsKey(String key);

    /**
     * Retrieves the PDX type of key. 
     *
     * @param key the key of the element type to retrieve
     *
     * @return the type of the element
     */
    public short getType(String key);

    /**
     * Retrieves a integer element from the dictionary.
     *
     * @param key   the key of the boolean element to retrieve
     *
     * @return the value of the boolean element
     *
     * @throws RuntimeException
     */
    public boolean getBoolean(String key);

    /**
     * Adds a boolean element.
     *
     * @param key    the key to associate with the boolean element 
     * @param value  the value of the boolean element to add
     *
     */
    public void addBoolean(String key, boolean value);

    /**
     * Retrieves a double element from the dictionary.
     *
     * @param key   the key of the double element to retrieve
     *
     * @return the value of the double element
     *
     * @throws RuntimeException
     */
    public double getDouble(String key);

    /**
     * Adds a double element.
     *
     * @param key    the key to associate with the double element 
     * @param value  the value of the double element to add
     *
     */
    public void addDouble(String key, double value);

    /**
     * Retrieves an integer element from the dictionary.
     *
     * @param key   the key of the integer element to retrieve
     *
     * @return the value of the integer element
     *
     * @throws RuntimeException
     */
    public int getInteger(String key);

    /**
     * Adds an integer element.
     *
     * @param key    the key to associate with the integer element 
     * @param value  the value of the integer element to add
     *
     */
    public void addInteger(String key, int value);

    /**
     * Retrieves a byte string element from the dictionary.
     *
     * @param key     the key of the byte string element to retrieve
     *
     * @return the value of the byte string element
     *
     * @throws RuntimeException
     */
    public byte[] getByteString(String key);

    /**
     * Adds a byte string element.
     *
     * @param key   the key to associate with the byte string element
     * @param value the value of the byte string element to add
     *
     */
    public void addByteString(String key, byte[] value);

    /**
     * Retrieves a UTF8 string element from the dictionary.
     *
     * @param key   the key of the UTF8 string element to retrieve
     *
     * @return the value of the UTF8 string element
     *
     * @throws RuntimeException
     */
    public String getUTF8String(String key);

    /**
     * Adds a UTF8 string element.
     *
     * @param key     the key to associate with the UTF8 string element
     * @param value   the value of the UTF8 string element to add
     *
     */
    public void addUTF8String(String key, String value);

    /**
     * Retrieves an ASCII string element from the dictionary.
     *
     * @param key      the key of the ASCII string element to retrieve
     *
     * @return the value of the ASCII string element
     *
     * @throws RuntimeException
     */
    public String getAsciiString(String key);

    /**
     * Adds an ASCII string element.
     *
     * @param key   the key to associate with the ASCII string element
     * @param value the value of the ASCII string element to add
     */
    public void addAsciiString(String key, String value);

    /**
     * Retrieves a Dictionary element.
     *
     * @param key          the key of the Dictionary element to retrieve
     *
     * @return the value of the Dictionary element 
     *
     * @throws RuntimeException
     */
    public Dictionary getDictionary(String key);

    /**
     * Adds a Dictionary element.
     *
     * @param key    the key to associate with the Dictionary element
     * @param value  the value of the Dictionary element to add
     *
     */
    public void addDictionary(String key, Dictionary value);

    /**
     * Retrieves a Sequence element from the dictionary.
     *
     * @param key        the key of the Sequence element to retrieve 
     *
     * @return the value of the Sequence element
     *
     * @throws RuntimeException
     */
    public Sequence getSequence(String key);

    /**
     * Adds a Sequence element.
     *
     * @param key    the key to associate with the Sequence element
     * @param value  the value of the Sequence element to add
     *
     */
    public void addSequence(String key, Sequence value);
}
