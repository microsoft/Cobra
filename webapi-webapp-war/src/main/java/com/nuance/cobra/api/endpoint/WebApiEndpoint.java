package com.nuance.cobra.api.endpoint;

import com.google.re2j.Pattern;
import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.Response;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.api.response.ResponseResult;
import com.nuance.cobra.client.globalregistry.GlobalRegistryClient;
import com.nuance.cobra.client.mix.MixClientCredentialsManager;
import com.nuance.cobra.command.*;
import com.nuance.cobra.logging.EdfFluentbitListener;
import com.nuance.cobra.logging.EdfLogForwarder;
import com.nuance.cobra.logging.eventhub.EventHubListener;
import com.nuance.cobra.session.*;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.LoggingUtils;
import com.nuance.cobra.utils.ScheduledTaskUtils;
import com.nuance.cobra.utils.UrlUtils;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.http.CachingHttpClient;
import com.nuance.cobra.utils.metrics.MetricsConstants;
import com.nuance.cobra.utils.response.LogResponseSender;
import com.nuance.cobra.utils.response.ResponseErrorHandler;
import com.nuance.cobra.utils.response.SnmpResponseTrapSender;
import com.nuance.cobra.utils.response.error.listener.HttpErrorListener;
import com.nuance.cobra.utils.response.error.listener.LoggerErrorListener;
import com.nuance.cobra.utils.response.error.listener.ResponseSenderErrorListener;
import com.nuance.cobra.utils.response.error.listener.SnmpErrorListener;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import com.nuance.plugin.v1.shareable.IResponseSender;
import com.nuance.plugin.v1.shareable.IWebApiEndpoint;
import io.prometheus.client.Counter;
import io.prometheus.client.hotspot.DefaultExports;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import javax.servlet.ServletContextEvent;
import javax.websocket.CloseReason;
import javax.websocket.OnMessage;
import javax.websocket.PongMessage;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static javax.websocket.CloseReason.CloseCodes;


//testing fork
public class WebApiEndpoint implements IWebApiEndpoint {
    private static final Logger logger = LogManager.getLogger();
    protected javax.websocket.Session webSocketSession;

    protected String tenantKey = null;
    private final IResponseSender responseSender;
    private final CombinedConfiguration injectedConfig;

    /**
     * Error handling object
     */
    protected ResponseErrorHandler errorHandler;

    /**
     * The member activeSession is the webApi session tied to this endpoint
     * Note the activeSession is needed here to send the audio received from
     * the client application to the the right command. See method onAudio() below
     */
    protected com.nuance.cobra.session.Session activeSession;

    /**
     * Prometheus metrics for recording number of commands executed for the tenant
     */
    public static final Counter commandsCounter = Counter.build()
            .name("webapi_commands_total")
            .help("Commands per tenant")
            .labelNames(MetricsConstants.TENANT_LABEL, MetricsConstants.COMMAND_LABEL)
            .register();

    /**
     * Metrics for calculating number of errors per API per tenant
     */
    public static final Counter commandErrorsCounter = Counter.build()
            .name("webapi_command_errors_total")
            .help("Number of command errors")
            .labelNames(MetricsConstants.TENANT_LABEL, MetricsConstants.COMMAND_LABEL, MetricsConstants.ERROR_CODE_LABEL)
            .register();

    public WebApiEndpoint(IResponseSender responseSender) {
        this(responseSender, null);
    }

    public WebApiEndpoint(IResponseSender responseSender, CombinedConfiguration configs) {
        this.responseSender = responseSender;
        this.injectedConfig = configs;
    }

    CombinedConfiguration getConfigs(String tenantKey) throws CobraException {
        if (activeSession != null &&
                activeSession.getSessionConfig() != null) {
            return activeSession.getSessionConfig();
        }

        if (injectedConfig != null) {
            if (tenantKey == null && injectedConfig.getString(WebApiConfig.Property.APPLICATION_KEY.key, null) == null) {
                return CobraConfigurationUtils.shallowCopyCombinedConfiguration(injectedConfig);
            }

            if (injectedConfig.getString(WebApiConfig.Property.APPLICATION_KEY.key, "").equalsIgnoreCase(tenantKey)) {
                return CobraConfigurationUtils.shallowCopyCombinedConfiguration(injectedConfig);
            }
        }

        return WebApiConfigManager.getCombinedConfigForTenant(tenantKey);
    }

