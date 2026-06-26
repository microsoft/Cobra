package com.nuance.cobra.api.request;

import org.junit.Test;

import static com.nuance.cobra.api.request.RequestDirective.RequestDirectiveName.STOP;
import static org.junit.Assert.assertEquals;

public class RequestDirectiveTest {

    @Test
    public void testName() {
        RequestDirective.RequestDirectiveName stop = STOP;
        RequestDirective rd = new RequestDirective();
        rd.setName(stop);
        assertEquals(rd.getName(), stop);
        assertEquals("\n---name: " + stop, rd.toString());
    }
}