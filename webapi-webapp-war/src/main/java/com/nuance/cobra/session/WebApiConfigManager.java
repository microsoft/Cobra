package com.nuance.cobra.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.SecretCacheProvider;
import com.nuance.cobra.utils.TenantHttpClient;
import com.nuance.cobra.utils.Tools;
import com.nuance.cobra.utils.config.ReverseOverrideCombiner;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.config.WebapiGlobalCombinedConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Configuration master class. Used to manage the server configuration as well as retrieval of tenant configuration files.
 * Contains mainly static methods.
 */
public class WebApiConfigManager
{
    public enum TenantConfigScheme {
        onDisk,
        fetched
    }

    private static CombinedConfiguration webApiConfig = null;
    private static Logger logger = LogManager.getLogger(WebApiConfigManager.class);
    private static org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(WebApiConfigManager.class);

    private static final String SERVER_CONFIG_FILE = "webapi"; //looks for webapi.json
    private static final String TENANTS_DIR = "/tenants/";

    public static final String CONFIG_NAME_DEFAULTS = "defaults";
    public static final String CONFIG_NAME_WEBAPI = "webapi";
    public static final String CONFIG_NAME_KEY_VAULT_SERVER = "keyVaultServer";
    public static final String CONFIG_NAME_TENANT = "tenant";
    public static final String CONFIG_NAME_KEY_VAULT_TENANT = "keyVaultTenant";
    public static final String CONFIG_NAME_START_SESSION_PARAMS = "startSessionParams";
    public static final String CONFIG_NAME_COMMAND_PARAMS = "commandParams";

    public static final String CONFIG_NAME_BLANK = "blank";

    /**
     * @return Server Level Configuration
     */
    public static CombinedConfiguration getInstance()
    {
        return getInstance(SERVER_CONFIG_FILE);
    }

    public static CombinedConfiguration getInstance(String configFilePrefix)
    {
        if (webApiConfig != null)
        {
            return webApiConfig;
        }

        logger.trace("new CombinedConfiguration");
        webApiConfig = new WebapiGlobalCombinedConfiguration(new ReverseOverrideCombiner());

        if(configFilePrefix.isBlank()) //Enforces Java 11
        {
            configFilePrefix = SERVER_CONFIG_FILE;
        }

        synchronized (WebApiConfigManager.class)
        {
            WebApiJSONConfiguration webApiJSONConfigurationDefaults = CobraConfigurationUtils.getDefaults();
            if(webApiJSONConfigurationDefaults != null)
            {
                webApiJSONConfigurationDefaults.setProperty(WebApiConfig.Property.API_VERSION.key, WebApiConfig.getBuildVersion());
                webApiConfig.addConfiguration(webApiJSONConfigurationDefaults, CONFIG_NAME_DEFAULTS);
            }

            try
            {
                String path = getPresentConfigFile(getConfigPath(), configFilePrefix, Arrays.asList(".json"));
                logger.debug("Config file path : {}", path);
                Configuration webapiDotJson = (Configuration) getConfig(path);
                webApiConfig.addConfiguration(webapiDotJson, CONFIG_NAME_WEBAPI);

                //Add key vault server secret parameters to override existing webapi.json
                Configuration serverConfig = getKeyVaultConfig(webApiConfig, WebApiConfig.Property.KEY_VAULT_SERVER_SECRET_NAME);
                if (serverConfig != null) {
                    webApiConfig.addConfiguration(serverConfig);
                }
            }
            catch (CobraException e)
            {
                logger.error("Couldn't fetch the server config.", e);
            }

        }

        return webApiConfig;
    }

    /**
     * Returns the tenant+webapi+defaults config for a tenantKey
     *
     * @param tenantKey
     *         -- the key representing the desired tenant
     *
     * @return That tenant's config.
     *         Needs to be cleared for garbage collection e.g. CobraConfigurationUtils.clearCombinedConfig()
     */
    public static CombinedConfiguration getCombinedConfigForTenant(String tenantKey)
            throws CobraException
    {
        return getCombinedConfigForTenant(tenantKey, TENANTS_DIR);
    }

