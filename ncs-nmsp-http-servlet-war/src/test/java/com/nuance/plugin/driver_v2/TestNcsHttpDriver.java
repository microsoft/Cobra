package com.nuance.plugin.driver_v2;

import com.nuance.plugin.driver_v2.Activities.ASRActivity;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.file.Paths;

public class TestNcsHttpDriver
{
    private static final Logger logger = LogManager.getLogger();

    public static class Defaults {
        public static int txnTimeout = 6000;
        public static String languageCode = "eng-USA";
        //public static String languageTopic = "nma_dm_main";
        public static String languageTopic = "dictation";
        public static String deviceModel = System.getProperty("os.arch") + " - " + System.getProperty("os.name") + " - " + System.getProperty("os.version");

    }

    @Test(expected = Test.None.class)
    public void ncsHttpV2Post() {
        Configurator.setRootLevel(Level.TRACE);
        JSONObject jsonOptions = new JSONObject();

        // Set verbose option
        jsonOptions.put("verbose", false);

        // Disable verifying the server certificate belongs to a trusted root CA
        jsonOptions.put("disable-trusted-root", true);

        // Set the value for device model if provided
        jsonOptions.put("device-model", Defaults.deviceModel);

        jsonOptions.put("mixnlu-model", Configuration.CONTEXT_TAG);
//        jsonOptions.put("nlu-text", cmd.getOptionValue("nlu-text", null));

        jsonOptions.put("tts-text", "Hello World");
        jsonOptions.put("tts-ssml", false);

//        jsonOptions.put("tts-voice", cmd.getOptionValue("tts-voice"));
//        jsonOptions.put("tts-genre", cmd.getOptionValue("tts-genre"));

        jsonOptions.put("language", Defaults.languageCode);
        jsonOptions.put("language-topic", Defaults.languageTopic);

        jsonOptions.put("disable-start-of-speech-detection", false);
        jsonOptions.put("word-by-word-streaming", true);

//        jsonOptions.put("wake-up-phrase", cmd.getOptionValue("wake-up-phrase"));

        jsonOptions.put("text-with-timing-results", false);

        jsonOptions.put("opus-encoded", false);

        jsonOptions.put("adpcm-encoded", false);
        //jsonOptions.put("adpcm-encoded", true);

        jsonOptions.put("high-nibble-first", false);

        jsonOptions.put("batch-mode", true);

        jsonOptions.put("audio-source", Paths.get("samples/asr/one.pcm").toAbsolutePath().toString());
        //jsonOptions.put("audio-source", Paths.get("../webapi-webapp-war/src/test/resources/audio/life.imaadpcm").toAbsolutePath().toString());

        jsonOptions.put("saved-audio-path", ".");

        jsonOptions.put("txn-timeout", String.valueOf(Defaults.txnTimeout));


        jsonOptions.put("request-type", "asr");
        ASRActivity recognizerActivity = new ASRActivity();
        recognizerActivity.start(jsonOptions);
    }
}