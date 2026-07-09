package com.nuance.cobra.utils.configs;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.config.ConfigHelpers;
import com.nuance.cobra.utils.config.ReverseOverrideCombiner;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

public class CobraConfigurationUtils
{
    private static final Logger logger = LogManager.getLogger(CobraConfigurationUtils.class);

    private static WebApiJSONConfiguration webApiJSONConfigurationDefaults = null;

    CobraConfigurationUtils()
    {
        throw new IllegalStateException("Utility class");
    }

    public static CombinedConfiguration addStartSessionParamsToConfiguration(CombinedConfiguration cc, Map<String, Object> startSessionParams)
    {
        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
        webApiJSONConfiguration.load(startSessionParams);
        cc.addConfiguration(webApiJSONConfiguration, WebApiConfigManager.CONFIG_NAME_START_SESSION_PARAMS);
        return cc;
    }

    public static CombinedConfiguration addCommandParamsToConfiguration(CombinedConfiguration cc, Map<String, Object> params)
    {
        cc.removeConfiguration(WebApiConfigManager.CONFIG_NAME_COMMAND_PARAMS);
        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration();
        webApiJSONConfiguration.load(params);
        cc.addConfiguration(webApiJSONConfiguration, WebApiConfigManager.CONFIG_NAME_COMMAND_PARAMS);
        return cc;
    }

    public static CombinedConfiguration lookupTenantConfigAndAddParams(String tenantKey, Map<String,Object> parameters)
    {
        CombinedConfiguration cc = null;
        try
        {
            cc = WebApiConfigManager.getCombinedConfigForTenant(tenantKey);
        }
        catch (CobraException e)
        {
            logger.error(e.getMessage());
            cc = shallowCopyCombinedConfiguration(WebApiConfigManager.getInstance());
        }

        return addCommandParamsToConfiguration(cc, parameters);
    }

    public static void setProperty(CombinedConfiguration cc, String property, Object value)
    {
        Configuration c = getHighestConfig(cc);
        if (null != c)
        {
            c.setProperty(property, value);
        }
    }

    public static Configuration getHighestConfig(CombinedConfiguration cc)
    {
        if (cc.getNumberOfConfigurations() == 0)
        {
            return null;
        }
        return cc.getConfiguration(cc.getNumberOfConfigurations() - 1);
    }

    public static WebApiJSONConfiguration getHighestConfigAsWebapiJsonConfiguration(CombinedConfiguration cc)
    {
        return (WebApiJSONConfiguration) getHighestConfig(cc);
    }


    private static void logNewCC()
    {
        logger.trace("new CombinedConfiguration");
    }

    public static CombinedConfiguration newCombinedConfigurationWithSingleConfig()
    {
        WebApiJSONConfiguration config = new WebApiJSONConfiguration();
        logNewCC();
        CombinedConfiguration cc = new CombinedConfiguration(new ReverseOverrideCombiner());
        logger.trace("newCombinedConfigurationWithSingleConfig: {}", cc);

        cc.addConfiguration(config, WebApiConfigManager.CONFIG_NAME_BLANK);
        return cc;
    }

    public static CombinedConfiguration newCombinedConfigurationWithDefaults()
    {
        logNewCC();
        CombinedConfiguration cc = new CombinedConfiguration(new ReverseOverrideCombiner());
        logger.trace("newCombinedConfigurationWithDefaults: {}", cc);

        WebApiJSONConfiguration forSetProperties = new WebApiJSONConfiguration();
        WebApiJSONConfiguration defaults = new WebApiJSONConfiguration(getDefaults());

        cc.addConfiguration(defaults, WebApiConfigManager.CONFIG_NAME_DEFAULTS);
        cc.addConfiguration(forSetProperties, WebApiConfigManager.CONFIG_NAME_BLANK);

        return cc;
    }

    public static WebApiJSONConfiguration getDefaults()
    {
        if (webApiJSONConfigurationDefaults == null)
        {
            String apiVersion = WebApiConfig.Property.API_VERSION.key; //init the enum
            logger.trace("{} = {}", WebApiConfig.Property.API_VERSION.key, apiVersion);
            String defaultsAsJson = WebApiConfig.getDefaultsAsJson();
            Reader targetReader = new StringReader(defaultsAsJson);
            webApiJSONConfigurationDefaults = new WebApiJSONConfiguration();
            try
            {
                webApiJSONConfigurationDefaults.read(targetReader);
                targetReader.close();
            }
            catch (Exception e)
            {
                logger.error("Exception while converting webapi defaults json to WebApiJSONConfiguration: {}",
                        e.getMessage());
                webApiJSONConfigurationDefaults = null;
            }
        }

        return webApiJSONConfigurationDefaults;
    }

    public static CombinedConfiguration shallowCopyCombinedConfiguration(CombinedConfiguration cc)
    {
        logNewCC();
        CombinedConfiguration result = new CombinedConfiguration(new ReverseOverrideCombiner());
        logger.trace("shallowCopyCombinedConfiguration: res = {} src = {}", result, cc);

        for(int i = 0; i < cc.getNumberOfConfigurations(); i++)
        {
            logger.trace("Copying config {} named \"{}\".", i, cc.getConfigurationNameList().get(i));
            result.addConfiguration(cc.getConfiguration(i), cc.getConfigurationNameList().get(i));
        }
        return result;
    }

    public static HierarchicalConfiguration configurationAtOrNull(HierarchicalConfiguration config, String key)
    {
        HierarchicalConfiguration rc;
        try
        {
            rc = config.configurationAt(key);
        } catch(Exception e)
        {
            rc = null;
        }
        return rc;
    }

    public static Map<String, Object> hierarchicalConfigurationToMap(HierarchicalConfiguration config)
    {
        if(null == config || config.isEmpty())
        {
            return null;
        }

        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration(config);
        return webApiJSONConfiguration.constructMap();
    }

    public static String hierarchicalConfigurationToJson(HierarchicalConfiguration config)
    {
        if(null == config || config.isEmpty())
        {
            return null;
        }

        StringWriter rc = new StringWriter();
        WebApiJSONConfiguration webApiJSONConfiguration = new WebApiJSONConfiguration(config);
        try
        {
            webApiJSONConfiguration.write(rc);
            return rc.toString();
        } catch (Exception e)
        {
            return null;
        }
    }

    public static void clearCombinedConfiguration(CombinedConfiguration cc, String sessionId)
    {
        ConfigHelpers.clearCombinedConfiguration(cc, sessionId);
    }

    //Returns a "flattened" WebApiJSONConfiguration object
    public static WebApiJSONConfiguration getFlattenedConfig(CombinedConfiguration cc)
    {
        return new WebApiJSONConfiguration(cc);
    }

}
