package com.nuance.ncs.nmsp.servlet.utils;

public class HexToArray
{
	private HexToArray() { throw new IllegalStateException("Utility class!"); }

	@SuppressWarnings("java:S1168")
	public static byte[] hexStringToByteArray(String s) {
	    if( s == null )
	    	return null;
	    
		int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
}
