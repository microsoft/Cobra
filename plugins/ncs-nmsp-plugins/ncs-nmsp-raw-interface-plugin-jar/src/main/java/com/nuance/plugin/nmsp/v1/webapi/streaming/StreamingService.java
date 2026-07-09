package com.nuance.plugin.nmsp.v1.webapi.streaming;

import com.nuance.plugin.nmsp.v1.webapi.orchestrator.WebAPIOrchestrator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class StreamingService implements Runnable {
    private static final Logger logger = LogManager.getLogger();
    volatile boolean terminated = false;
    private final IStreamingEndpoint endpoint;
    private final String name;
    BlockingQueue<AudioPacket> bufferQueue;
    Thread runner = null;

    public static class StreamingServiceBuilder {

        public static StreamingService build(BlockingQueue<StreamingService.AudioPacket> inBufferQueue, IStreamingEndpoint endpoint, String name) {
            StreamingService service = new StreamingService(inBufferQueue, endpoint, name);

            WebAPIOrchestrator.executor.submit(service);

            return service;
        }
    }

    private StreamingService(BlockingQueue<AudioPacket> inBufferQueue, IStreamingEndpoint endpoint, String name) {
        this.bufferQueue = inBufferQueue;
        this.name = name;
        this.endpoint = endpoint;
        logger.debug("==== Creating streaming service: {}. Buffer size: {}", name, inBufferQueue.size());
    }

    public void terminate() {
        terminate(false);
    }

    public void terminate(boolean immediately) {
        if (!terminated) {
            if (immediately) {
                bufferQueue.add(new AudioPacket(AudioPacket.MessageType.NOTIFY, "Streaming.terminate".getBytes()));
            } else {
                bufferQueue.add(new AudioPacket(AudioPacket.MessageType.TERMINATE, "Streaming.terminate".getBytes())); //wakeup the blocking queue if in waiting state.
            }
            terminated = true;

            logger.debug("====== Streaming {} set to terminate. Queue size {}", name, bufferQueue.size());
        } else {
            bufferQueue.add(new AudioPacket(AudioPacket.MessageType.NOTIFY, "Streaming.terminate".getBytes()));
        }

        if (runner != null) {
            try {
                runner.join(100);
            } catch (InterruptedException e) {
                logger.debug("====== Streaming service did not terminate", e);
                Thread.currentThread().interrupt();
            }
        }

    }

    @Override
    public void run() {
        runner = Thread.currentThread();
        try {
            while (endpoint.isOpen() && !terminated) {
                AudioPacket entry = bufferQueue.poll(10, TimeUnit.SECONDS);
                if (entry == null) {
                    //timeout, cleanup and terminate
                    entry = new AudioPacket(AudioPacket.MessageType.NOTIFY, "".getBytes());
                }

                switch (entry.getType()) {
                    case DATA:
                        endpoint.sendBinary(entry.getData());
                        break;
                    case MESSAGE:
                        String message = new String(entry.getData());
                        switch (message) {
                            case "audio_end":
                                terminated = true;
                                endpoint.onStreamingDone();
                                break;
                            default:
                                //do nothing
                                break;
                        }
                        break;
                    case TERMINATE:
                        terminated = true;
                        break;
                    default:
                        //do nothing
                }
            }
        } catch (InterruptedException e) {
            logger.debug("==== Streaming service interrupted {}", name);
            terminated = true;
            Thread.currentThread().interrupt();
            logger.info("Streaming service interupted.", e);
        } finally {
            logger.debug("==== Streaming service Terminated {} terminated: {}, endpoint open: {}", name, terminated, endpoint.isOpen());
            bufferQueue.clear();
        }
    }

    public static class AudioPacket {
        private MessageType type;
        private byte[] data;

        public AudioPacket(MessageType type, byte[] data) {
            this.setType(type);
            this.setData(data);
        }

        public MessageType getType() {
            return type;
        }

        public void setType(MessageType type) {
            this.type = type;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public enum MessageType {
            DATA, MESSAGE, TERMINATE, NOTIFY
        }
    }
}
