package com.nuance.cobra.utils.http;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.*;

public class WebSocketHttpSession implements Session {
    private final HttpSession httpSession;
    private final Map<String, Object> userProperties = new HashMap<>();
    private final Map<String, String> pathProperties = new HashMap<>();

    private long maxIdleTimeout = 1000;
    private int maxBinaryMessageBufferSize = 1024 * 4 * 1000;
    private int maxTextMessageBufferSize = 1024 * 4 * 1000;
    private boolean isOpen = true;

    public WebSocketHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;

        Enumeration<String> elem = httpSession.getAttributeNames();
        while (elem != null && elem.hasMoreElements()) {
            String key = elem.nextElement();
            pathProperties.put(key, httpSession.getAttribute(key).toString());
        }
    }

    @Override
    public WebSocketContainer getContainer() {
        return null;
    }

    @Override
    public void addMessageHandler(MessageHandler messageHandler) throws IllegalStateException {
        //not needed for webapi
    }

    @Override
    public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Whole<T> whole) {
        //not needed for webapi
    }

    @Override
    public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Partial<T> partial) {
        //not needed for webapi
    }

    @Override
    public Set<MessageHandler> getMessageHandlers() {
        return new HashSet<>();
    }

    @Override
    public void removeMessageHandler(MessageHandler messageHandler) {
        //not needed for webapi
    }

    @Override
    public String getProtocolVersion() {
        return null;
    }

    @Override
    public String getNegotiatedSubprotocol() {
        return null;
    }

    @Override
    public List<Extension> getNegotiatedExtensions() {
        return new ArrayList<>();
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public long getMaxIdleTimeout() {
        return maxIdleTimeout;
    }

    @Override
    public void setMaxIdleTimeout(long l) {
        maxIdleTimeout = l;
    }

    @Override
    public void setMaxBinaryMessageBufferSize(int i) {
        maxBinaryMessageBufferSize = i;
    }

    @Override
    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    @Override
    public void setMaxTextMessageBufferSize(int i) {
        maxTextMessageBufferSize = i;
    }

    @Override
    public int getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        return null;
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        return null;
    }

    @Override
    public String getId() {
        return httpSession.getId();
    }

    @Override
    public void close() throws IOException {
        //Veracode complains if we never invalidate the httpSession
        httpSession.invalidate();
        isOpen = false;
    }

    @Override
    public void close(CloseReason closeReason) throws IOException {
        //Veracode complains if we never invalidate the httpSession
        close();
    }

    @Override
    public URI getRequestURI() {
        return null;
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        return new HashMap<>();
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public Map<String, String> getPathParameters() {
        return pathProperties;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public Set<Session> getOpenSessions() {
        return new HashSet<>();
    }
}
