package com.nuance.cobra.api.globalregistry.parser;

import com.nuance.cobra.api.globalregistry.Application;
import com.nuance.cobra.api.globalregistry.Company;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.logging.Logger;

@PrepareForTest(Logger.class)
public class ApplicationJSONParserTest {

    @Test
    public void parseJSONTest(){
        ApplicationJSONParser parser = new ApplicationJSONParser();
        JSONObject json = new JSONObject();
        json.put("application", new JSONObject().put("grammar", new JSONObject().put("current","test")));
        assert(parser.parseJSON(json) instanceof Application);
        json = new JSONObject();
        json.put("application", new JSONObject().put("grammar", new JSONObject().put("test","test")));
        assert(parser.parseJSON(json) instanceof Application);
        Assert.assertNotNull(parser.parseJSON(json));
    }

    @Test

    public void companyJSONParserTest(){
        CompanyJSONParser parser = new CompanyJSONParser();
        JSONObject json = new JSONObject();
        json.put("application", new JSONObject().put("grammar", new JSONObject().put("current","test")));
        assert(parser.parseJSON(json) instanceof Company);
        json = new JSONObject();
        json.put("application", new JSONObject().put("grammar", new JSONObject().put("test","test")));
        assert(parser.parseJSON(json) instanceof Company);
        Assert.assertNotNull(parser.parseJSON(json));
    }
}
