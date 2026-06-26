package com.nuance.cobra.servlet;

import com.nuance.cobra.utils.validator.TenantConfigValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TenantValidationServlet extends HttpServlet {
    private static Logger logger = LogManager.getLogger(TenantValidationServlet.class);
    private ServletConfig servletConfig;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servletConfig = config;

        logger.info("{} Servlet Started. ", servletConfig.getServletName());
    }

    private void startValidation(HttpServletRequest req, HttpServletResponse resp) {
        TenantConfigValidator tenantValidator = new TenantConfigValidator();
        String userConfig = "";
        String responseString = "";
        resp.setStatus(500);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()))){
            userConfig = br.lines().collect(Collectors.joining());
            userConfig = URLDecoder.decode(userConfig, StandardCharsets.UTF_8);
            JSONObject response = tenantValidator.jsonCompare(new JSONObject(userConfig));
            if(response.length() == 0){
                response.put("Response", "OK");
            }
            responseString = response.toString();
            resp.setStatus(200);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            writeToResponse(resp, responseString);
        }catch (Exception e){
            logger.warn("Servlet Request Failed with error: {}", e.getMessage());
        }
    }

    private void writeToResponse(HttpServletResponse resp, String response) throws IOException
    {
        Encode.forHtmlContent(resp.getWriter(), response);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher("/configValidator.html");
        try{
            dispatcher.forward(req, resp);
        }catch (Exception e){
            logger.warn("There is an exception when executing onHttpGet", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            startValidation(req, resp);
        }
        catch(Exception e){
            logger.warn("There is an exception when executing onHttpPost", e);
        }
    }

    @Override
    public void destroy(){
        logger.warn("{} servlet being destroyed!", servletConfig.getServletName());
        super.destroy();
    }
}