    @Deprecated
    //this method is not thread safe. Use getWebapiSession(SessionID) instead
    public Session getActiveSession() {
        return activeSession;
    }

    public Session getWebapiSession(String sessionID) {
        return SessionManager.getInstance().getSession(sessionID, true);
    }

    /**
     * Get active session if any
     *
     * @param sessionId
     *         The session identifier
     *
     * @return The active session or null if there is none
     */
    private com.nuance.cobra.session.Session getActiveSession(String sessionId)

    {
        com.nuance.cobra.session.Session s;

        try
        {
            s = SessionManager.getInstance()
                              .getSession(sessionId, true);
        }
        catch (NullPointerException e)
        {
            s = null;
        }

        return s;
    }

    private ResponseErrorHandler getErrorHandler(javax.websocket.Session session)
    {
        if (errorHandler == null)
        {
            errorHandler = new ResponseErrorHandler();
            errorHandler.addListener(new ResponseSenderErrorListener(responseSender));
            errorHandler.addListener(new LoggerErrorListener(logger));
            errorHandler.addListener(new SnmpErrorListener(new SnmpResponseTrapSender(WebApiConfigManager.getInstance())));
            errorHandler.addListener(new HttpErrorListener());
        }

        return errorHandler;
    }

    /**
     * End session with Self
     *
     * @param sessionId
     *         The session identifier
     */
    private void endSessionWithSelf(String sessionId, String origin)
    {
        logger.trace("endSessionWithSelf");
        try
        {
            //Cancel the existing command, if any.
            Session session = getActiveSession();
            if(session == null)
            {
                return;
            }

            if(session.getCommand() instanceof EndSessionCommand)
            {
                logger.trace("Letting existing EndSessionCommand proceed.");
                return;
            }

            session.cancelActiveCommand(origin);

            //End the session
            Request endSessionRequest = EndSessionCommand.createEndSessionRequest(sessionId);
            if(endSessionRequest == null)
            {
                logger.debug("Null endSessionRequest. Session already ended.");
                return;
            }
            Command command = CommandFactory.createCommand(endSessionRequest, new LogResponseSender(), getConfigs(endSessionRequest.tenantKey));
            command.execute();
        }
        catch (Exception e)
        {
            logger.error("Exception in endSessionWithSelf: ", e);
        }
    }

    /**
     * Validate directive. The methods checks if the active command id
     * match the command id in the directive
     * *
     *
     * @param request
     *         The request sent by the client application
     */
    private void validateCommandIdAndNameDirective(Request request, Command command)
            throws CobraException
    {
        if (request.requestCommand != null &&
                (!request.requestCommand.id.equals(command.getCommandId()) ||
                 !request.requestCommand.name.equals(command.getCommandName())))
        {
            ResponseCode r =
                    ResponseCode.createResponseError(ExternalResponseErrorCode.INVALID_COMMAND_ID_ERROR);
            throw new CobraException(r);
        }
    }

    /**
     * Send an error response to the client application
     *
     * @param session
     *         The websocket session
     * @param error
     *         The error containing the text message and the code
     */
    protected void onInternalError(javax.websocket.Session session, ResponseCode error, Request request)
    {
        final ResponseResult result = new ResponseResult(error);
        onInternalError(session, result, request);
    }

    /**
     * Close websocket
     *
     * @param session
     *         The websocket session
     * @param error
     *         The reason tho close the websocket
     */
    protected void closeWebsocket(javax.websocket.Session session, String error)
    {
        //Reason Phrase cannot exceed 123 UTF-8 encoded bytes
        String shortError = StringUtils.abbreviate(error, 123);

        try
        {
            if (session.isOpen())
            {
                CloseReason reason = new CloseReason(CloseCodes.CLOSED_ABNORMALLY, shortError);
                session.close(reason);
            }
        }
        catch (Exception e)
        {
            logger.error("Cannot close webscoket - error: ", e);
        }
    }

