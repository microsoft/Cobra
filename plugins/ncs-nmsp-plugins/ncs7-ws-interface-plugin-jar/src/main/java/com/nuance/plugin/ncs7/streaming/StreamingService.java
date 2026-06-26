package com.nuance.plugin.ncs7.streaming;

import com.nuance.plugin.ncs7.Orchestrator;
import com.nuance.plugin.ncs7.translators.AbstractCommandTranslator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class StreamingService {
    private static final Logger logger = LogManager.getLogger(StreamingService.class);

    boolean terminated = false;
    BlockingQueue<Map.Entry<Orchestrator.MessageType, Object>> bufferQueue;

    public StreamingService(BlockingQueue<Map.Entry<Orchestrator.MessageType, Object>> inBufferQueue, IStreamingEndpoint endpoint, String name) {
        this.bufferQueue = inBufferQueue;
        logger.debug("====== Creating streaming service {}", name);
        AbstractCommandTranslator.executor.submit(
                () -> {
                    while (endpoint.isOpen() && !terminated) {
                        try {
                            Map.Entry<Orchestrator.MessageType, Object> entry = bufferQueue.poll(AbstractCommandTranslator.THREAD_MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                            if (entry == null) {
                                //timeout, cleanup and terminate
                                entry = Map.entry(Orchestrator.MessageType.NOTIFY, "");
                            }

                            switch (entry.getKey()) {
                                case DATA:
                                    byte[] data = (byte[])entry.getValue();
                                    endpoint.sendBinary(data);
                                    break;
                                case MESSAGE:
                                    Map<String, Object> audioEvent = (Map<String, Object>) entry.getValue();
                                        switch ((String)audioEvent.get("message")) {
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
                                    terminated  = true;
                                    break;
                                default:
                                    //do nothing
                            }
                        } catch (InterruptedException e) {
                            logger.debug("====== Streaming service interrupted {}", name);
                            Thread.currentThread().interrupt();
                            logger.info("Streaming service interupted.", e);
                            break;
                        }
                    }
                    logger.debug("====== Streaming service Terminated {} terminated: {}, endpoint open: {}", name, terminated, endpoint.isOpen());
                    inBufferQueue.clear();
                }
        );
    }
    public void terminate() {
        terminate(false);
    }

    public void terminate(boolean imediatly) {
        if (!terminated) {
            logger.debug("====== Streaming service terminate");
            if (imediatly) {
                bufferQueue.add(Map.entry(Orchestrator.MessageType.NOTIFY, "Streaming.terminate"));
            } else {
                bufferQueue.add(Map.entry(Orchestrator.MessageType.TERMINATE, "Streaming.terminate")); //wakeup the blocking queue if in waiting state.
            }
            terminated = imediatly;
        } else {
            bufferQueue.add(Map.entry(Orchestrator.MessageType.NOTIFY, "Streaming.terminate"));
        }
    }
}
