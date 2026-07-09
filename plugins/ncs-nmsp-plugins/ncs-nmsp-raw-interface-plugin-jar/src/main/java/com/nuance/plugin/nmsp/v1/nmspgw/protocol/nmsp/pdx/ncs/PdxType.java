/**
 * COPYRIGHT (c) 2012 Nuance Communications Inc. All Rights Reserved. 
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc. 
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied. 
 */
package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

//PdxType should not be confused with PdxClass.  This is for parsing the PdxCmdTable only.
public enum PdxType
{
  Ascii, 
  Bytes, 
  Utf8, 
  Number, 
  List, 
  Dictionary
}