    /**
     * Send an error response to the client application
     *
     * @param session
     *         The websocket session
     * @param result
     *         The result
     */
    public void onInternalError(javax.websocket.Session session, ResponseResult result, Request request)
    {
        Response response;

        if (request == null)
        {
            response = Response.fromInvalidRequest();
        }
        else if (request.type == Request.RequestType.COMMAND_CHAIN)
        {
            response = Response.fromCommandChainRequest(request, true);
        }
        else
        {
            response = Response.fromRequest(request);
        }

        response.setResult(result);
        onInternalError(session, response, request);
    }

    /**
     * Send an error response to the client application
     *
     * @param session
     *         The websocket session
     * @param response
     *         The response
     */
    private void onInternalError(javax.websocket.Session session, Response response, Request request)
    {
        getErrorHandler(session).handleError(response);

        String tenant = MetricsConstants.UNDEFINED_LABEL_VALUE;
        String command = MetricsConstants.UNDEFINED_LABEL_VALUE;
        String errorCode = MetricsConstants.UNDEFINED_LABEL_VALUE;

        if (request != null) {
            tenant = MetricsConstants.nonNullLabelValue(request.tenantKey);
            command = MetricsConstants.nonNullLabelValue(request.requestCommand.name);

            List<ResponseCode> errors = response.getResult().getErrors();
            if (!errors.isEmpty()) {
                ResponseCode rc = errors.get(0);
                errorCode = Integer.toString(rc.getCode());
            }
        }

        commandErrorsCounter.labels(tenant, command, errorCode).inc();
    }

    /**
     * When a client application opens a websocket connection
     *
     * @param session
     *         The session associated with the websocket connection
     */
    public void onOpen(javax.websocket.Session session)
    {
        Map<String,List<String>> params;

        params = session.getRequestParameterMap();

        String startSessionJson = "";

        this.webSocketSession = session;

        //See if client passed in a header parameter
        if (params != null)
        {
            Pattern pattern = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
            if (params.containsKey("authentication")
                    && params.get("authentication").get(0)!=null
                    && params.get("authentication").get(0).length() % 4 == 0
                    && pattern.matches(params.get("authentication").get(0)))
            {
                startSessionJson = new String(Base64.getDecoder().decode(params.get("authentication").get(0)));

                //rudimentary fail-safe
                if (!startSessionJson.contains("startSession")){
                    String error = "Attempted to start session via WebSocket request, could not";
                    onInternalError(session, ResponseCode.createResponseError(ExternalResponseErrorCode.START_SESSION_COMMAND_GENERIC_ERROR),
                            null);
                    closeWebsocket(session,error);
                    return;
                }

                logger.debug("START SESSION JSON {} " , startSessionJson);

                try {
                    onMessage(session,startSessionJson);
                } catch (Exception e) {
                    logger.warn("There is an exception when executing onMessage ", e);
                }
            }


        }

        logger.debug("WebSocket opened: {}", session::getId);
    }

