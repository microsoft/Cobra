package com.nuance.cobra.command;


import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.session.WebApiConfigManager;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.webapi.ResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("WeakerAccess")
@PrepareForTest(WebApiConfigManager.class)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class CommandFactoryTest
{
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static final String START_SESSION_COMMAND_NAME = "startSession";
    private static final String START_SESSION_COMMAND_NAME_OFF_CASE = "stArtsEssIOn";
    private static final String END_SESSION_COMMAND_NAME = "endSession";
    private static final String END_SESSION_COMMAND_NAME_OFF_CASE = "endsEssIOn";

    ResponseSender mockResponseSender;
    Request request;
    CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithDefaults();
    Configuration testConf = testConfs.getConfiguration(0);

    @Before
    public void setUp()
            throws Exception
    {
        testConf.setProperty(WebApiConfig.Property.HOST_ENVIRONMENT.key,("pred dev"));
        testConf.setProperty(WebApiConfig.Property.HOST_REGION.key,("mtl"));
        testConf.setProperty(WebApiConfig.Property.API_VERSION.key,("1.0"));

        PowerMockito.mockStatic(WebApiConfigManager.class);
        when(WebApiConfigManager.getInstance()).thenReturn(testConfs);

        mockResponseSender = mock(ResponseSender.class);
    }


    @After
    public void tearDown()
            throws Exception
    {

    }


    @Test
    public void test_constructor()
            throws Exception
    {
        new CommandFactory();
    }

    @Test
    public void test_createCommand_invalidCommand()
            throws Exception
    {
        String requestString = "{ \"type\": \"command\",  \"command\": { \"name\": \"" + START_SESSION_COMMAND_NAME +
                               "\", \"id\": \"1\",    \"parameters\": { \"applicationKey\":\"applicationKey\" , " +
                               "\"verificationCode\":\"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c"
                               + "\" } }}";

        request = Request.fromJson(requestString);
        Command command = CommandFactory.createCommand(request, mockResponseSender, testConfs);
        final ResponseHandler responseHandler = command.getResponseHandler();
        assertNotNull(responseHandler);
        assertTrue(responseHandler.getSender() == mockResponseSender);
        assertNotNull(responseHandler.getFormatter());
        request.requestCommand.name = "blaBlaBlaCommand";

        exception.expect(CobraException.class);
        command = CommandFactory.createCommand(request, mockResponseSender, testConfs);
    }

    @Test
    public void test_createCommand_startSession()
            throws Exception
    {
        String requestString = "{ \"type\": \"command\",  \"command\": { \"name\": \"" + START_SESSION_COMMAND_NAME +
                               "\", \"id\": \"1\",    \"parameters\": { \"applicationKey\":\"applicationKey\" , \"verificationCode\":\"5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c\" } }}";
        request = Request.fromJson(requestString);
        final Command command = CommandFactory.createCommand(request, mockResponseSender, testConfs);
        assertTrue(command instanceof StartSessionCommand);

        final ResponseHandler responseHandler = command.getResponseHandler();
        assertNotNull(responseHandler);

        assertTrue(responseHandler.getSender() == mockResponseSender);

        assertNotNull(responseHandler.getFormatter());
    }

    @Test
    public void test_createCommand_cancelCommand()
            throws Exception
    {
        String requestString = "{ \"type\": \"command\", \"sessionId\": \"2fcc32d0-edf2-4059-9790-4003f4bc1fb6\",  \"command\": { \"name\": \"" + "cancelCommand" +
                "\", \"id\": \"1\" }}";
        request = Request.fromJson(requestString);
        final Command command = CommandFactory.createCommand(request, mockResponseSender, testConfs);
        assertTrue(command instanceof CancelCommandCommand);

        final ResponseHandler responseHandler = command.getResponseHandler();
        assertNotNull(responseHandler);

        assertTrue(responseHandler.getSender() == mockResponseSender);

        assertNotNull(responseHandler.getFormatter());
    }

    //TODO: following test doesn't work since the request cannot be created from json since the command is invalid (validated against schema. Request.fromJson should throw an exception.
/*    @Test
    public void test_createCommand_startSession_offCase() throws Exception
    {

        String requestString = "{ \"type\": \"command\",  \"command\": { \"name\": \"" + START_SESSION_COMMAND_NAME_OFF_CASE + "\", \"id\": \"1\",    \"parameters\": { \"applicationKey\":\"applicationKey\" } }}";

        request = Request.fromJson(requestString);
        final CommandFactory commandFactory = new CommandFactory();

        final Command command = commandFactory.createCommand(request, mockResponseSender);
        assertTrue(command instanceof StartSessionCommand);

        final ResponseHandler responseHandler = command.getResponseHandler();
        assertNotNull(responseHandler);

        assertTrue(responseHandler.getSender() == mockResponseSender);

        assertNotNull(responseHandler.getFormatter());
    }*/

    @Test
    public void test_createCommand_endSession()
            throws Exception
    {
        String requestString =
                "{ \"type\": \"command\", \"sessionId\": \"2fcc32d0-edf2-4059-9790-4003f4bc1fb6\", \"command\": { \"name\": \"" +
                END_SESSION_COMMAND_NAME + "\", \"id\": \"1\" } }";

        request = Request.fromJson(requestString);
        final Command command = CommandFactory.createCommand(request, mockResponseSender, testConfs);
        assertTrue(command instanceof EndSessionCommand);

        final ResponseHandler responseHandler = command.getResponseHandler();
        assertNotNull(responseHandler);

        assertTrue(responseHandler.getSender() == mockResponseSender);

        assertNotNull(responseHandler.getFormatter());
    }

    //TODO: following test doesn't work since the request cannot be created from json since the command is invalid (validated against schema. Request.fromJson should throw an exception.
/*    @Test
    public void test_createCommand_endSession_offCase() throws Exception
    {

        final CommandFactory commandFactory = new CommandFactory();
        String requestString = "{ \"type\": \"command\", \"sessionId\": \"2fcc32d0-edf2-4059-9790-4003f4bc1fb6\", \"command\": { \"name\": \"" + END_SESSION_COMMAND_NAME_OFF_CASE + "\", \"id\": \"1\" } }";

        request = Request.fromJson(requestString);
        final Command command = commandFactory.createCommand(request, mockResponseSender);
        assertTrue(command instanceof EndSessionCommand);

        final ResponseHandler responseHandler = command.getResponseHandler();
        assertNotNull(responseHandler);

        assertTrue(responseHandler.getSender() == mockResponseSender);

        assertNotNull(responseHandler.getFormatter());
    }*/

    @Test
    public void test_createEndSessionRequest()
    {
        try
        {
            String sessionId = "pabloSession";
            Request request = EndSessionCommand.createEndSessionRequest(sessionId);
            assertEquals(request.type, Request.RequestType.COMMAND);
            assertEquals(request.requestCommand.name.toLowerCase(), CommandFactory.getCommandName(EndSessionCommand.class).toLowerCase());
            assertEquals(request.sessionId, sessionId);
        }
        catch (Exception unused)
        {
            //Should not be triggered
        }
    }
}