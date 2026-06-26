package com.nuance.plugin.v1;

import com.nuance.plugin.v1.shareable.IResponseSender;

import java.util.List;

public interface IWebApiCommandPlugin extends IWebApiTenantPlugin {
    /**
     * WebAPI will call this method after initializing the plugin to find out what commands this plugin will implement.
     * The list can be empty or null if this plugin attaches to "onValidRequest" event only
     * @return  A list of all commands this plugin provides.
     */
    List<String> getProvidedCommands();

    /**
     * Called after the JSON string was validated, and authentication was successful.
     * @param parameters    list of request parameters.
     * @param response      object that will handle the response.
     * @return  Return true if the event was consumed.
     */
    default boolean onValidRequest(java.util.Map<String, Object> parameters, IResponseSender response) {
        return false;
    }

    /**
     * Called on a start session command
     * @param parameters    list of request parameters.
     * @param response      object that will handle the response.
     * @return  Return true if the event was consumed.
     */
    default boolean onStartSessionCommand(java.util.Map<String, Object> parameters, IResponseSender response) {
        return false;
    }

    /**
     * Called on an end session command
     * @param parameters    list of request parameters.
     * @param response      object that will handle the response.
     * @return  Return true if the event was consumed.
     */
    default boolean onEndSessionCommand(java.util.Map<String, Object> parameters, IResponseSender response) {
        return false;
    }

    /**
     * Called on a cancel command
     * @param parameters    list of request parameters.
     * @param response      object that will handle the response.
     * @return  Return true if the event was consumed.
     */
    default boolean onCancelCommandCommand(java.util.Map<String, Object> parameters, IResponseSender response) {
        return false;
    }

    /**
     * Called on a speech recognition command
     * @param parameters    list of request parameters.
     * @param response      object that will handle the response.
     * @return  Return true if the event was consumed.
     */
    default boolean onSpeechRecognitionCommand(java.util.Map<String, Object> parameters, IResponseSender response) {
        return false;
    }

    /**
     * Called on a text to speech command
     * @param parameters    list of request parameters.
     * @param response      object that will handle the response.
     * @return  Return true if the event was consumed.
     */
    default boolean onTextToSpeechCommand(java.util.Map<String, Object> parameters, IResponseSender response) {
        return false;
    }

    /**
     * Called on a vocabulary upload command
     * @param parameters    list of request parameters.
     * @param response      object that will handle the response.
     * @return  Return true if the event was consumed.
     */
    default boolean onUploadVocabularyCommand(java.util.Map<String, Object> parameters, IResponseSender response) {
        return false;
    }

    /**
     * Called on an interpretation command
     * @param parameters    list of request parameters.
     * @param response      object that will handle the response.
     * @return  Return true if the event was consumed.
     */
    default boolean onInterpretationCommand(java.util.Map<String, Object> parameters, IResponseSender response) {
        return false;
    }

    /**
     * Called every time there is a new PCM audio data chunk available
     * @param chunk     Audio chunk
     * @return  Return true if the event was consumed.
     */
    default boolean onPCMAudio(byte[] chunk) {
        return false;
    }

    /**
     * Called on the start of speech detection
     * @return  Return true if the event was consumed.
     */
    default boolean onStartOfSpeech() {
        return false;
    }

    /**
     * Called on the end of speech detection
     * @return  Return true if the event was consumed.
     */
    default boolean onEndOfSpeech() {
        return false;
    }
}