    /**
     * When a client application sends a message to the web socket
     *
     * @param session
     *         The session associated with the websocket connection
     * @param message
     *         The received message on the websocket
     *
     * @throws Exception
     *         An exception
     */
    @OnMessage
    public void onMessage(javax.websocket.Session session, String message)
            throws IOException
    {
        LoggingUtils.setThreadContext(session);

        String sessionId = LoggingUtils.getWebApiSessionIdForLogging(session);

        // Log
        String messageToLog = message.replaceAll("[\n\r\t]", "_");
        logger.debug("onMessage: Message received for session {}: {}", sessionId, messageToLog);

        // Get request from client
        Request request;
        try
        {
            request = Request.fromJson(message);
        }
        catch (CobraException e)
        {
            if (Request.isCommandChainRequest(message))
            {
                Response response = Response.fromInvalidCommandChainRequest();
                response.setResult(e.getResult());
                onInternalError(session, response, null);
            }
            else
            {
                onInternalError(session, e.getResult(), null);
            }
            return;
        }

        if (request.type == Request.RequestType.COMMAND_CHAIN)
        {
            try
            {
                String firstCommandOfTheCommandChain = Request.toSingleCommandRequestJson(request);
                Request singleCommandRequest = Request.fromJson(firstCommandOfTheCommandChain);
                request.requestCommand = singleCommandRequest.requestCommand;
            }
            catch (CobraException e)
            {
                onInternalError(session, e.getResult(), null);
                CommandChain.sendCommandChainResponse(request,
                        new ResponseResult(ResponseCode.createResponseError(ExternalResponseErrorCode.COMMAND_CHAIN_NEXT_COMMAND_ERROR, e.getMessage())),
                        responseSender);
                return;
            }
        }

        // Get active session for the request
        // If it is a start session command, there is no active session.
        // Then, activeSession == null and will create one
        sessionId = request.sessionId;

        Map<String,Object> sessionParameters = request.sessionParameters;

        try
        {
            activeSession = getActiveSession(sessionId);
            if (activeSession == null
                    && !request.requestCommand.name.equals(CommandFactory.getCommandName(StartSessionCommand.class))
                    && sessionParameters == null)
            {
                // Send error response to client application
                final ResponseResult result = new ResponseResult(ResponseCode.createResponseError(ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR));
                Response response;
                if (request.type == Request.RequestType.COMMAND_CHAIN)
                {
                    response = Response.fromCommandChainRequest(request, true);
                }
                else
                {
                    response = Response.fromRequest(request, true);
                }
                response.setResult(result);
                onInternalError(session, response, request);
                return;
            }

            // Start Session command
            if (request.requestCommand.name.equals(CommandFactory.getCommandName(StartSessionCommand.class)) &&
                request.requestCommand.parameters.get(WebApiConfig.Property.APPLICATION_KEY.key) != null &&
     !((String) request.requestCommand.parameters.get(WebApiConfig.Property.APPLICATION_KEY.key)).trim().isEmpty()) {

                request.tenantKey = (String) request.requestCommand.parameters.get(WebApiConfig.Property.APPLICATION_KEY.key);

            // sessionless
            } else if (sessionParameters != null &&
                       sessionParameters.get(WebApiConfig.Property.APPLICATION_KEY.key) != null &&
            !((String) sessionParameters.get(WebApiConfig.Property.APPLICATION_KEY.key)).trim().isEmpty()) {

                request.tenantKey = (String) sessionParameters.get(WebApiConfig.Property.APPLICATION_KEY.key);

            // session has already been started
            } else if (activeSession != null) {
                request.tenantKey = activeSession.getSessionConfig()
                        .getString(WebApiConfig.Property.APPLICATION_KEY.key);

            // exception
            } else {
                onInternalError(session, ResponseCode.createResponseError(
                        ExternalResponseErrorCode.START_SESSION_COMMAND_EXECUTE_ERROR, "Caught an InvalidConfigurationException while processing startSession command. The applicationKey is invalid."), request);
                return;
            }

            if (activeSession != null)
            {
                ThreadContext.put(ThreadContextRunnable.COMPANY_NAME, (String) activeSession
                        .getSessionConfig()
                        .getProperty(WebApiConfig.Property.DIALOG_MODEL_COMPANY_NAME.key));
                ThreadContext.put(ThreadContextRunnable.APP_NAME, (String) activeSession
                        .getSessionConfig()
                        .getProperty(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key));
                ThreadContext.put(ThreadContextRunnable.TENANT_ID, request.tenantKey);
            }
        }
        catch (IllegalStateException e)
        {
            logger.debug("Session has expired: {}", sessionId);
            // Send error response to client application
            ResponseCode error =
                    ResponseCode.createResponseError(ExternalResponseErrorCode.SESSION_EXPIRED_ERROR, sessionId);
            onInternalError(session, error, request);
            session.close(); //Will implicitly call endSessionWithSelf();
            return;
        }

        if (activeSession == null && sessionParameters != null)
        {

            if (SessionManager.getInstance().getSession(webSocketSession)!=null)
            {
                activeSession = SessionManager.getInstance().getSession(webSocketSession);
                request.sessionId = activeSession.getSessionId();
                request.tenantKey = tenantKey;
                try {
                    handleCommand(session,request);
                } catch (Exception e) {
                    // We should probably handle this exception
                    throw new IOException(e);
                }
            }
            else
            {
                String passedSessionId;
                if (sessionParameters.containsKey(WebApiConfig.Property.CLIENT_SESSION_ID.key))
                {
                    passedSessionId = sessionParameters.get(WebApiConfig.Property.CLIENT_SESSION_ID.key).toString();
                }
                else
                {
                    passedSessionId = SessionManager.createSessionId();
                }
                String tenantKey = sessionParameters.get(WebApiConfig.Property.APPLICATION_KEY.key).toString();
                CombinedConfiguration cobraConfiguration = CobraConfigurationUtils.lookupTenantConfigAndAddParams(tenantKey, sessionParameters);
                SessionValidator validator = new SessionValidator(cobraConfiguration);

                if (validator.validate())
                {
                    activeSession = new com.nuance.cobra.session.Session(passedSessionId,cobraConfiguration);
                    activeSession.setProperty(Session.SessionPropertyKeys.SESSIONLESS_KEY,Boolean.TRUE);
                    SessionManager.getInstance().putWsSession(session,passedSessionId);
                    try {
                        SessionManager.getInstance().putSession(activeSession);
                    } catch (CobraException e) {
                        // We should probably handle this exception
                        throw new IOException(e);
                    }
                    activeSession = SessionManager.getInstance().getSession(passedSessionId,false);
                    request.sessionId = passedSessionId;
                    request.tenantKey = tenantKey;
                    sessionId = passedSessionId;
                    if (sessionId != null)
                    {
                        // Set a flag that tells start session has been called on this connection
                        Map<String, Object> map = session.getUserProperties();
                        map.put(CommandFactory.getCommandName(StartSessionCommand.class), sessionId);

                        //Associate the websocket session and the webapi session
                        SessionManager.getInstance()
                                      .putWsSession(session, sessionId);

                        //Set the websocket maximums
                        setWebsocketMaximums(session);
                    }

                    try {
                        handleCommand(session, request);
                    } catch (Exception e) {
                        // We should probably handle this exception
                        throw new IOException(e);
                    }
                }

            }

        }

        //Only start Session can be run with no active session.
        else
        {

            // Process request based on type
            switch (request.type)
            {
                case DIRECTIVE:
                    handleDirective(session, request);
                    break;

                case COMMAND:
                case COMMAND_CHAIN:
                    try {
                        handleCommand(session, request);
                    } catch (Exception e) {
                        // We should probably handle this exception
                        throw new IOException(e);
                    }
                    break;

                default:
                    // Send error response to client application
                    onInternalError(session,
                            ResponseCode.createResponseError(ExternalResponseErrorCode.UNKNOWN_REQUEST_TYPE_ERROR,
                                    request.type.toString()), request);
            }
        }
    }

