package com.nuance.cobra.utils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class DirectoryWatcherServiceTest {

    @Test
    public void testDirectoryWatcher() throws IOException, InterruptedException {
        DirectoryWatcherServiceTest.Listener listener = new DirectoryWatcherServiceTest.Listener();
        DirectoryWatcherService service = new DirectoryWatcherService("./target/", listener, "Test");
        service.start();

        Thread.sleep(10); //give time to the JVM to start a new thread.

        File file = File.createTempFile("temp", null, new File("./target"));
        //System.err.println("Writing file!");
        file.deleteOnExit();

        listener.getLatch().await(30, TimeUnit.SECONDS);

        if(listener.getLatch().getCount() != 0)
        {
            fail();
        }
    }

    public class Listener implements DirectoryWatcherService.Listener
    {
        private CountDownLatch latch = new CountDownLatch(1);
        public void notification(String filename, DirectoryWatcherService.NotificationType type)
        {
            //System.err.println("Got notification!");
            latch.countDown();
        }

        public CountDownLatch getLatch()
        {
            return latch;
        }
    }
}