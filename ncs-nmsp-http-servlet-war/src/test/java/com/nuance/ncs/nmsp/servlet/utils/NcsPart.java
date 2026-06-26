package com.nuance.ncs.nmsp.servlet.utils;

import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class NcsPart implements Part
{
    private final String name;
    private final Map<String, String> headers = new HashMap<>();
    private ByteArrayInputStream bodyPartInputStream;

    public NcsPart(String name, byte[] bodyPartBytes)
    {
        this.name = name;
        setBytes(bodyPartBytes);
    }

    public void setBytes(byte[] bodyPartBytes)
    {
        bodyPartInputStream = new ByteArrayInputStream(bodyPartBytes);
    }

    @Override
    public InputStream getInputStream()
            throws IOException
    {
        return bodyPartInputStream;
    }

    @Override
    public String getContentType()
    {
        return getHeader("Content-Type");
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getSubmittedFileName()
    {
        return null;
    }

    @Override
    public long getSize()
    {
        return bodyPartInputStream.available();
    }

    @Override
    public void write(String s)
            throws IOException
    {

    }

    @Override
    public void delete()
            throws IOException
    {

    }

    @Override
    public String getHeader(String s)
    {
        return headers.get(s);
    }

    @Override
    public Collection<String> getHeaders(String s)
    {
        List<String> rcList = new ArrayList<>();
        String rcString = getHeader(s);
        if(rcString != null) rcList.add(rcString);
        return rcList;
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return headers.keySet();
    }

    public void setHeader(String name, String value)
    {
        headers.put(name, value);
    }
}