    private void handleDirective(javax.websocket.Session session, Request request)
    {
        //In case of a race conditon between message processing (of a directive) and a disconnect, which ends the session
        if(!session.isOpen() || activeSession.isSessionEnded())
        {
            return;
        }

        // Must have a command to execute the directive
        if (activeSession.getCommand() == null)
        {
            // Send error response to client application
            onInternalError(session, ResponseCode.createResponseError(
                    ExternalResponseErrorCode.NO_ACTIVE_COMMAND_ERROR), request);
            return;
        }

        String webApiSessionId = SessionManager.getInstance().getSessionId(session);
        if(webApiSessionId == null ||
           !webApiSessionId.equals(request.sessionId))
        {
            onInternalError(session, ResponseCode.createResponseError(
                    ExternalResponseErrorCode.NO_SESSION_FOUND_ERROR), request);
            return;
        }

        try
        {
            // Execute directive
            validateCommandIdAndNameDirective(request, activeSession.getCommand());
            activeSession.getCommand()
                    .onDirective(request.requestDirective, true, "API");
        }
        catch (CobraException e)
        {
            // Send error response to client application
            onInternalError(session, e.getResult(), request);
            return;
        }
    }

    private void handleCommand(javax.websocket.Session session, Request request)
            throws Exception
    {
        String sessionId;

        if (activeSession == null)
        {
            // Check if a start session has already been called on this connection. If not,
            // set a flag in the websocket session that tells start session has been called
            sessionId = (String) session.getUserProperties()
                                        .get(CommandFactory.getCommandName(StartSessionCommand.class));

            if (sessionId != null && getActiveSession(sessionId) != null)
            {
                // Send error response to client application
                activeSession = getActiveSession(sessionId);
                String error = "Session already exists (" + sessionId + ")";
                request.sessionId = sessionId;
                onInternalError(session, ResponseCode.createResponseError(
                        ExternalResponseErrorCode.START_SESSION_COMMAND_EXECUTE_ERROR, error), request);
                return;
            }
        }
        else
        {

            Command activeCommand = activeSession.getCommand();
            // Must not be an active command already executing unless this is an endSession command.
            // Also preventing to back-to-back endSession command from being sent
            if (activeCommand != null)
            {
                if (activeCommand.getCommandName()
                                 .equals(CommandFactory.getCommandName(EndSessionCommand.class))
                    || ((!request.requestCommand.name.equals(CommandFactory.getCommandName(EndSessionCommand.class)))
                        &&
                        !request.requestCommand.name.equals(CommandFactory.getCommandName(CancelCommandCommand.class))))
                {
                    // Log
                    String commandId = activeCommand.getCommandId();
                    String commandName = activeCommand.getCommandName();
                    logger.warn("Cannot execute the command, there is another active command: id = {} name = {}", commandId, commandName);

                    // Send error response to client application
                    onInternalError(session, ResponseCode.createResponseError(
                            ExternalResponseErrorCode.CONCURRENT_COMMAND_ERROR, commandId, commandName), request);
                    return;
                }
            }


        }

        try
        {
            Command command;
            CombinedConfiguration configForTenant = getConfigs(request.tenantKey);

            switch (request.type)
            {
                case COMMAND:
                    command = CommandFactory.createCommand(request, responseSender, configForTenant);

                    String commandName = request.requestCommand.name;
                    if (null != request.tenantKey && null != commandName)
                    {
                        commandsCounter.labels(request.tenantKey, commandName)
                                .inc();
                    }

                    command.execute();
                    break;

                case COMMAND_CHAIN:
                default:
                    command = new CommandChain(this, session, request, responseSender, configForTenant).execute();
                    break;
            }

            if (request.requestCommand.name.equals(CommandFactory.getCommandName(
                    StartSessionCommand.class))) // TODO for command chains, should we move this block to CommandChain.onResponseWillBeSent() ?
            {
                sessionId = command.getSessionId();
                if (sessionId != null)
                {
                    // Set a flag that tells start session has been called on this connection
                    Map<String, Object> map = session.getUserProperties();
                    map.put(CommandFactory.getCommandName(StartSessionCommand.class), sessionId);

                    //Associate the websocket session and the webapi session
                    SessionManager.getInstance()
                                  .putWsSession(session, sessionId);

                    //Set the websocket maximums
                    setWebsocketMaximums(session);

                    try
                    {
                        // When StartSessionCommand gets executed, it creates the session
                        // We need to retrieve it to associate the session with the endpoint
                        activeSession = getActiveSession(sessionId);
                    }
                    catch (IllegalStateException e) // That would be really unlikely to happen
                    {
                        logger.debug("Session has expired: {}", sessionId);

                        // Send error response to client application
                        ResponseCode error = ResponseCode.createResponseError(
                                ExternalResponseErrorCode.SESSION_EXPIRED_ERROR, sessionId);
                        onInternalError(session, error, request);

                        // Make sure to end session with NCP
                        endSessionWithSelf(sessionId, "Session wasn't created properly");
                        session.close();
                        return;
                    }
                }
            }


        }
        catch (CobraException e)
        {
            // Send error response to client application
            onInternalError(session, e.getResult(), request);
        }
    }

