package com.nuance.nmsp.servlet.util;

import org.junit.Test;

import javax.activation.MimeTypeParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class MimeTypeWrapperTest {

    @Test
    public void noArgConstructorYieldsEmptyRawAndDefaultMimeType() {
        MimeTypeWrapper wrapper = new MimeTypeWrapper();
        assertEquals("", wrapper.getRawString());
        assertNotNull(wrapper.getMimeType());
    }

    @Test
    public void stringConstructorParsesMimeType() throws MimeTypeParseException {
        MimeTypeWrapper wrapper = new MimeTypeWrapper("audio/x-wav");
        assertEquals("audio/x-wav", wrapper.getRawString());
        assertEquals("audio", wrapper.getMimeType().getPrimaryType());
        assertEquals("x-wav", wrapper.getMimeType().getSubType());
    }

    @Test
    public void stringConstructorThrowsOnInvalidInput() {
        try {
            new MimeTypeWrapper("not-a-valid-mime-type;;==");
            fail("expected MimeTypeParseException");
        } catch (MimeTypeParseException expected) {
            // OK
        }
    }
}
