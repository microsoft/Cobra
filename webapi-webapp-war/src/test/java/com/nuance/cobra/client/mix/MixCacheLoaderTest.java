package com.nuance.cobra.client.mix;

import com.google.common.util.concurrent.ListenableFuture;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MixClientCredentialsManager.class)
@PowerMockIgnore({"org.xml.*","jdk.xml.*","java.xml.*","javax.xml.*","com.sun.org.apache.xerces.*","javax.management.*","org.w3c.dom.*"})
public class MixCacheLoaderTest
{
    @Before
    public void setUp() throws Exception
    {
        PowerMockito.mockStatic(MixClientCredentialsManager.class);
    }

    @Test
    public void load()
            throws Exception
    {
        MixCacheLoader mixCacheLoader = new MixCacheLoader(Executors.newSingleThreadExecutor());
        MixAccessToken mat1 = new MixAccessToken(new JSONObject("{\"access_token\":\"abc1234\",\"expires_in\":899,\"scope\":\"asr nlu tts dlg\",\"token_type\":\"bearer\"}"), Instant.now());
        when(MixClientCredentialsManager.callAuthService(any(), anyBoolean())).thenReturn(mat1);

        MixClientCredentials mixClientCredentials = new MixClientCredentials("testTenant", Collections.singletonList("testTenant"), "asr nlu tts dlg", "grant_type=client_credentials&scope=", "https://auth.example.com/oauth2/token");
        MixAccessToken mat2 = mixCacheLoader.load(mixClientCredentials.serialize());
        assertEquals(mat1, mat2);
    }

    @Test
    public void reload()
            throws Exception
    {
        MixCacheLoader mixCacheLoader = new MixCacheLoader(Executors.newSingleThreadExecutor());
        MixAccessToken mat1 = new MixAccessToken(new JSONObject("{\"access_token\":\"abc1234\",\"expires_in\":899,\"scope\":\"asr nlu tts dlg\",\"token_type\":\"bearer\"}"), Instant.now());
        MixAccessToken matPrev = new MixAccessToken(new JSONObject("{\"access_token\":\"xyz987\",\"expires_in\":899,\"scope\":\"asr nlu tts dlg\",\"token_type\":\"bearer\"}"), Instant.now());
        when(MixClientCredentialsManager.callAuthService(any(), anyBoolean())).thenReturn(mat1);

        MixClientCredentials mixClientCredentials = new MixClientCredentials("testTenant", Collections.singletonList("testTenant"), "asr nlu tts dlg", "grant_type=client_credentials&scope=", "https://auth.example.com/oauth2/token");
        ListenableFuture<MixAccessToken> future = mixCacheLoader.reload(mixClientCredentials.serialize(), matPrev);
        MixAccessToken mat2 = future.get();
        assertEquals(mat1, mat2);
    }
}