package com.nuance.nmsp.servlet.util;

public class HexToArray
{
    private HexToArray() { throw new IllegalStateException("Utility class!"); }

    /**
     * Converts an hex string to a byte array
     *
     * @param hexString - e.g. "8E77144A"
     * @return
     */
    public static byte[] hexStringToByteArray(String hexString)
    {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                  + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
