package com.nuance.cobra.utils;


import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatcherService
{
    private static final Logger logger = LogManager.getLogger(DirectoryWatcherService.class);

    private static final String DEFAULT_THREAD_NAME = "DirectoryWatcherService";

    private final String threadName;
    private final Watcher watcher;
    private final Listener listener;

    public enum NotificationType
    {
        CREATE,
        MODIFY,
        DELETE
    }

    public interface Listener
    {
        void notification(String filename, NotificationType type);
    }

    private DirectoryWatcherService(String directory, Listener listener)
            throws IOException
    {
        this(directory, listener, DEFAULT_THREAD_NAME);
    }

    public DirectoryWatcherService(String directory, Listener listener, String threadName)
            throws IOException
    {
        this.threadName = threadName;
        this.listener = listener;

        final Path dir = Paths.get(directory);

        final WatchService watchService;
        watchService = FileSystems.getDefault().newWatchService();
        dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        this.watcher = new Watcher(watchService);
    }

    class Watcher extends ThreadContextRunnable
    {
        private final WatchService watcher;
        private volatile boolean isRunning = false;

        Watcher(WatchService watcher)
        {
            this.watcher = watcher;
        }

        @SuppressWarnings("unchecked")
        WatchEvent<Path> cast(WatchEvent<?> event)
        {
            return (WatchEvent<Path>)event;
        }

        @Override
        public void runWithContext()
        {
            isRunning = true;

            for (;;)
            {
                WatchKey key;

                try
                {
                    key = watcher.take();
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    logger.warn("Thread was interrupted: {}", Thread.currentThread().getName(), e);
                    return;
                }

                for (final WatchEvent<?> event: key.pollEvents())
                {
                    final WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only
                    // for ENTRY_MODIFY events, but
                    // an OVERFLOW event can occur
                    // regardless if events are lost
                    // or discarded.
                    if (OVERFLOW == kind)
                    {
                        continue;
                    }

                    // The filename is the context of the event
                    final WatchEvent<Path> ev = cast(event);
                    final Path path = ev.context();
                    final String filename = path.toString();
                    final NotificationType type;
                    if (ENTRY_CREATE == kind)
                    {
                        type = NotificationType.CREATE;
                    }
                    else if (ENTRY_MODIFY == kind)
                    {
                        type = NotificationType.MODIFY;
                    }
                    else if (ENTRY_DELETE == kind)
                    {
                        type = NotificationType.DELETE;
                    }
                    else
                    {
                        type = null;
                    }

                    listener.notification(filename, type);
                }

                // Reset the key -- this step is critical to receive further watch events.
                // If the key is no longer valid, the directory is inaccessible so exit the loop.
                final boolean valid = key.reset();
                if (! valid)
                {
                    break;
                }

            }

            isRunning = false;
        }
    }

    public void start()
    {
        final Thread t = new Thread(watcher, threadName);
        t.start();
    }

    public boolean isRunning()
    {
        return watcher.isRunning;
    }
}
