package com.nuance.ncs.nmsp.servlet;

import com.nuance.ncs.nmsp.servlet.authorization.AuthorizationService;
import com.nuance.ncs.nmsp.servlet.response.ResponseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NcsServlet extends HttpServlet {
	private static final Logger log = LogManager.getLogger(NcsServlet.class);

	private static final long serialVersionUID = 1535648719229287107L;

	public NcsServlet()
	{
		super();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		log.trace("========================================= doPost() start");
		try {
			NcsServletHandler handleRequest = new NcsServletHandler();
			handleRequest.handlePost(request, response);
		}
		catch (Exception e)	{
			log.error("Error: An unhandled exception occurred while handling the request", e);
			ResponseManager.quietSendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occured while processing the request");
		}
		finally {
			log.trace("========================================= doPost() end");
		}
	}
	
	@Override
	public void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try {
			String headerOrigin = request.getHeader(NcsServletDefines.HTTP_REQUEST_ORIGIN_HEADER);
			if(headerOrigin != null)
			{
				if(AuthorizationService.authorizeCrossOrigin(NcsServletDefaults.getAuthorizedCrossOrigins(), headerOrigin))
				{
					response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
					response.addHeader("Access-Control-Max-Age", "320");
					ESAPI.httpUtilities().addHeader(response,"Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
					ESAPI.httpUtilities().addHeader(response,"Access-Control-Allow-Origin", headerOrigin);
				}
				else
				{
					ResponseManager.quietSendError(response, HttpServletResponse.SC_BAD_REQUEST, "The provided origin is not an authorized cross-origin.");
				}
			}
			else {
				super.doOptions(request, response);
			}
		}
		catch (Exception e) {
			log.error("Error: An unhandled exception occurred while handling the request", e);
			ResponseManager.quietSendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request");
		}
	}
}