    public void setWebsocketMaximums(javax.websocket.Session session)
    {
        com.nuance.cobra.session.Session webApiSession = SessionManager.getInstance().getSession(session);
        if (webApiSession == null)
        {
            return;
        }

        int maxTextMessageSize = webApiSession.getSessionConfig().getInt(WebApiConfig.Property.WEBSOCKET_MAX_TEXT_MESSAGE_BUFFER_SIZE.key);
        int maxBinaryMessageSize = webApiSession.getSessionConfig().getInt(WebApiConfig.Property.WEBSOCKET_MAX_BINARY_MESSAGE_BUFFER_SIZE.key);
        int maxIdleTimeout = webApiSession.getSessionConfig().getInt(WebApiConfig.Property.WEBSOCKET_MAX_IDLE_TIMEOUT.key);

        logger.debug(String.format("Before: websocket max text buffer size = %d. websocket max binary buffer size = %d. websocket max idle time = %d",
                session.getMaxTextMessageBufferSize(), session.getMaxBinaryMessageBufferSize(), session.getMaxIdleTimeout()));

        session.setMaxTextMessageBufferSize(maxTextMessageSize);
        session.setMaxBinaryMessageBufferSize(maxBinaryMessageSize);
        session.setMaxIdleTimeout(maxIdleTimeout);

        logger.debug(String.format("After: websocket max text buffer size = %d. websocket max binary buffer size = %d. websocket max idle time = %d",
                session.getMaxTextMessageBufferSize(), session.getMaxBinaryMessageBufferSize(), session.getMaxIdleTimeout()));
    }

