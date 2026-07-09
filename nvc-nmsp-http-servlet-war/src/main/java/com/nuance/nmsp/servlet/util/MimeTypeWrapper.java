package com.nuance.nmsp.servlet.util;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class MimeTypeWrapper
{
    private String rawString;
    private MimeType mimeType;

    public MimeTypeWrapper()
    {
        this.rawString = "";
        this.mimeType = new MimeType();
    }

    public MimeTypeWrapper(String rawString)
            throws MimeTypeParseException
    {
        this.rawString = rawString;
        this.mimeType = new MimeType(rawString);
    }

    public String getRawString()
    {
        return rawString;
    }

    public MimeType getMimeType()
    {
        return mimeType;
    }
}
