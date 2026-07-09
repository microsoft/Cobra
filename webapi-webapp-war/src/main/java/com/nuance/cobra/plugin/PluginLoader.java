package com.nuance.cobra.plugin;

import com.nuance.cobra.api.endpoint.WebApiEndpoint;
import com.nuance.cobra.logging.EdfFluentbitListener;
import com.nuance.cobra.logging.EdfLogForwarder;
import com.nuance.cobra.logging.edf.EdfMessageQueueable;
import com.nuance.cobra.logging.eventhub.EventHubListener;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.Tools;
import com.nuance.cobra.utils.config.WebApiJSONConfiguration;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.plugin.v1.IWebApiCommandPlugin;
import com.nuance.plugin.v1.IWebApiRawPlugin;
import com.nuance.plugin.v1.IWebApiSystemPlugin;
import com.nuance.plugin.v1.IWebApiTenantPlugin;
import com.nuance.plugin.v1.shareable.IResponseSender;
import com.nuance.plugin.v1.shareable.ISnmpTrapSender;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import com.nuance.plugin.v1.shareable.edf.IEdfLogPoster;
import com.nuance.plugin.v1.shareable.edf.IEdfMessage;
import com.nuance.plugin.v1.shareable.edf.IEdfV2Message;
import com.nuance.plugin.v1.shareable.edf.INeapMessage;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.PatternSyntaxException;

public class PluginLoader {
    private static final Logger logger = LogManager.getLogger();
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(PluginLoader.class);

    private static final Map<String, Object> multitoneCache = new HashMap();
    private static final List<IWebApiSystemPlugin> systemPlugins = new LinkedList<>();
    private static final File pluginsPath = new File(Tools.getJettyPath() + "/plugins");
    private static final String PLUGIN_PARAMS_NAME = "pluginSettings";

    private static File[] installedPluginsList() {

        return pluginsPath.listFiles(file -> {
            Path p = Paths.get(file.getPath());
            String filename = p.getFileName().toString().toLowerCase();
            return filename.endsWith(".jar");
        });
    }

