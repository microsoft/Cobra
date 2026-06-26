package com.nuance.cobra.utils.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class JSONUtil
{

    private static final Logger logger = LogManager.getLogger(JSONUtil.class);

    public enum PACKAGE_TYPE
    {
        nleSeries3Grammars, kryptonDlms, nleSeries2Grammars, ivrGrammars, dmGrammars
    }

    public static URL getPackageUrl(String companyName, String applicationName, String version, PACKAGE_TYPE type)
    {

        JSONObject jsonObject = new JSONObject(/*replace this with wherever the global registry json is stored after retrieval)*/);

        if ((version.length() != 3 && version.length() != 5)) throw new AssertionError("Invalid Version Passed. " +
                "Format: x.y or x.y.z, where x y and z are integers.");

        try
        {
            JSONObject companyJson = jsonObject.getJSONObject(companyName);
            JSONObject applicationInfo = companyJson.getJSONObject(applicationName);
            JSONObject grammarInfo = applicationInfo.getJSONObject(type.name());
            if (version.length() == 3)
            {
                return new URL(grammarInfo.getString(grammarInfo.getString("current")));
            }
            else
            {
                return new URL(grammarInfo.getString(version));
            }
        }
        catch (MalformedURLException e)
        {
            logger.error("MalformedUrlException : " + e.getMessage());
        }

        return null;
    }
}