    //Returns a "flattened" Configuration object that doesn't need clearing
    public static WebApiJSONConfiguration getConfigForTenant(String tenantKey)
            throws CobraException
    {
        CombinedConfiguration cc = getCombinedConfigForTenant(tenantKey);
        WebApiJSONConfiguration rc = CobraConfigurationUtils.getFlattenedConfig(cc);
        CobraConfigurationUtils.clearCombinedConfiguration(cc, null);
        return rc;
    }

    public static CombinedConfiguration getCombinedConfigForTenant(String tenantKey, String tenantsDir)
            throws CobraException
    {
        if (!validateTenantKey(tenantKey)) {
            throw new CobraException(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.TENANT_CONFIG_FILE_NOT_FOUND, tenantKey));
        }

        CombinedConfiguration result = CobraConfigurationUtils.shallowCopyCombinedConfiguration(getInstance());

        WebApiJSONConfiguration tenantOnlyConfig;

        String tenantConfigScheme = WebApiConfigManager.getInstance().getString(WebApiConfig.Property.TENANT_CONFIG_SCHEME.key);
        if (tenantConfigScheme.equals(TenantConfigScheme.fetched.toString()))
        {
            tenantOnlyConfig = TenantHttpClient.getConfigForTenant(tenantKey);
        } else {

            /*
            Reloads are only functional if the /builder/ is
            used as central component for accessing configuration data.
            The configuration instance obtained from the builder
            will not change automatically! So if an application fetches
            a configuration object from the builder at startup and then
            uses it throughout its life time, changes on the external
            configuration file become never visible. The correct approach is to
            keep a reference to the builder centrally and obtain the configuration
            from there every time configuration data is needed.
             */

            String pathWithExtension = getPresentConfigFile(Tools.getJettyPath() + tenantsDir, tenantKey);
            FileBasedConfigurationBuilder builder = getConfigBuilder(pathWithExtension);

            try {
                ImmutableConfiguration i = builder.getConfiguration();

                if (i instanceof WebApiJSONConfiguration) {
                    tenantOnlyConfig = (WebApiJSONConfiguration) i;
                } else {
                    throw new ConfigurationException("Unsupported Type");
                }
            } catch (ConfigurationException e) {
                logger.error("Could not get config for tenant from builder: {}", e.getMessage());
                throw new CobraException(
                        ResponseCode.createResponseError(ExternalResponseErrorCode.TENANT_CONFIG_FILE_NOT_FOUND, tenantKey));
            }
        }

        tenantOnlyConfig.setProperty(WebApiConfig.Property.APPLICATION_KEY.key, tenantKey);
        result.addConfiguration(tenantOnlyConfig, CONFIG_NAME_TENANT);

        //Add key vault tenant secret parameters to override existing tenant file parameters
        Configuration keyVaultTenantConfig = getKeyVaultConfig(result, WebApiConfig.Property.KEY_VAULT_TENANT_SECRET_NAME);
        if (keyVaultTenantConfig != null) {
            result.addConfiguration(keyVaultTenantConfig, CONFIG_NAME_KEY_VAULT_TENANT);
        }

