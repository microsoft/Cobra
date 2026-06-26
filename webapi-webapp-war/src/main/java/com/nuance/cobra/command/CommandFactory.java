package com.nuance.cobra.command;

import com.nuance.cobra.api.request.Request;
import com.nuance.cobra.api.response.ExternalResponseErrorCode;
import com.nuance.cobra.api.response.ResponseCode;
import com.nuance.cobra.utils.CobraException;
import com.nuance.cobra.utils.response.CommandChainer;
import com.nuance.cobra.utils.response.ResponseFormatter;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.cobra.utils.response.ResponseReceiver;
import com.nuance.cobra.utils.webapi.ResponseSender;
import com.nuance.plugin.v1.shareable.IResponseSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;

/**
 * This factory is the one that creates the appropriate command
 * based on the user's request. It will return the command and set
 * up the whole response's handler, sender, formatter and receiver,
 * if needed.
 *
 * @see Command
 * @see ResponseHandler
 * @see ResponseSender
 * @see ResponseFormatter
 * @see ResponseReceiver
 */
public class CommandFactory
{
    private static final Logger logger = LogManager.getLogger(CommandFactory.class);
    /**
     * Creates a command based on the message and sets up the response
     * handler, sender, formatter and receiver, if needed.
     *
     * @param request
     *         The message received from the user
     * @param sender
     *         The sender to set in the responseHandler
     *
     * @return The command to execute
     */
    public static Command createCommand(Request request, IResponseSender sender, CombinedConfiguration commandConfigs) throws CobraException {
        return createCommand(request, sender, commandConfigs, null);
    }

    /**
     * Creates a command based on the message and sets up the response
     * handler, sender, formatter, receiver, and chainer, if needed.
     *
     * @param request
     *         The message received from the user
     * @param sender
     *         The sender to set in the responseHandler
     * @param chainer
     *         The chainer to set in the responseHandler
     *      
     * @return The command to execute
     */
    public static Command createCommand(Request request, IResponseSender sender, CombinedConfiguration commandConfigs, CommandChainer chainer) throws CobraException
    {
        final Command command;

        try {
            String input = toCamelCase(request.requestCommand.name);
            Class<?> clazz = Class.forName("com.nuance.cobra.command." + input + "Command");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Request.class, CombinedConfiguration.class, CommandChainer.class);
            command = (Command) constructor.newInstance(request, commandConfigs, chainer);
        } catch (Exception e) {
            logger.debug(e);
            throw new CobraException(ResponseCode.createResponseError(ExternalResponseErrorCode.UNKNOWN_COMMAND_ERROR,
                    request.requestCommand.name));
        }

        final ResponseHandler responseHandler = new ResponseHandler(command, command, sender, chainer);
        command.setResponseHandler(responseHandler);

        return command;
    }

    /**
     * Builds up the command name out of the class name.
     * @param c the class to be converted to a command name.
     * @return the command name.
     */
    public static String getCommandName(Class c) {
        String[] names = c.getName().split("\\.");
        String name = toCommandCase(names[names.length-1]);
        return name.substring(0, name.lastIndexOf("Command"));
    }

    /**
     * Capitalize the first letter of the string.
     * @param inputString
     * @return
     */
    public static String toCamelCase(String inputString) {
        return inputString.substring(0,1).toUpperCase()
                + inputString.substring(1);
    }

    /**
     * Lower case the first character of the string.
     * @param inputString
     * @return
     */
    public static String toCommandCase(String inputString) {
        return inputString.substring(0,1).toLowerCase()
                + inputString.substring(1);
    }
}
