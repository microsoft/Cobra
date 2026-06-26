package com.nuance.cobra.servlet;

import com.nuance.cobra.client.mix.MixAccessToken;
import com.nuance.cobra.client.mix.MixClientCredentials;
import com.nuance.cobra.client.mix.MixClientCredentialsManager;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;

public class AuthTokenCacheServlet
        extends HttpServlet
{
    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
    {
        try {
            final String authorization = req.getHeader("Authorization");
            if (authorization == null) {
                sendError(resp, "Missing authorization entry.", 401);
                return;
            }

            String authDecode = new String(Base64.getDecoder().decode(authorization.split(" ")[1]));

            StringBuilder form = new StringBuilder();
            String scope = "";
            String authUri = ESAPI.httpUtilities().getHeader(req, "Mix-Auth-Uri");

            Enumeration<String> parameterNames = req.getParameterNames();
            while(parameterNames.hasMoreElements())
            {
                String key = ESAPI.encoder().encodeForHTML(parameterNames.nextElement());
                if (key.equalsIgnoreCase("scope")) {
                    scope = ESAPI.httpUtilities().getParameter(req, key);
                } else {
                    form.append(key)
                            .append("=")
                            .append(ESAPI.httpUtilities().getParameter(req, key))
                            .append("&");
                }
            }
            form.append("scope="); //this is such a hack.

            String appID = URLDecoder.decode(authDecode.split(":")[0], StandardCharsets.UTF_8);
            List<String> secret = List.of(URLDecoder.decode(authDecode.split(":")[1], StandardCharsets.UTF_8));
            MixClientCredentials mixClientCredentials = new MixClientCredentials(appID, secret, scope, form.toString(), authUri);
            MixAccessToken mixToken = MixClientCredentialsManager.getMixAccessToken(mixClientCredentials);

            ServletOutputStream out = resp.getOutputStream();
            resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            if (mixToken != null)
            {
                out.write(mixToken.getResponseJson().toString().getBytes(StandardCharsets.UTF_8));
                out.flush();
                out.close();
            } else {
                sendError(resp, "No valid auth found.", 404);
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to send response back.", e);
        }
    }

    private void sendError(HttpServletResponse resp, String message, int code) throws IOException {
        resp.setStatus(code);
        PrintWriter out = resp.getWriter();
        resp.setContentType(ContentType.TEXT_PLAIN.getMimeType());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        out.print(message);
        out.flush();
    }
}
