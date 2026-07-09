package com.nuance.cobra.session;

public class TenantConfigManagerTest
{
//    private final TenantConfigManager tenantConfigManager = TenantConfigManager.getInstance();
//    private final Map<String, CobraConfiguration> tenantConfigMap = tenantConfigManager.getConfigMap();
//    private TenantConfigManager.Factory mockFactory = mock(TenantConfigManager.Factory.class);
//    private TenantConfigManager.TenantConfigRetriever mockRetriever = mock(TenantConfigManager.TenantConfigRetriever.class);
//    private final CobraConfiguration mockTenantConfig = new CobraConfiguration();
//
//    @Before
//    public void setUp() throws Exception
//    {
//        tenantConfigMap.clear();
//    }
//
//    @After
//    public void tearDown() throws Exception
//    {
//        tenantConfigMap.clear();
//    }
//
//    @Test
//    public void test_getTenantConfig_isPresent() throws Exception
//    {
//        tenantConfigMap.put("testKey", mockTenantConfig);
//
//        final CobraConfiguration tenantConfig = tenantConfigManager.getTenantConfig("testKey");
//        assertTrue(tenantConfig == mockTenantConfig);
//    }

    /*
    @Test
    public void test_getTenantConfig_isPresentAfterSync() throws Exception
    {
        final CountDownLatch latch = new CountDownLatch(2);

        final Answer<TenantConfig> returner = (invocation) ->
        {
            latch.countDown();
            return mockTenantConfig;
        };
        when(mockFactory.createTenantRetriever()).thenReturn(mockRetriever);
        when(mockRetriever.retrieveTenantConfig(anyString())).thenAnswer(returner);

        final Runnable r1 = () ->
        {
            synchronized (tenantConfigMap)
            {
                final TenantConfig tenantConfig = tenantConfigManager.getTenantConfig("testKey");
            }
        };

        final Runnable r2 = () ->
        {
            synchronized (tenantConfigMap)
            {
                final TenantConfig tenantConfig = tenantConfigManager.getTenantConfig("testKey");
            }
        };

        new Thread(r1).start();
        new Thread(r2).start();

        assertTrue(tenantConfig == mockTenantConfig);
    }
    */


}
