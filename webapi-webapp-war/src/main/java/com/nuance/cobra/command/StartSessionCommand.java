package com.nuance.cobra.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.*;
import com.nuance.cobra.client.response.StartSessionResponse;
import com.nuance.cobra.session.*;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.logging.EsapiLogger;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.EDF.CommandLog;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.esapi.ESAPI;

import java.util.List;

/**
 * StartSessionCommand command.
 *
 * Refer to and update docs/webapi-guides/nuance-webapi-platform-api-guide/source/includes/start-session-command.md
 * for documentation.
 */

public class StartSessionCommand
        extends Command
        implements EDFResponse {

    private static final Logger logger = LogManager.getLogger(StartSessionCommand.class);
    private static final org.owasp.esapi.Logger esapiLogger = ESAPI.getLogger(StartSessionCommand.class);

    //The session ID of the current session
    private String sessionId;

    /**
     * Initializes the command object
     *
     * @param request The request received by cobra
     * @param commandConfigs The command configuration combined with the tenant config
     * @param chainer Reference to the command chainer, if any
     */
    public StartSessionCommand(Request request, CombinedConfiguration commandConfigs, CommandChainer chainer) {
        super(request, commandConfigs, chainer);
    }

    /**
     * Starts the command
     */
    @Override
    public void executeImplementation() {
        logger.trace("StartSessionCommand executeImplementation");
        final var sessionManager = SessionManager.getInstance();
        IResponseSender closer = getResponseHandler().getSender();
        Session session = null;

        try {
            session = sessionManager.startSession(request.requestCommand.parameters, commandConfigs);
            if (session == null){
                throw new InvalidConfigurationException("Tenant Configuration could not be loaded, applicationKey is either null or empty.");
            }
            LoggingUtils.setThreadContext(session);

            session.setCommand(this);
            sessionId = session.getSessionId();

            EsapiLogger.debug(esapiLogger, "parameters: %s", request.requestCommand.parameters.toString());

            //Creation of the client factory, using a method in order to mock during tests
            var clientFactory = getClientFactory();

            final CombinedConfiguration requestConfig = session.getSessionConfig();
            var sessionClient = clientFactory.createSessionClient(requestConfig, sessionId);

            if (isRequestValid(requestConfig)) {
                logger.debug("startSession request is valid, sending to sessionClient");
                sessionClient.startSession(getResponseHandler(), requestConfig);
                //Need to store the sender which implements the apiConnectionCloser. Needed if a session times out, it
                // will be used to close the connection.
                session.setProperty(Session.SessionPropertyKeys.APICONNECTIONCLOSER, closer);
            } else {
                String verificationCode = session.getSessionConfig()
                        .getString(WebApiConfig.Property.VERIFICATION_CODE.key);
                logger.error("Failed to validate the verification code, and verificationCode: {}",
                        verificationCode);
                var startSessionResponse = new StartSessionResponse(ResponseCode.createResponseError(
                        ExternalResponseErrorCode.INVALID_VERIFICATION_CODE_ERROR));
                sendErrorResponse(startSessionResponse);
                closeConnection(session, closer);
            }
        } catch (InvalidConfigurationException e) {
            String errorGist;
            if (e.getMessage().equalsIgnoreCase("Tenant Configuration could not be parsed: applicationKey is either null or empty.")){
                errorGist = e.getMessage();
            } else {
                errorGist =
                        "Caught an InvalidConfigurationException while processing startSession command. The applicationKey's tenant configuration did not pass validation (e.g. missing required keys)";
            }
            logger.error("{} Exception: {}", errorGist, e.getMessage());
            var startSessionResponse = new StartSessionResponse(ResponseCode.createResponseError(
                    ExternalResponseErrorCode.START_SESSION_COMMAND_EXECUTE_ERROR, errorGist));
            sendErrorResponse(startSessionResponse);
            closeConnection(session, closer);
        } catch (IllegalArgumentException e) {
            var startSessionResponse = new StartSessionResponse(
                    ResponseCode.createResponseError(ExternalResponseErrorCode.ILLEGAL_PARAMETER_ERROR,
                            e.toString()));
            sendErrorResponse(startSessionResponse);
            closeConnection(session, closer);
        } catch (CobraException e) {
            List<ResponseCode> errors = e.getResult().getErrors();
            var startSessionResponse = new StartSessionResponse(errors.toArray(new ResponseCode[errors.size()]));
            sendErrorResponse(startSessionResponse);
            closeConnection(session, closer);
        }
    }

    private void closeConnection(Session session, IResponseSender closer)
    {
        if (session != null) session.setCommand(null);
        sessionId = null;
        if (closer != null) closer.closeConnection(null);
    }

    @Override
    public String formatResponse(Object clientResponse)
            throws CobraException {
        logger.trace("StartSessionCommand formatResponse");
        //Creating the response based on the request, it automatically set the type, the sessionId and the command
        var response = Response.fromRequest(request);

        if(clientResponse instanceof StartSessionResponse &&
                ((StartSessionResponse)clientResponse).getErrors() != null &&
                ((StartSessionResponse)clientResponse).getErrors().stream().count() > 0 &&
                ((StartSessionResponse)clientResponse).getErrors().get(0).getCode() == ExternalResponseErrorCode.INVALID_VERIFICATION_CODE_ERROR.getValue()
        ) {
            response.setSessionId(null);
        }
        else {
            response.setSessionId(sessionId);
        }
        response.setResult((ResponseResult) clientResponse);

        if (response.getResult().isFinal()) {
            reportBillableEvent(response);
        }

        return response.toJson();
    }

    /**
     * Implementation of the receiver in order to process the response and remove the command from the session if needed
     *
     * @param response
     *    The formatted response received from the client
     */
    @Override
    public boolean onResponseImplementation(ResponseResult response)
    {
        if (response instanceof StartSessionResponse &&
            response.isFinal() &&
            response.getStatus() == ResponseResult.ResponseStatus.SUCCESS)
        {
            SessionManager.getInstance()
                          .getSession(sessionId, false)
                          .setCommand(null);
        }
        return true;
    }

    /**
     * Method for validating the verificationCode of the incoming request. The verification code must be equal to the sha256 representation of the nmaid_key
     *
     * @param config
     *      The config object representing all the params combined between the request and the session configuration
     * @return
     *      Returns a boolean telling if the request is valid (true) or not (false)
     */
    private boolean isRequestValid(CombinedConfiguration config) {
        var validator = new SessionValidator(config);
        return validator.validate();
    }

    protected void sendErrorResponse(ResponseResult response)
    {
        getErrorHandler(logger).handleError(response);

        //startSession failed with an error, need to remove the session from SessionManager
        if (sessionId != null)
        {
            SessionManager.getInstance().endSession(sessionId);
            CobraConfigurationUtils.clearCombinedConfiguration(commandConfigs, sessionId);
        }
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public CommandLog formatLog(IResponse response) {


        CommandLog baseResponse = super.formatLog(response);

            /*
            We need to do this to pretty-print the EDF response without duplicate parameters
             */
        baseResponse.request.response = prettyFormatResponse((Response) response);

        try {
            var baseResponseString = baseResponse.toJson(false);
            logger.debug("{}--EDF Response : {} ", getCommandName(), baseResponseString);
        } catch (JsonProcessingException e) {
            logger.warn("Exception caught at {}", e.getStackTrace());
        }
        return baseResponse;
    }

}