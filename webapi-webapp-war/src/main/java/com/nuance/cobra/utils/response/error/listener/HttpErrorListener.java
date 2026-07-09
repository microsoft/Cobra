package com.nuance.cobra.utils.response.error.listener;

import com.nuance.cobra.api.response.IResponse;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.response.HttpAlarmSender;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpErrorListener
        implements ThrottledErrorListener
{
    private static final Logger logger = LogManager.getLogger(HttpErrorListener.class);
    private static ExecutorService executor = Executors.newCachedThreadPool(); //this thread pool will resize according to load.

    private HttpAlarmSender alarmSender = null;

    public HttpErrorListener()
    {
        final String endpointUrl = WebApiConfigManager.getInstance().getString(WebApiConfig.Property.HTTP_ALARM_ENDPOINT_URL.key);
        if (endpointUrl != null) {
            alarmSender = new HttpAlarmSender();
        }
    }

    @Override
    public void notify(IResponse event)
    {
        final CombinedConfiguration srvConf = WebApiConfigManager.getInstance();

        if (alarmSender == null) {
            return;
        }

        executor.submit(
                new ThreadContextRunnable() {
                    @Override
                    public void runWithContext() {
                        alarmSender.sendAlarm(event, srvConf);
                    }
                }
        );
    }
}
