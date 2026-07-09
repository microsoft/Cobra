package com.nuance.cobra.status;

import com.nuance.cobra.api.http.HttpEndpoint;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.HealthCheckClient;
import com.nuance.cobra.utils.HealthCheckException;
import com.nuance.cobra.utils.HtmlGen;
import com.nuance.cobra.utils.logging.EsapiLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static com.nuance.cobra.utils.HtmlGen.*;

// TODO [>1.0] We need some way to limit access to this servlet.
//@WebServlet("/services")
public class ServicesServlet extends HttpServlet
{
    private static final Logger logger = LogManager.getLogger(ServicesServlet.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(ServicesServlet.class);


    void doServices(Element table)
    {
        final StatusUtils statusUtils = new StatusUtils();
        final Map<String, StatusUtils.ServiceData> services = statusUtils.readServices(logger);

        final ClientFactory clientFactory = new ClientFactory();
        for (final Map.Entry<String, StatusUtils.ServiceData> entry : services.entrySet())
        {
            final String name = entry.getKey();
            logger.trace("Service name: {}", name);
            final StatusUtils.ServiceData data = entry.getValue();
            final String type = data.type;
            logger.trace("Service type: {}", type);
            logger.trace("Service params: {}", () -> data.parameters.toString());

            final Element row = table.add(TR);
            final Element status = row.add(TD, "&#11044;");
            row.add(TD, name);
            row.add(TD, type);
            row.add(TD, Boolean.toString(data.required).toLowerCase(Locale.ENGLISH));

            try
            {
                final HealthCheckClient client = clientFactory.createHealthCheckClient(type, data.parameters);
                client.checkHealth();
                row.add(TD, "Health check passed");
                status.addAttributes(cls("okay"));
            }
            catch (HealthCheckException e)
            {
                row.add(TD, e.getDisplayMessage());

                if (data.required)
                {
                    status.addAttributes(cls("error"));
                }
                else
                {
                    status.addAttributes(cls("warn"));
                }
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        logger.trace("doGet");
        logger.debug("localAddr: {}", () -> request.getRemoteAddr());
        logger.debug("localPort: {}", () -> request.getLocalPort());
        logger.debug("remoteAddr: {}", () -> request.getRemoteAddr());
        EsapiLogger.debug(esapiLogger, "remoteHost: %s", request.getRemoteHost());
        logger.debug("remotePort: {}", () -> request.getRemotePort());

        final HtmlElement html = HtmlGen.html();
        final HeadElement head = html.head();
        head.stylesheet("services.css");
        head.title("Services");

        final Element body = html.body();

        final Element table = body.add(TABLE);
        final Element header = table.add(TR);
        header.add(TH);
        header.add(TH, "Service Name");
        header.add(TH, "Type");
        header.add(TH, "Req.");
        header.add(TH, "Status");

        doServices(table);

        final String string = html.render();
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        try{
            response.getOutputStream().write(bytes);
        }catch(Exception e){
            HttpEndpoint.quietErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

}
