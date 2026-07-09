package com.nuance.cobra.utils.logging;

import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

@Plugin(name="CustomConverter",category="Converter")
@ConverterKeys({"cc"})
public class CustomConverter extends LogEventPatternConverter
{

    private String[] options;
    private static final String INVALID_SESSION_ID = "[xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx]";


    private CustomConverter(String[] options)
    {
        super("cc","cc");
        this.options = options;
    }

    public static CustomConverter newInstance(final String[] options)
    {
        return new CustomConverter(options);
    }

    @Override
    public void format(LogEvent logEvent, StringBuilder stringBuilder) {

        if (options.length!=1)
        {
            return;
        }

        switch (options[0]) {
            case "session.id":
                String sessionId = logEvent.getContextData().getValue(options[0]);
                if (sessionId != null && !sessionId.equals(""))
                {
                    stringBuilder.append("[");
                    stringBuilder.append(sessionId);
                    stringBuilder.append("]");
                }
                else
                {
                    stringBuilder.append(INVALID_SESSION_ID);
                }
                break;
            case "company.name_app.name":
                String companyName = logEvent.getContextData().getValue(ThreadContextRunnable.COMPANY_NAME);
                String appName = logEvent.getContextData().getValue(ThreadContextRunnable.APP_NAME);
                if (companyName != null && appName != null) {
                    stringBuilder.append("[");
                    stringBuilder.append(companyName);
                    stringBuilder.append("_");
                    stringBuilder.append(appName);
                    stringBuilder.append("]");
                }
                break;
            default:
                stringBuilder.append("");
                break;
        }


    }
}
