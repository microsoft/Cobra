package com.nuance.cobra.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class WebApiPlatformServlet
        extends HttpServlet
{
    private static final Logger logger = LogManager.getLogger(WebApiPlatformServlet.class);

    private ServletConfig servletConfig;

    /**
     * Called on first access (and on first access after servlet re-deploy)
     * Perform servlet initialization here
     * e.g.
     *  - Process Virtual Machine System Properties
     *  - Read Config Files
     *  - Open Databases
     *  - Start Cleanup Threads
     *  - Start Executor Services
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {

        /* NOTE!!!!!!!!!!!!!!!!!!!!!!!
        Use ContextEventListener to initialize start-up services
         */
        super.init(config);
        servletConfig = config;
        logger.warn("{} servlet starting!", servletConfig.getServletName());

    }

    /**
     * Called on servlet shutdown if servlet has been accessed (including re-deploy)
     * Perform servlet cleanup here
     * e.g.
     *  - Close Databases
     *  - Stop Cleanup Threads
     *  - Stop Executor Services
     */
    @Override
    public void destroy()
    {
        logger.warn("{} servlet being destroyed!", servletConfig.getServletName());
        super.destroy();
    }

}
