package com.nuance.cobra.command;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class BaseResponseFormatterTest {

    @Test
    public void testFormatResponse(){
        BaseResponseFormatter formatter = new BaseResponseFormatter();
        String test = "test";

        Assert.assertEquals("test", formatter.formatResponse(test));
    }
}
