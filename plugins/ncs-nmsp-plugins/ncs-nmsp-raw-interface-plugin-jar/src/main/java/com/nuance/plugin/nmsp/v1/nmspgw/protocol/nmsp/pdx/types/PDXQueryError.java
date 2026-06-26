package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types;

public class PDXQueryError {
    private static final short CMD = (short) 0x7202;
    private final byte[] dictBytes;

    public PDXQueryError(byte[] dictBytes) {
        this.dictBytes = dictBytes;
    }

    public byte[] toByteArray() {
        byte[] out = new byte[2 + dictBytes.length];
        out[0] = (byte) (CMD & 0xFF);
        out[1] = (byte) ((CMD >>> 8) & 0xFF);
        System.arraycopy(dictBytes, 0, out, 2, dictBytes.length);
        return out;
    }
}