    /**
     * When the client application sends audio to the websocket
     *
     * @param buffer
     *         The data received from the client application
     *
     * @throws Exception
     *         An exception
     */
    public void onBinary(javax.websocket.Session session, byte[] buffer) {
        String sessionId = LoggingUtils.getWebApiSessionIdForLogging(session);
        ThreadContext.put(ThreadContextRunnable.SESSION_ID, sessionId);

        if (activeSession == null)
        {
            logger.debug("onBinary: No active session. Discarding binary message of size {} bytes.", buffer.length);
            return;
        }

        CombinedConfiguration config = null;
        try
        {
            config = SessionManager.getInstance().getSession(activeSession.getSessionId(),true).getSessionConfig();
        } catch (NullPointerException | IllegalStateException e)
        {
            // Ignore audio when there is no active command.
            // It's not an error when end-pointing stops a recognition command but the client
            // continues streaming until it receives the final recognition.
            logger.debug("onBinary: {}. Discarding binary message of size {} bytes.", e.getMessage(), buffer.length);
            return;
        }

        ThreadContext.put(ThreadContextRunnable.COMPANY_NAME, config.getString(WebApiConfig.Property.DIALOG_MODEL_COMPANY_NAME.key));
        ThreadContext.put(ThreadContextRunnable.APP_NAME, config.getString(WebApiConfig.Property.DIALOG_MODEL_APPLICATION_NAME.key));
        ThreadContext.put(ThreadContextRunnable.TENANT_ID, config.getString(WebApiConfig.Property.APPLICATION_KEY.key));
        logger.debug("onBinary: Consuming binary message of size {} bytes.", buffer.length);
        final Command activeCommand = activeSession.getCommand();
        if (null != activeCommand)
        {
            activeCommand.onAudio(buffer);
        }

    }

    public void onPong(javax.websocket.Session session, PongMessage pongMessage)
            throws IOException
    {
        logger.debug("onPong: Got a pong message for session {}: {}", LoggingUtils.getWebApiSessionIdForLogging(session), pongMessage);
    }

