package com.nuance.cobra.api.globalregistry.parser;


import com.nuance.cobra.api.globalregistry.Application;
import com.nuance.cobra.api.globalregistry.Company;
import com.nuance.cobra.api.globalregistry.adapter.GlobalRegistryJSONParserAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Custom JSON parser for global registry "Company" element
 */
public class CompanyJSONParser
        implements GlobalRegistryJSONParser<Company>
{

    private static final Logger logger = LogManager.getLogger(CompanyJSONParser.class);

    @Override
    public Company parseJSON(JSONObject jsonObject)
    {
        Company company = new Company();
        for (String keyName : jsonObject.keySet())
        {
            logger.debug("Application Name: " + keyName);
            Object app = jsonObject.get(keyName);
            if (app instanceof JSONObject)
            {
                GlobalRegistryJSONParserAdapter<Application> parser = new GlobalRegistryJSONParserAdapter<Application>(
                        new ApplicationJSONParser());
                //TODO add comment
                Application application = parser.parseJSON((JSONObject) app);
                company.addApplication(keyName, application);
            }

        }
        return company;

    }
}
