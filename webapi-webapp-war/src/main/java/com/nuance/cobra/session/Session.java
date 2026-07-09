package com.nuance.cobra.session;


import com.nuance.cobra.command.Command;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import com.nuance.cobra.utils.response.ResponseHandler;
import com.nuance.plugin.v1.shareable.ISession;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This class holds information relative to a user's session
 */
public class Session implements ISession
{
    private static final Logger logger = LogManager.getLogger(Session.class);

    // UUID to identify this runnning instance of webapi (shared across sessions, will be used for the audioBlobPath.)
    private static final String WEBAPI_INSTANCE_UUID = UUID.randomUUID().toString();


    public enum SessionPropertyKeys
    {
        DYNAMICVOCABULARIES,
        APICONNECTIONCLOSER,
        SESSIONLESS_KEY,
        SESSIONLESS_NIM_STARTED
    }

    /**
     * The active command. Used for directive and audio streaming
     */
    private Command command;

    /**
     * The session identifier (Server-generated)
     */
    private final String sessionId;

    /**
     * The stepId of the session. Increments on every command executed
     * during the session.
     */

    private int stepId = 0;

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public void incrementStepId(){
        incrementStepId(1);
    }

    public void incrementStepId(int step){
        stepId += step;
    }

    /**
     * The time when the session was last used
     */
    private Calendar lastActivityTime;
    private String lastActivityTimeString; //For debugging with readable times

    /**
     * The time when the session started
     */
    private Calendar startSessionTime;
    private String startSessionTimeString; //For debugging with readable times

    private Calendar endSessionTime = null;

    private final CombinedConfiguration config;

    /**
     * Properties to store any object as part of the session
     */
    private final Map<SessionPropertyKeys, Object> properties =
            new EnumMap<>(SessionPropertyKeys.class);


    /**
     * Creates a session and stores the time it was created at for expiration
     *
     * @param sessionId
     *         The id associated with the session
     * @param config
     *         The session configuration
     */
    public Session(String sessionId, CombinedConfiguration config)
    {
        this(sessionId, config, Calendar.getInstance());
    }

    /**
     * factored this out (with package visibility) so tests can invoke this constructor to control (= fake :-)
     * the session start time.
     */
    Session(String sessionId, CombinedConfiguration config, Calendar startTime)
    {
        lastActivityTime = startTime;
        lastActivityTimeString = lastActivityTime.getTime().toString();
        startSessionTime = startTime;
        startSessionTimeString = startSessionTime.getTime().toString();
        this.sessionId = sessionId;
        this.config = config;
        setProperty(SessionPropertyKeys.SESSIONLESS_KEY,Boolean.FALSE);
        setProperty(SessionPropertyKeys.SESSIONLESS_NIM_STARTED,Boolean.FALSE);
    }

    public String getSessionId() {
        return sessionId;
    }

    /**
     * Allows to retrieve a property based on a key (String)
     *
     * @param key
     *         The key used to retrieve the object from the properties map.
     *
     * @return Object
     * The object
     */
    public Object getProperty(SessionPropertyKeys key)
    {
        return properties.get(key);
    }

    /**
     * Allows to set a property (an object) based on a key (String)
     */
    public void setProperty(SessionPropertyKeys key, Object object)
    {
        properties.put(key, object);
    }

    public CombinedConfiguration getSessionConfig()
    {
        return config;
    }

    public CombinedConfiguration getConfigForRequest()
    {
        return CobraConfigurationUtils.addCommandParamsToConfiguration(config, getCommand().request.requestCommand.parameters);
    }

    public CombinedConfiguration getConfigWithMerge(Map<String, Object> parameters)
    {
        return CobraConfigurationUtils.addCommandParamsToConfiguration(config, parameters);
    }


    // To support tests
    Map<SessionPropertyKeys, Object> getPropertyMap()
    {
        return properties;
    }

    /**
     * Gets the last activity time
     */
    private Calendar getLastActivityTime()
    {
        return lastActivityTime;
    }

