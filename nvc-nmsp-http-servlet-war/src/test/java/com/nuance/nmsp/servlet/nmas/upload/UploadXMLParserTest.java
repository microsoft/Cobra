package com.nuance.nmsp.servlet.nmas.upload;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class UploadXMLParserTest extends TestCase {

    private static InputStream xmlStream(String xml) {
        return new ByteArrayInputStream(xml.getBytes());
    }

    public void testParseXML() {
        String testRequest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<doc>\n" +
                "<datablock>\n" +
                "<data>\n" +
                "<id>personal_contacts</id>\n" +
                "<checksum>0</checksum>\n" +
                "<previous_checksum>0</previous_checksum>\n" +
                "<type>contacts</type>\n" +
                "<actions>\n" +
                "<clearall/>\n" +
                "<add>\n" +
                "<contacts>\n" +
                "<contact>\n" +
                "<firstname>John</firstname>\n" +
                "<lastname>Smith</lastname>\n" +
                "<fullname>John Smith</fullname>\n" +
                "<nickname>Johnny</nickname>\n" +
                "<locations>\n" +
                "<location>work</location>\n" +
                "<location>home</location>\n" +
                "<location>mobile</location>\n" +
                "</locations>\n" +
                "</contact>\n" +
                "<contact>\n" +
                "<firstname>Peter</firstname>\n" +
                "<lastname>McDonald</lastname>\n" +
                "<locations>\n" +
                "<location>home</location>\n" +
                "</locations>\n" +
                "</contact>\n" +
                "</contacts>\n" +
                "</add>\n" +
                "</actions>\n" +
                "</data>\n" +
                "</datablock>\n" +
                "</doc>\n";

        UploadXMLParser testXmlParser = new UploadXMLParser();
        DataBlock result = testXmlParser.parseXML(xmlStream(testRequest));

        assertTrue(result.isSuccess());
    }

    public void testParseXMLWithDeleteAll() {
        // Covers the <deleteall/> startElement branch.
        String xml = "<doc><datablock><deleteall/></datablock></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertEquals(1, result.getDeleteAll());
    }

    public void testParseXMLWithCustomWordsType() {
        // Covers `type.equals(CUSTOM_WORDS)` and the WORD start-element branch via <add><word>.
        String xml = "<doc><data>" +
                "<id>idA</id><checksum>0</checksum><type>custom_words</type>" +
                "<actions><add><words><word>hello</word><word>world</word></words></add></actions>" +
                "</data></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertTrue(result.isSuccess());
    }

    public void testParseXMLWithHistoryTypeAndRemove() {
        // Covers `type.equals(HISTORY)` plus the <remove> branch.
        String xml = "<doc><data>" +
                "<id>idH</id><checksum>0</checksum><type>history</type>" +
                "<actions><remove><words><word>x</word></words></remove></actions>" +
                "</data></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertTrue(result.isSuccess());
    }

    public void testParseXMLInvalidTypeReportsError() {
        // type other than contacts/custom_words/history → setErrorMsg with bad-type message.
        String xml = "<doc><data>" +
                "<id>bad</id><checksum>0</checksum><type>not_a_real_type</type>" +
                "</data></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMsg());
        assertTrue(result.getErrorMsg().contains("type"));
    }

    public void testParseXMLMalformedReturnsDataBlockOrError() {
        // An invalid XML document hits the XMLStreamException catch.
        String xml = "<doc><data><id>x</id><checksum>0</checksum><type>contacts</type></data";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        // parseXML swallows the XMLStreamException and returns the partial block.
        assertNotNull(result);
    }

    public void testParseXMLMissingIdFlagsError() {
        // </data> reached without <id> → setMissingTagError path.
        String xml = "<doc><data><checksum>0</checksum><type>contacts</type></data></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertFalse(result.isSuccess());
    }

    public void testParseXMLMissingChecksumFlagsError() {
        String xml = "<doc><data><id>x</id><type>contacts</type></data></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertFalse(result.isSuccess());
    }

    public void testParseXMLMissingTypeFlagsError() {
        String xml = "<doc><data><id>x</id><checksum>0</checksum></data></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertFalse(result.isSuccess());
    }

    public void testParseXMLWordOutsideActionFlagsError() {
        // A <word> without an enclosing <add>/<remove> action triggers setMissingTagError.
        String xml = "<doc><data><id>x</id><checksum>0</checksum><type>custom_words</type>" +
                "<actions><word>orphan</word></actions></data></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertFalse(result.isSuccess());
    }

    public void testParseXMLClearallOutsideDataFlagsError() {
        // <clearall/> not inside <data> reports the missing-tag error.
        String xml = "<doc><clearall/></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertFalse(result.isSuccess());
    }

    public void testParseXMLContactWithAllFields() {
        // Drives all CONTACT child branches (firstname, lastname, fullname, nickname, location).
        String xml = "<doc><data><id>id1</id><checksum>0</checksum><type>contacts</type>" +
                "<actions><add><contacts><contact>" +
                "<firstname>F</firstname><lastname>L</lastname>" +
                "<fullname>F L</fullname><nickname>FL</nickname>" +
                "<locations><location>home</location></locations>" +
                "</contact></contacts></add></actions></data></doc>";
        DataBlock result = new UploadXMLParser().parseXML(xmlStream(xml));
        assertTrue(result.isSuccess());
    }
}
