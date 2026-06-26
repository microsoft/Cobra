package com.nuance.cobra.utils.response.EDF;

import com.nuance.cobra.utils.audio.WavDumperNode;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AudioLogTest {
    @Test
    public void getFileNameNoExtension() {
        AudioLog audioLog = new AudioLog();
        audioLog.fileName = "testfile.test";

        assertEquals(audioLog.getFileNameNoExtension(), "testfile");
    }

    @Test
    public void getFile() {
        AudioLog audioLog = new AudioLog();
        audioLog.fileName = "testfile.test";

        assertEquals(audioLog.getFile().getName(), "testfile.test");
    }

    @Test
    public void toJson() {
        AudioLog audioLog = new AudioLog();
        audioLog.enabledPosting = true;
        audioLog.encrypted = true;
        audioLog.tenantKey = "testKey";
        audioLog.fileName = "testfile.test";

        try {
            audioLog.toJson(false);
            String json = audioLog.toJson(true);
            assertTrue(json.contains("fileName"));

            AudioLog testLog = AudioLog.fromJson(json);
            assertTrue(testLog.postingEnabled());
            assertTrue(testLog.encrypted);
            assertEquals(testLog.getTenantKey(), "testKey");
            assertEquals(testLog.getFileNameNoExtension(), audioLog.getFileNameNoExtension());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void getJmsType() {
    }

    @Test
    public void getTenantKey() {
    }

    @Test
    public void postingEnabled() {
    }

    @Test
    public void getEdfEndpointUrlProperty() {
    }

    @Test
    public void clone1() {
        AudioLog audioLog = new AudioLog();
        audioLog.enabledPosting = true;
        audioLog.encrypted = true;
        audioLog.tenantKey = "testKey";
        audioLog.fileName = "testfile.test";

        try {
            AudioLog testLog = audioLog.clone();
            assertEquals(testLog.enabledPosting, audioLog.enabledPosting);
            assertEquals(testLog.encrypted, audioLog.encrypted);
            assertEquals(testLog.getFileNameNoExtension(), audioLog.getFileNameNoExtension());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void delete()
            throws IOException
    {
        String filename = "testfile.test";

        AudioLog audioLog = new AudioLog();
        audioLog.fileName = filename;
        Files.write(Paths.get(filename), filename.getBytes());
        WavDumperNode.getEncryptedFiles().put(audioLog.getFileNameNoExtension(),"");
        audioLog.delete();
        assertFalse(Files.exists(Paths.get(filename)));

        File f1 = new File(filename);
        f1.mkdir();
        Files.write(Paths.get(filename + File.separator + filename), filename.getBytes());
        audioLog.delete();
        assertTrue(Files.exists(Paths.get(filename)));
        FileUtils.deleteDirectory(f1);
    }
}