    public Calendar getStartSessionTime() { return startSessionTime;}

    /**
     * Sets the last activity time
     *
     * @param time
     *         The new last activity time
     */
    void setLastActivityTime(Calendar time)
    {
        lastActivityTime = time;
        lastActivityTimeString = lastActivityTime.getTime().toString();
    }

    /**
     * Updates the last activity time to now
     */
    void updateLastActivityTime()
    {
        setLastActivityTime(Calendar.getInstance());
    }

    /**
     * Checks if the session has expired
     *
     * @return True if it has expired
     */
    boolean isExpired()
    {
        Calendar now = Calendar.getInstance();
        //Check session timeout first
        int sessionTimeoutSeconds = getSessionConfig().getInt(WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.key, (int) WebApiConfig.Property.SESSION_TIMEOUT_SECONDS.defaultValue);

        Calendar startSessionTimePlusTimeout = (Calendar) getStartSessionTime().clone();
        startSessionTimePlusTimeout.add(Calendar.SECOND, sessionTimeoutSeconds);
        if( now.after(startSessionTimePlusTimeout))
        {
            logger.info("Session expired due to session timeout, sessionId - {}", sessionId);
            return true;
        }

        //If not timed out, check if active command, if yes, reset lastactivitytime
        if(command != null)
        {
            updateLastActivityTime();
            return false;
        }

        //if no command active check if inactivity expired.

        int sessionInactivityTimeoutSeconds = getSessionConfig().getInt(WebApiConfig.Property.SESSION_INACTIVITY_TIMEOUT_SECONDS.key);
        Calendar lastActivityPlusTimeout = (Calendar) getLastActivityTime().clone();
        lastActivityPlusTimeout.add(Calendar.SECOND, sessionInactivityTimeoutSeconds);
        if(now.after(lastActivityPlusTimeout))
        {
            logger.info("Session expired due to session inactivity timeout, sessionId - {}", sessionId);
            return true;
        }
        return false;
    }
    
    public void setCommand(Command command)
    {
        if (command != null)
        {
            incrementStepId();
        }
        else
        {
            config.removeConfiguration(WebApiConfigManager.CONFIG_NAME_COMMAND_PARAMS);
        }

        this.command = command;
    }

    public Command getCommand()
    {
        return command;
    }

    public String getCommandName()
    {
        if(command != null)
        {
            return command.getCommandName();
        }
        return "";
    }

    public String getCommandId()
    {
        if(command != null)
        {
            return command.getCommandId();
        }
        return "";
    }

    public ResponseHandler getResponseHandler()
    {
        if(command != null)
        {
            return command.getResponseHandler();
        }
        return null;
    }

    public static boolean isSessionUsingStubBackend(String sessionId)
    {
        final SessionManager sessionManager = SessionManager.getInstance();
        Session session = sessionManager.getSession(sessionId, false);

        return session != null &&
                session.getSessionConfig() != null &&
                session.getSessionConfig()
                      .getBoolean(WebApiConfig.Property.STUB_BACKEND.key);

    }

    public static String getTenantKey(Session session)
    {
        String tenantKey;

        try
        {
            tenantKey = session.getSessionConfig().getString(WebApiConfig.Property.APPLICATION_KEY.key);
        } catch (Exception e)
        {
            tenantKey = null;
        }

        logger.debug("Session::getTenantKey: Tenant Key Returned -- {}", tenantKey);
        return tenantKey;
    }

    public String getTenantKey()
    {
        return getTenantKey(this);
    }

