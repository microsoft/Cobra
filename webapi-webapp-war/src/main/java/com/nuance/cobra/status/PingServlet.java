package com.nuance.cobra.status;

import com.nuance.cobra.api.http.HttpEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/ping")
public class PingServlet extends HttpServlet
{
    private static Logger logger = LogManager.getLogger(PingServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        try{
            response.getWriter().println("PONG");
        }catch(Exception e){
            HttpEndpoint.quietErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            logger.warn("Exception caught at {}", e.getStackTrace());
        }
    }


}