    public static void startSystemPlugins () throws MalformedURLException {
        File[] installedPlugins = installedPluginsList();
        URL[] urls;
        if (installedPlugins != null) {
            urls = new URL[installedPlugins.length];
            for (int i = 0; i < installedPlugins.length; i++) {
                urls[i] = installedPlugins[i].toURI().toURL();
            }
        } else {
            urls = new URL[0];
        }

        URLClassLoader ucl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        PluginServiceLoader<IWebApiSystemPlugin> pluginLoader = PluginServiceLoader.load(IWebApiSystemPlugin.class, ucl);

        for (IWebApiSystemPlugin plugin : pluginLoader) {
            String jarFileName = (new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile())).getName();
            jarFileName = jarFileName.substring(0, jarFileName.lastIndexOf("."));
            if (plugin.onServerStart(buildWebApiFeatures(null, jarFileName))) {
                systemPlugins.add(plugin);
            }
        }
    }

    public static void stopSystemPlugins () {
        for (IWebApiSystemPlugin plugin: systemPlugins) {
            plugin.onServerStop();
        }
    }

    public static List<WebApiPlugin<IWebApiCommandPlugin>> loadCommandPlugins(String tenant) throws IOException {
        return loadPlugins(IWebApiCommandPlugin.class, tenant);
    }

    public static List<WebApiPlugin<IWebApiRawPlugin>> loadRawPlugins(String tenant) throws IOException {
        return loadPlugins(IWebApiRawPlugin.class, tenant);
    }

    private static <T extends IWebApiTenantPlugin> List<WebApiPlugin<T>> loadPlugins(Class<T> type, String tenant) throws IOException {
        String cacheKey = type.getName(); //have different cache for different plugin types.
        List<WebApiPlugin<T>> result = new ArrayList<>();

        //check to see if we have loaded this jars before.
        //If no then load them.
        //If yes then just create a new instance of the class.
        if (!multitoneCache.containsKey(cacheKey)) {
            synchronized (PluginLoader.class) {
                if (!multitoneCache.containsKey(cacheKey)) {
                    List<Object> tenantPlugins;

                    try {
                        WebApiJSONConfiguration config = WebApiConfigManager.getConfigForTenant(tenant);
                        tenantPlugins = config.getList(WebApiConfig.Property.TENANT_ACTIVE_PLUGINS_LIST.key);

                        List<String> tenantPluginsNames = new ArrayList<>();

                        for (Object pluginItem : tenantPlugins) {
                            HashMap<String, Object> pluginInfo = (LinkedHashMap<String, Object>)pluginItem;
                            String jarName = (String) pluginInfo.get("jarName");
                            if (jarName != null)
                                tenantPluginsNames.add(jarName.toLowerCase());
                        }

                        File[] pluginJars = installedPluginsList();
                        List<File> pluginJarsFound = new ArrayList<>();

                        if (pluginJars != null) {
                            for (String pluginName : tenantPluginsNames) {
                                boolean foundIt = false;
                                for (File pluginJar : pluginJars) {
                                    Path path = Paths.get(pluginJar.getPath());
                                    String filename = path.getFileName().toString().toLowerCase();
                                    if (filename.matches(pluginName)) {
                                        pluginJarsFound.add(pluginJar);
                                        foundIt = true;
                                        break;
                                    }
                                }

                                if (!foundIt) {
                                    EsapiLogger.error(esapiLogger, "Tenant %s: plugin %s not found on disk.", tenant, pluginName);
                                }
                            }
                        }

                        URL[] urls = new URL[pluginJarsFound.size()];
                        for (int i = 0; i < pluginJarsFound.size(); i++) {
                            urls[i] = pluginJarsFound.get(i).toURI().toURL();
                        }


                        URLClassLoader ucl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
                        PluginServiceLoader<T> pluginLoader = PluginServiceLoader.load(type, ucl);

                        for (T plugin : pluginLoader) {
                            URL jarLocation = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
                            Path p = Paths.get(jarLocation.getFile());
                            String filename = p.getFileName().toString().toLowerCase();

                            for (String pluginName : tenantPluginsNames) {
                                if (filename.matches(pluginName)) {
                                    plugin.onInitPlugin(buildWebApiFeatures(tenant, pluginName));
                                    WebApiPlugin<T> pluginWrapper = new WebApiPlugin<T>(plugin, tenant, pluginName);
                                    result.add(pluginWrapper);
                                }
                            }
                        }

                    } catch (CobraException | PatternSyntaxException e) {
                        logger.warn(e);
                        throw new IOException(e);
                    }

                    multitoneCache.put(cacheKey, result);
                }
            }
        } else {
            for (WebApiPlugin<T> pluginOrigin : (List<WebApiPlugin<T>>) multitoneCache.get(cacheKey)) {
                try {
                    IWebApiTenantPlugin plugin = pluginOrigin.getPlugin().getClass().getDeclaredConstructor().newInstance();
                    plugin.onInitPlugin(buildWebApiFeatures(tenant, pluginOrigin.getPluginName()));
                    WebApiPlugin<T> pluginWrapper = new WebApiPlugin<T>((T) plugin, tenant, pluginOrigin.getPluginName());
                    result.add(pluginWrapper);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    logger.warn(e);
                    throw new IOException(e);
                }
            }
        }
        return result;
    }

    // This should be implemented in its own class, or maybe a factory pattern.
    // (As a start we give it package visibility so it's unit-testable.)
    static IWebApiFeatures buildWebApiFeatures(final String tenantName, final String pluginName) {
        return new IWebApiFeatures() {
            CombinedConfiguration combinedConfiguration = null;

            @Override
            public CombinedConfiguration getConfigs() throws IOException {
                return getConfigs(tenantName);
            }

            @Override
            public CombinedConfiguration getConfigs(String pTenantName)
                    throws IOException
            {
                if (pTenantName == null) {
                    return WebApiConfigManager.getInstance();
                }

                if(combinedConfiguration != null
                        && !pTenantName.equals(combinedConfiguration.getString(WebApiConfig.Property.APPLICATION_KEY.key)))
                {
                    close();
                }

                if (combinedConfiguration == null) {
                    try
                    {
                        combinedConfiguration = buildPluginConfigs(pTenantName, pluginName);
                    }
                    catch (CobraException e)
                    {
                        logger.error("Configuration loading failed.", e);
                        throw new IOException(e);
                    }
                }
                return combinedConfiguration;
            }

            @Override
            public void close()
            {
                CobraConfigurationUtils.clearCombinedConfiguration(combinedConfiguration, null);
                combinedConfiguration = null;
            }

            @Override
            public ISnmpTrapSender getSnmpTrapSender(CombinedConfiguration configs) {
                return new SnmpResponseTrapSender(configs);
            }

            @Override
            public IEdfLogPoster getEdfPoster(CombinedConfiguration configs) {
                return new IEdfLogPoster() {
                    @Override
                    public void publishEdfLog(IEdfMessage log) {
                        EdfLogForwarder logForwarder = getEdfLogForwarder();

                        EdfMessageQueueable edfMessage;
                        edfMessage = new PluginEdfV1MessageQueueable(log, tenantName, configs);
                        logForwarder.forwardLog(edfMessage, configs);
                    }

                    @Override
                    public void publishEdfLog(IEdfV2Message log)
                    {
                        EdfLogForwarder logForwarder = getEdfLogForwarder();

                        EdfMessageQueueable edfMessage;
                        edfMessage = new PluginEdfV2MessageQueueable(log, tenantName, configs);
                        logForwarder.forwardLog(edfMessage, configs);
                    }

                    @Override
                    public void publishNeapLog(INeapMessage log)
                    {
                        EdfLogForwarder logForwarder = getEdfLogForwarder();

                        EdfMessageQueueable edfMessage;
                        edfMessage = new PluginNeapMessageQueueable(log, tenantName, configs);
                        logForwarder.forwardLog(edfMessage, configs);
                    }
                };
            }

            @Override
            public IWebApiEndpoint getWebApiEndpoint(IResponseSender responseSender) throws IOException {
                return new WebApiEndpoint(responseSender, getConfigs());
            }
        };
    }


    protected static EdfLogForwarder getEdfLogForwarder() {
        CombinedConfiguration cobraConfig = WebApiConfigManager.getInstance();
        EdfLogForwarder edfLogForwarder = new EdfLogForwarder();
        if(cobraConfig.getBoolean(WebApiConfig.Property.ENABLE_FLUENT_LOG_POSTER.key))
        {
            edfLogForwarder.addListener(new EdfFluentbitListener());
        }
        if(cobraConfig.getBoolean(WebApiConfig.Property.ENABLE_EVENTHUB_LOG_POSTER.key))
        {
            edfLogForwarder.addListener(new EventHubListener());
        }
        return  edfLogForwarder;
    }


    public static CombinedConfiguration buildPluginConfigs(final String tenantName, final String pluginName) throws CobraException {
        if (tenantName == null) {
            return WebApiConfigManager.getInstance();
        }

        CombinedConfiguration pluginConfig = WebApiConfigManager.getCombinedConfigForTenant(tenantName);
        List<Object> tenantPlugins = pluginConfig.getList(WebApiConfig.Property.TENANT_ACTIVE_PLUGINS_LIST.key);

        for (Object pluginItem : tenantPlugins) {
            HashMap<String, Object> pluginInfo = (LinkedHashMap<String, Object>)pluginItem;
            String jarName = (String) pluginInfo.get("jarName");
            if (jarName != null && jarName.equalsIgnoreCase(pluginName)) {
                WebApiJSONConfiguration pluginSettings = new WebApiJSONConfiguration();
                pluginConfig.addConfiguration(pluginSettings, PLUGIN_PARAMS_NAME);
                LinkedHashMap<String, Object> settings = (LinkedHashMap<String, Object>)pluginInfo.get("settings");
                pluginSettings.mergeMap(settings);
                break;
            }
        }

        return pluginConfig;
    }
}