    /**
     * Method that returns the list of active vocabularies for the actual command. This combines the always active
     * application vocabularies (from tenant config file) and the activated (using activeDynamicVocabularySets list of
     * string from the speechRecognition command) vocabularies from the uploadDynamicVocabularies command.
     *
     * @return
     *  The list of active Dynamic Vocabularies
     */
    @SuppressWarnings("unchecked")
    public List<DynamicVocabulary> retrieveActiveDynamicVocabularies(CombinedConfiguration requestConfig)
    {
        logger.trace("retrieveActiveDynamicVocabularies - sessionId: {}", sessionId);
        ArrayList<DynamicVocabulary> requestDynamicVocabularies = null;
        //Retrieving the list of always active dynamic vocabularies from the config (these are set in the tenant config file)
        List<DynamicVocabulary> vocabularyList = requestConfig.getList(DynamicVocabulary.class, WebApiConfig.Property.APPLICATION_VOCABULARIES.key);
        ArrayList<DynamicVocabulary> applicationVocabularies = null;
        if (vocabularyList!=null
                && !vocabularyList.isEmpty())
        {
            applicationVocabularies = new ArrayList<>(vocabularyList);
        }

        //Retrieving the list of Ids from the speechRecognition request
        List<String> activeVocabulary = requestConfig.getList(String.class, WebApiConfig.Property.ACTIVE_DYNAMIC_VOCABULARY.key);

        if (activeVocabulary == null || activeVocabulary.isEmpty()) {
            logger.debug("No active dynamic vocabularies, looking for application vocabularies");
            if (applicationVocabularies != null && !applicationVocabularies.isEmpty())
            {
                requestDynamicVocabularies = new ArrayList<>();
                //Then need to add the list from tenant config, this list is always active
                requestDynamicVocabularies.addAll(applicationVocabularies);
            }
            return requestDynamicVocabularies;
        }


        logger.debug("List of vocabulary ids to activate: {}", () -> String.join(",", activeVocabulary));

        //Retrieving the list of DynamicVocabularies loaded using the uploadDynamicVocabulary command
        HashMap<String, DynamicVocabulary> sessionDynamicVocabularies = (HashMap<String, DynamicVocabulary>)SessionManager.getInstance().getSession(sessionId, false).getProperty(
                Session.SessionPropertyKeys.DYNAMICVOCABULARIES);


        if(!hasDynamicVocabularies(sessionDynamicVocabularies, applicationVocabularies))
        {
            return null;
        }

        //The dynamicVocabularies to return. Empty list if no match.
        requestDynamicVocabularies = new ArrayList<>();
        for(String vocabularyId : activeVocabulary)
        {
            if(sessionDynamicVocabularies != null && sessionDynamicVocabularies.containsKey(vocabularyId))
            {
                requestDynamicVocabularies.add(sessionDynamicVocabularies.get(vocabularyId));
            }
            else
            {
                logger.warn("Couldn't find DynamicVocabulary associated with Id: {}", vocabularyId);
            }
        }

        if (applicationVocabularies != null && !applicationVocabularies.isEmpty())
        {
            //Then need to add the list from tenant config, this list is always active
            requestDynamicVocabularies.addAll(applicationVocabularies);
        }

        return requestDynamicVocabularies;
    }

    private boolean hasDynamicVocabularies(HashMap<String, DynamicVocabulary> sessionDynamicVocabularies, ArrayList<DynamicVocabulary> applicationVocabularies) {
        return sessionDynamicVocabularies != null || (applicationVocabularies != null && !applicationVocabularies.isEmpty());
    }

    public String getLastActivityTimeString()
    {
        return lastActivityTimeString;
    }

    public String getStartSessionTimeString()
    {
        return startSessionTimeString;
    }


    /**
     * Return a unique key to retrieve the audio in the azure blobs, if multipleAudioLog per blob is enabled, null otherwise.
     */
    public String getAudioLogKey() {
        boolean multipleAudioPerBlob = getSessionConfig().getBoolean(WebApiConfig.Property.FLUENT_MULTIPLE_AUDIO_LOGS_PER_BLOB.key);
        if (multipleAudioPerBlob) {
            return sessionId + "-" + stepId;
        } else {
            return null;
        }
    }


