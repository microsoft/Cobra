package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.ncs;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class PdxToXmlTest {

    @Test
    public void write() throws IOException {
        Writer writer = new StringWriter();
        PdxToXml testPdxToXml = new PdxToXml(writer);
        PdxPacker testPacker = new PdxPacker();
        testPacker.packAsciiString("test");
        byte[] testPacket = testPacker.toArray();
        testPdxToXml.write(new PdxPacket(testPacket, 0));
        assertEquals("<ASCII>test</ASCII>\n", writer.toString());

        writer = new StringWriter();
        testPdxToXml = new PdxToXml(writer);
        testPacker = new PdxPacker();
        testPacker.packNumber(42);
        testPacket = testPacker.toArray();
        testPdxToXml.write(new PdxPacket(testPacket, 0));
        assertEquals("<INT>42</INT>\n", writer.toString());

        writer = new StringWriter();
        testPdxToXml = new PdxToXml(writer);
        testPacker = new PdxPacker();
        List<Object> testList = new ArrayList<>();
        testList.add("test");
        testPacker.packSequence(testList);
        testPacket = testPacker.toArray();
        testPdxToXml.write(new PdxPacket(testPacket, 0));
        assertEquals("<SEQ><UTF8>test</UTF8>\n" +
                "</SEQ>\n", writer.toString());

        writer = new StringWriter();
        testPdxToXml = new PdxToXml(writer);
        testPacker = new PdxPacker();
        HashMap<String, Object> testMap = new HashMap<>();
        testMap.put("test", "value");
        testPacker.packDictionary(testMap);
        testPacket = testPacker.toArray();
        testPdxToXml.write(new PdxPacket(testPacket, 0));
        assertEquals("<DICT><UTF8>test</UTF8>\n" +
                "<UTF8>value</UTF8>\n" +
                "</DICT>\n", writer.toString());
    }
}