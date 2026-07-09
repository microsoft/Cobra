package com.nuance.cobra.status;

import com.nuance.cobra.api.http.HttpEndpoint;
import com.nuance.cobra.api.response.InternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.ClientFactory;
import com.nuance.cobra.client.HealthCheckClient;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.HealthCheckException;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/status")
public class StatusServlet extends HttpServlet
{
    private static final Logger logger = LogManager.getLogger(StatusServlet.class);

    private static final String RESPONSE_STRING = "STATUS OK";
    private static ResponseErrorHandler errorHandler;

    public StatusServlet()
    {
        this(true);
    }

    public StatusServlet(boolean withSnmpListener)
    {
        super();
        errorHandler = new ResponseErrorHandler();
        errorHandler.addListener(new LoggerErrorListener(logger));
        if (withSnmpListener)
        {
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());
        }
    }

    private ResponseErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        final StatusUtils statusUtils = new StatusUtils();
        final Map<String, StatusUtils.ServiceData> services = statusUtils.readServices(logger);

        final ClientFactory clientFactory = new ClientFactory();
        for (final Map.Entry<String, StatusUtils.ServiceData> entry : services.entrySet())
        {
            final StatusUtils.ServiceData data = entry.getValue();
            if ( ! data.required)
            {
                continue;
            }

            final String name = entry.getKey();
            final String type = data.type;
            logger.trace("Checking service -- name: {}, type: {}", name, type);

            try
            {
                final HealthCheckClient client = clientFactory.createHealthCheckClient(type, data.parameters);
                client.checkHealth();
            }
            catch (HealthCheckException e)
            {
                logger.error("Required service unavailable -- name: {}, type: {} ... maybe others too", name, type);
                ResponseResult responseResult = new ResponseResult(ResponseCode.createResponseError(
                        InternalResponseErrorCode.MONITORING_CHECK_FAILED, name, type));
                getErrorHandler().handleError(responseResult);
                try
                {
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    return;
                }
                catch(Exception ex){
                    HttpEndpoint.quietErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                }   
            }
        }
        try
        {
            response.getWriter().println(RESPONSE_STRING);
        }
        catch(Exception e){
            HttpEndpoint.quietErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

}