    /**
     * Returns the value of the configured time slice duration, rounded down to the closest match in allowed values
     * If 0 if configured, the lowest allowed value (1) will be returned.
     *
     * @return configured timeslice duration or rounded down to closest allowed value
     */
    private int getAudioLogsTimesliceDuration() {
        final int[] VALID_VALUES = { 1, 5, 10, 15, 20, 30, 60 };
        int configuredTimesliceDuration = getSessionConfig().getInt(WebApiConfig.Property.FLUENT_AUDIO_LOGS_TIMESLICE_DURATION.key,
                (Integer) WebApiConfig.Property.FLUENT_AUDIO_LOGS_TIMESLICE_DURATION.defaultValue);

        int closestMatch = -1;
        for (int value : VALID_VALUES) {
            closestMatch = value;
            if (value >= configuredTimesliceDuration) {
                break;
            }
        }

        return closestMatch;
    }


    /**
     * Builds the path for storage of audio blobs. By default it is <dbtopic>/<tenant>/audio/yyyy/MM/dd/HH/<sessionid>-<stepid>.json
     * If the option to combine multiple audio logs per blob is enabled (fluentMultipleAudioLogsPerBlob),
     * the path is <dbtopic>/<tenant>/audio/yyyy/MM/dd/HH/<instance id>-<timeslice>.json where instance id is a uuid generated at startup for this
     * running instance of webapi, and timeslice is the timeslice in minutes for this blob.
     * The number of timeslices per hour depends on the fluentAudioLogsTimesliceMinutes parameter. For example, if set to 10, the timeslices will be
     * 00, 10, 20, 30, 40, 50.
     */
    public String makeAudioBlobPath() {

        String topicName = getSessionConfig().getString(WebApiConfig.Property.FLUENT_LOG_TOPIC_NAME.key);
        if(topicName == null)
        {
            String channel = getSessionConfig().getString(WebApiConfig.Property.FLUENT_LOG_CHANNEL.key);
            String clientId = getSessionConfig().getString(WebApiConfig.Property.EDF_CLIENT_ID.key);
            topicName = channel + "-" + clientId;
        }

        String tenant = getSessionConfig().getString(WebApiConfig.Property.APPLICATION_KEY.key);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH");
        OffsetDateTime utcStartTime = OffsetDateTime.ofInstant(startSessionTime.toInstant(), ZoneOffset.UTC);
        String timestampPathComponent = dtf.format(utcStartTime);

        StringBuilder blobPath = new StringBuilder();
        blobPath.append(topicName);
        blobPath.append("/");
        blobPath.append(tenant);
        blobPath.append("/audio/");
        blobPath.append(timestampPathComponent);
        blobPath.append("/");

        boolean multipleAudioPerBlob = getSessionConfig().getBoolean(WebApiConfig.Property.FLUENT_MULTIPLE_AUDIO_LOGS_PER_BLOB.key);

        if (multipleAudioPerBlob) {
            // each multiple audio logs will be stored in the same blob, grouped by webapi instance (unique id generated
            // for this purpose) and timeslice (number of minutes, configurable, default 5)
            blobPath.append(WEBAPI_INSTANCE_UUID);
            int audioSliceDuration = getAudioLogsTimesliceDuration();

            String  timesliceStr = String.format("%02d", utcStartTime.getMinute() / audioSliceDuration * audioSliceDuration);
            blobPath.append("-");
            blobPath.append(timesliceStr);
        }
        else {
            // each audio blob identified by sessionid-stepid in its own blob
            blobPath.append(sessionId);
            blobPath.append("-");
            blobPath.append(stepId);
        }
        return blobPath.toString();
    }

    public void cancelActiveCommand(String origin)
    {
        logger.trace("cancelActiveCommand");

        Command commandToCancel = getCommand();
        if (getCommand() == null)
        {
            return;
        }

        logger.info("Cancelling commandName = {}. commandId = {}", commandToCancel.getCommandName(), commandToCancel.getCommandId());

        commandToCancel.cancel(origin);
    }

    public void setEndSessionTime(Calendar endTime)
    {
        if(endSessionTime != null) return;

        endSessionTime = endTime;
    }

    public Calendar getEndSessionTime()
    {
        return endSessionTime;
    }

    public boolean isSessionEnded()
    {
        return getEndSessionTime() != null;
    }

}
