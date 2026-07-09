package com.nuance.cobra.logging;

import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.json.JSONObject;

import java.nio.charset.Charset;

@Plugin(name = "WapiCustomJsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class WapiCustomJsonLayout  extends AbstractStringLayout
{
    protected WapiCustomJsonLayout( Charset charset )
    {
        super( charset );
    }

    @PluginFactory
    public static WapiCustomJsonLayout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset) {
        return new WapiCustomJsonLayout(charset);
    }

    @Override public String toSerializable( LogEvent event )
    {
        JSONObject contextMap = new JSONObject(event.getContextData().toMap());
        // Replacement of empty or missing session ids by a masked one for ECS
        if(!contextMap.has(ThreadContextRunnable.SESSION_ID) || contextMap.getString(ThreadContextRunnable.SESSION_ID).length() == 0)
        {
            contextMap.put(ThreadContextRunnable.SESSION_ID, "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
        }

        // Json structure mimicking the basic json layout
        // Each line will be a complete json object {"key1":"value1",key2
        JSONObject json = new JSONObject();
        json.put("message", event.getMessage().getFormattedMessage());
        json.put("contextMap", contextMap);
        json.put("level", event.getLevel());
        json.put("timeMillis", event.getTimeMillis());
        json.put("thread", event.getThreadName());
        json.put("threadId", event.getThreadId());
        json.put("threadPriority", event.getThreadPriority());
        json.put("loggerName", event.getLoggerName());
        json.put("loggerFqcn", event.getLoggerFqcn());

        return json.toString() + "\n";
    }
}