        return result;
    }

    public static boolean validateTenantKey(String tenantKey)
    {
        if (tenantKey == null)
        {
            return false;
        }

        String regex = WebApiConfigManager.getInstance().getString(WebApiConfig.Property.TENANT_VALIDATION_REGEX.key);
        if(tenantKey.matches(regex)
           || regex.equals("") //Tenant validation disabled
          )
        {
            EsapiLogger.debug(esapiLogger, "tenant %s matched tenant validation regex of \"%s\"", tenantKey, regex);
            return true;
        } else
        {
            EsapiLogger.warning(esapiLogger, "tenant %s didn't match tenant validation regex of \"%s\"", tenantKey, regex);
            return false;
        }
    }

    /**
     * Return the ImmutableConfig at the given path using a builder object.
     * @param path the path of the config file
     * @return the ImmutableConfiguration. ImmutableConfiguration is an interface implemented by most Configuration types.
     */
    private static ImmutableConfiguration getConfig(String path) throws CobraException
    {
        try
        {
            return getConfigBuilder(path).getConfiguration();
        }
        catch (ConfigurationException e)
        {
            logger.error("Could not get Config at path: {}. e = {}", path, e.getMessage());
            throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.FAILED_TO_LOAD_CONFIGS, path), e);
        }
    }

    public static String getConfigPath()
    {
        String jettyBase = Tools.getJettyPath();

        if (jettyBase != null)
        {
            jettyBase += "/config/";
        }

        return jettyBase;
    }

    /**
     * Search for and create a builder object based on the config file at the path passed. Different file extensions use different
     * builders managed by the library.
     * @param path the path where the config file is stored on disk.
     * @return the builder object associated with that config.
     * @throws ConfigurationException if the builder cannot be created
     */
    public static ReloadingFileBasedConfigurationBuilder getConfigBuilder(String path) throws CobraException
    {
        if (!Files.isRegularFile(Paths.get(path))){
            logger.error("Path for configuration does not point to a valid file");
            throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.TENANT_CONFIG_FILE_NOT_FOUND, path));
        }
        File configFile = new File(path);
        if (!configFile.exists())
        {
            logger.error("Couldn't find config file");
            throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.TENANT_CONFIG_FILE_NOT_FOUND, path));
        }

        FileBasedBuilderParameters params = new Parameters()
                .fileBased()
                .setEncoding("UTF-8")
                .setFileName(path);

        if ("json".equals(FilenameUtils.getExtension(path)))
        {
            ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> reloadingBuilder =
                    new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(WebApiJSONConfiguration.class)
                            .configure(params);
            reloadingBuilder.addEventListener(ConfigurationBuilderEvent.CONFIGURATION_REQUEST,
                    configurationBuilderEvent -> {
                        logger.debug("RELOADING JSON CONFIG!!!");
                        reloadingBuilder.getReloadingController()
                                        .checkForReloading(null);
                    });
            return reloadingBuilder;
        }
        throw new IllegalArgumentException("Configuration file type not supported");
    }

    public synchronized Object getProperty(String key)
    {
        return webApiConfig.getProperty(key);
    }

    public static String getPresentConfigFile(String path, String key)
    {
        List<String> extensions = WebApiConfigManager.getInstance().getList(String.class, WebApiConfig.Property.TENANT_CONFIG_SUFFIX_SEARCH_ORDER.key);
        return getPresentConfigFile(path, key, extensions);
    }

    /**
     * Check for a config file of various extensions at the given path. Currently only supports .json
     * @param key the path without the file extension
     * @return the full path including the file extension
     */
    public static String getPresentConfigFile(String path, String key, List<String> extensions)
    {
        File f = new File(path);
        ArrayList<String> names = new ArrayList<>(Arrays.asList(f.list()));

        for (String extension : extensions)
        {
            for (String name : names)
            {
                String filenameToTest = key + extension;
                EsapiLogger.trace(esapiLogger, "filename = %s filenameToTest = %s", name, filenameToTest);
                if(name.equals(filenameToTest))
                {
                    return Paths.get(path, name).toString();
                }
            }
        }

        logger.error("No valid config found for key");
        return path; //like null, but not.
    }

    private static Configuration getKeyVaultConfig(CombinedConfiguration webApiConfig, WebApiConfig.Property keyVaultSecretType) {
        if (webApiConfig == null) {
            return null;
        }

        SecretCacheProvider secretCacheProvider = SecretCacheProvider.getInstance();
        String keyVaultSecretName = webApiConfig.getString(keyVaultSecretType.key);
        String keyVaultUri = webApiConfig.getString(WebApiConfig.Property.KEY_VAULT_VAULT_URI.key);
        String keyVaultClientId = webApiConfig.getString(WebApiConfig.Property.KEY_VAULT_CLIENT_ID.key);

        if (keyVaultSecretName != null && keyVaultUri != null && keyVaultClientId != null) {
            JsonNode secretsJsonNode = secretCacheProvider.getSecretFromKeyVault(keyVaultSecretName, webApiConfig);
            if (secretsJsonNode != null) {
                JSONObject secretsJsonObject  = new JSONObject(secretsJsonNode.toString());
                Map<String, Object> keyVaultSecretsMap = secretsJsonObject.toMap();

                WebApiJSONConfiguration keyVaultConfig = new WebApiJSONConfiguration();
                keyVaultConfig.load(keyVaultSecretsMap);

                return keyVaultConfig;
            }
        } else {
            logger.debug("There are missing key vault attributes");
        }

        return null;
    }
}
