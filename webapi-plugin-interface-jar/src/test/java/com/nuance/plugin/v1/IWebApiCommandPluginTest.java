package com.nuance.plugin.v1;

import com.nuance.plugin.v1.shareable.IResponseSender;
import com.nuance.plugin.v1.shareable.IWebApiFeatures;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class IWebApiCommandPluginTest
{

    @Test
    public void webApiCommandPluginTest(){
        IWebApiCommandPlugin testPlugin = new IWebApiCommandPlugin() {
            @Override
            public void onInitPlugin(IWebApiFeatures featuresList) {

            }

            @Override
            public List<String> getProvidedCommands() {
                return List.of();
            }
        };

        assertEquals(0, testPlugin.getProvidedCommands().size());
        assertFalse(testPlugin.onValidRequest(null, null));
        assertFalse(testPlugin.onStartSessionCommand(null, null));
        assertFalse(testPlugin.onEndSessionCommand(null, null));
        assertFalse(testPlugin.onCancelCommandCommand(null, null));
        assertFalse(testPlugin.onSpeechRecognitionCommand(null, null));
        assertFalse(testPlugin.onTextToSpeechCommand(null, null));
        assertFalse(testPlugin.onUploadVocabularyCommand(null, null));
        assertFalse(testPlugin.onInterpretationCommand(null, null));
        assertFalse(testPlugin.onPCMAudio(null));
        assertFalse(testPlugin.onStartOfSpeech());
        assertFalse(testPlugin.onEndOfSpeech());
    }

}