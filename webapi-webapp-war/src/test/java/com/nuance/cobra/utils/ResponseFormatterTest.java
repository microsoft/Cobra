package com.nuance.cobra.utils;

import com.nuance.cobra.command.BaseResponseFormatter;
import com.nuance.cobra.utils.response.ResponseFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("StringEquality")
public class ResponseFormatterTest
{
    @Before
    public void setUp()
            throws Exception
    {

    }

    @After
    public void tearDown()
            throws Exception
    {

    }


    @Test
    public void test_constructor() throws Exception
    {
        new BaseResponseFormatter();
    }


    @Test
    public void test_formatResposne_null() throws Exception
    {
        final String response = null;
        final ResponseFormatter formatter = new BaseResponseFormatter();

        final String formatted = formatter.formatResponse(response);

        assertEquals(formatted, response);
    }

    @Test
    public void test_formatResposne_empty() throws Exception
    {
        final String response = "";
        final ResponseFormatter formatter = new BaseResponseFormatter();

        final String formatted = formatter.formatResponse(response);

        assertTrue(formatted.equals(response));
    }

    @Test
    public void test_formatResposne() throws Exception
    {
        final String response = "response";
        final ResponseFormatter formatter = new BaseResponseFormatter();

        final String formatted = formatter.formatResponse(response);

        assertTrue(formatted.equals(response));
    }

}
