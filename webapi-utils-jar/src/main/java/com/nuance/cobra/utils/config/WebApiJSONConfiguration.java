package com.nuance.cobra.utils.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ConfigurationLogger;
import org.apache.commons.configuration2.io.InputStreamSupport;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WebApiJSONConfiguration extends BaseHierarchicalConfiguration implements FileBasedConfiguration, InputStreamSupport {
    private final ObjectMapper mapper = new ObjectMapper();
    private final MapType type;

    public WebApiJSONConfiguration() {
        this.initLogger(new ConfigurationLogger(this.getClass()));
        this.type = this.mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        initMapper();
    }

    public WebApiJSONConfiguration(HierarchicalConfiguration<ImmutableNode> c) {
        super(c);
        initMapper();
        this.initLogger(new ConfigurationLogger(this.getClass()));
        this.type = this.mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
    }

    private void initMapper() {
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public void load(Map<String, Object> map) {
        ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        ImmutableNode top = this.constructHierarchy(rootBuilder, map);
        this.getNodeModel().setRootNode(top);
    }

    protected Map<String, Object> constructMap(ImmutableNode node) {
        Map<String, Object> map = new HashMap(node.getChildren().size());
        Iterator var3 = node.getChildren().iterator();

        while(var3.hasNext()) {
            ImmutableNode cNode = (ImmutableNode)var3.next();
            if (cNode.getChildren().isEmpty()) {
                map.put(cNode.getNodeName(), cNode.getValue());
            } else {
                map.put(cNode.getNodeName(), this.constructMap(cNode));
            }
        }

        return map;
    }

    private ImmutableNode constructHierarchy(ImmutableNode.Builder parent, Map<String, Object> map) {
        Iterator var3 = map.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry)var3.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                ImmutableNode.Builder subtree = (new ImmutableNode.Builder()).name(key);
                ImmutableNode children = this.constructHierarchy(subtree, (Map)value);
                parent.addChild(children);
            } else {
                ImmutableNode leaf = (new ImmutableNode.Builder()).name(key).value(value).create();
                parent.addChild(leaf);
            }
        }

        return parent.create();
    }

    static void rethrowException(Exception e) throws ConfigurationException {
        if (e instanceof ClassCastException) {
            throw new ConfigurationException("Error parsing", e);
        } else {
            throw new ConfigurationException("Unable to load the configuration", e);
        }
    }

    public void read(Reader in) throws ConfigurationException {
        try {
            Map<String, Object> map = (Map)this.mapper.readValue(in, this.type);
            this.load(map);
        } catch (Exception var3) {
            rethrowException(var3);
        }

    }

    public void write(Writer out) throws IOException {
        Object value = this.getNodeModel().getNodeHandler().getRootNode().getValue();

        if (value == null) {
            value = this.constructMap((ImmutableNode)this.getNodeModel().getNodeHandler().getRootNode());
        }

        this.mapper.writer().writeValue(out, value);
    }

    public void read(InputStream in) throws ConfigurationException {
        try {
            Map<String, Object> map = (Map)this.mapper.readValue(in, this.type);
            this.load(map);
        } catch (Exception var3) {
            rethrowException(var3);
        }

    }

    public Map<String, Object> constructMap()
    {
        return this.constructMap(this.getNodeModel().getNodeHandler().getRootNode());
    }

    public void setMapProperty(String key, Map<String, Object> mapProperty)
    {
        Map<String, Object> cfgMap = this.constructMap();
        cfgMap.put(key, mapProperty);
        this.clear();
        this.load(cfgMap);
    }

    public void merge(WebApiJSONConfiguration webApiJSONConfiguration)
    {
        if(webApiJSONConfiguration == null) return;
        mergeMap(webApiJSONConfiguration.constructMap());
    }

    public void mergeMap(Map<String, Object> mapProperty)
    {
        if(mapProperty == null) return;
        Map<String, Object> cfgMap = this.constructMap();
        cfgMap.putAll(mapProperty);
        this.clear();
        this.load(cfgMap);
    }
}
