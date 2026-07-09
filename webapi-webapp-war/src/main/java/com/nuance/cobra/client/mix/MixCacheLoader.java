package com.nuance.cobra.client.mix;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;

public class MixCacheLoader
        extends CacheLoader<String, MixAccessToken>
{
    private static final Logger logger = LogManager.getLogger(MixCacheLoader.class);
    private ExecutorService executorService;

    public MixCacheLoader(ExecutorService executorService)
    {
        super();
        this.executorService = executorService;
    }

    @Override
    public MixAccessToken load(String mixClientCredentialsString)
            throws Exception
    {
        MixClientCredentials mixClientCredentials = MixClientCredentials.deserialize(mixClientCredentialsString);
        logger.debug("Load called for MixAppId: {}. MixAuthUri: {}", mixClientCredentials.getMixClientAppId(), mixClientCredentials.getMixAuthUri());
        return MixClientCredentialsManager.callAuthService(mixClientCredentials, false);
    }

    @Override
    public ListenableFuture<MixAccessToken> reload(final String mixClientCredentialsString,
            MixAccessToken previousBearerToken)
    {
        MixClientCredentials mixClientCredentials = MixClientCredentials.deserialize(mixClientCredentialsString);
        logger.debug("Reload called for MixAppId: {}. MixAuthUri: {}", mixClientCredentials.getMixClientAppId(), mixClientCredentials.getMixAuthUri());
        ListenableFutureTask<MixAccessToken>
                task = ListenableFutureTask.create(() -> {
            MixAccessToken rc;
            try
            {
                rc = MixClientCredentialsManager.callAuthService(mixClientCredentials, true);
            }
            catch (Exception e)
            {
                logger.error("Using previous bearer token. There was an exception when reloading the cache: {}",
                        e.getMessage(), e);
                throw e;
            }
            return rc;
        });
        executorService.execute(task);
        return task;
    }

}