    /**
     * When the websocket connection throws an error
     * If this method is not implemented, we may have this warning:
     * WARN:oejwja.JsrEvents:qtp418304857-19: Unable to report throwable to websocket (no @OnError handler declared)
     *
     * @param session
     *         The session associated with the websocket connection
     * @param throwable
     *         Error thrown by the websocket
     */
    public void onError(javax.websocket.Session session, Throwable throwable)
    {
        String sessionId = LoggingUtils.getWebApiSessionIdForLogging(session);
        String errorMessage = "(no message from Throwable parameter)";

        ThreadContext.put(ThreadContextRunnable.SESSION_ID, sessionId);

        // Get error message if any
        if (throwable == null)
        {
            throwable = new Exception(errorMessage);
        } else if (throwable.getMessage() != null) {
            errorMessage = throwable.getMessage();
        }

        if(errorMessage.contains("Idle timeout expired"))
        {
            logger.info("Websocket {}", errorMessage);
            return;
        }

        // Don't log timeout as a warning
        // Don't log broken pipe as a warning
        // Don't log org.eclipse.jetty.websocket.core.exception.WebSocketTimeoutException: Connection Idle Timeout as a warning
        // Don't log ClosedChannel as a warning
        if (errorMessage.contains("Timeout on Read") ||
            errorMessage.contains("Broken pipe") ||
            errorMessage.contains("Connection Idle Timeout") ||
            throwable instanceof IOException)
        {
            logger.debug("onErr throwable message: {}", throwable.getMessage());
            return;
        }

        //Log a warning
        logger.warn("onErr: Websocket err: {}", errorMessage);
        //Log the throwable
        logger.warn("onErr: Throwable: ", throwable);

        // Let's not log a 1001 just for onError callbacks (Broken pipe, Encode/Decode errors).
        // Also, it may not be necessary to close the connection.
        //    See https://stackoverflow.com/a/27738683 , specifically Pavel Bucek (Tyrus author) mentioning
        //    "unhandled exception thrown from an endpoint"

        //onInternalError(session, ResponseCode.createResponseError(ExternalResponseErrorCode.UNKNOWN_ERROR),null);
        //closeWebsocket(session, errorMessage);
    }

    /**
     * When the websocket connection is closed
     *
     * @param session
     *         The session associated with the websocket connection
     * @param reason
     *         The reason why the connection was closed
     */
    public void onClose(javax.websocket.Session session, CloseReason reason)
    {
        String reasonPhrase = "";

        if (reason != null && reason.getReasonPhrase() != null)
        {
            reasonPhrase = reason.getReasonPhrase();
        }

        int closeCode = -1;
        if (reason != null && reason.getCloseCode() != null)
        {
            closeCode = reason.getCloseCode()
                              .getCode();
        }

        ThreadContext.put(ThreadContextRunnable.SESSION_ID, LoggingUtils.getWebApiSessionIdForLogging(session));


        logger.debug("onClose: Websocket is closing. SessionId = {}. ReasonPhrase = \"{}\". CloseCode = {}",
                LoggingUtils.getWebApiSessionIdForLogging(session), reasonPhrase, closeCode);

        com.nuance.cobra.session.Session webApiSession = SessionManager.getInstance()
                                                                       .getSession(session);
        if (webApiSession != null)
        {
            endSessionWithSelf(webApiSession.getSessionId(), "Websocket connection closed");
        } //else, session was already ended
    }


    @java.lang.SuppressWarnings("java:S1172")
    public static void contextInitialized(ServletContextEvent servletContextEvent)
    {
        BasicConfigurator.configure();

        UrlUtils.initalize();
        DefaultExports.initialize();
        //GlobalRegistryClient init to populate global registry data
        GlobalRegistryClient.getInstance().init();
        SessionManager.getInstance().init();
        MixClientCredentialsManager.init();
        CommandChain.initalize();
    }

    @java.lang.SuppressWarnings("java:S1172")
    public static void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        MixClientCredentialsManager.shutdown();
        CachingHttpClient.shutdown();
        ScheduledTaskUtils.destroy();
        SessionManager.getInstance().destroy();
        getEdfLogForwarder().destroy();
    }

    private static EdfLogForwarder getEdfLogForwarder()
    {
        EdfLogForwarder edfLogForwarder = new EdfLogForwarder();
        edfLogForwarder.addListener(new EventHubListener());
        edfLogForwarder.addListener(new EdfFluentbitListener());
        return edfLogForwarder;
    }

}
