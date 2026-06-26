package com.nuance.cobra.utils.response;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.configs.CobraConfigurationUtils;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnmpResponseTrapSenderTest
{
    private static final String PASSWORD = "U2FsdGVkX1+kgJjdYLiqgD6MtKpgzb/uvp6QNG3Vv6k="; //gitleaks:allow

    private CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
    private Configuration testConf = testConfs.getConfiguration(0);

    @Before
    public void setUp()
            throws Exception
    {
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_HOST_ADDRESS.key, "127.0.0.1");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_HOST_PORT.key, 8162);
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_PROTOCOL.key,"V3");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_USERNAME.key, "xyz");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_PASSWORD.key, PASSWORD);
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_SECURITY_ENCRYPTION_PROTOCOL.key, SnmpResponseTrapSender.TrapSecurityEncryptionAlgorithm.usmAesCfb128Protocol.toString());
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_PRIVACY.key,PASSWORD);
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_CONTEXT_NAME.key, "context");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_CONTEXT_ENGINE_OID.key, "9876543210");//hex
        testConf.setProperty(WebApiConfig.Property.SNMP_LOCAL_ENGINE_OID.key, "1234567890");//hex
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_SEND_RETRIES.key, 1);
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_SEND_TIMEOUT.key, 11500);
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_TRAP_OID.key, "1.3.6.1.4.1.3621.31415");
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAPS_PER_MINUTE.key, -1);
    }

    @Test
    public void testSendTrap()
    {
        SnmpResponseTrapSender trapResp = new SnmpResponseTrapSender(testConfs);

        List<Map<Integer, String>> testTrap = new ArrayList<>();
        HashMap<Integer, String> trapMessage = new HashMap<>();
        trapMessage.put(0, "1302");
        trapMessage.put(1, "2");
        trapMessage.put(2, "test");
        trapMessage.put(3, "testInternalMsg");
        testTrap.add(trapMessage);

        trapMessage = new HashMap<>();
        trapMessage.put(0, "1102");
        trapMessage.put(1, "2");
        trapMessage.put(2, "test1");
        trapMessage.put(3, "testInternalMsg1");
        testTrap.add(trapMessage);

        trapResp.sendTrap(testTrap);
    }

    @Test
    public void testSendTrapThrottleOff()
    {
        SnmpResponseTrapSender trapResp = new SnmpResponseTrapSender(testConfs);

        List<Map<Integer, String>> testTrap = new ArrayList<>();
        HashMap<Integer, String> trapMessage = new HashMap<>();
        trapMessage.put(0, "1302");
        trapMessage.put(1, "2");
        trapMessage.put(2, "test");
        trapMessage.put(3, "testInternalMsg");
        testTrap.add(trapMessage);

        trapMessage = new HashMap<>();
        trapMessage.put(0, "1102");
        trapMessage.put(1, "2");
        trapMessage.put(2, "test1");
        trapMessage.put(3, "testInternalMsg1");
        testTrap.add(trapMessage);

        trapResp.sendTrap(testTrap);
        for (int i = 0; i < 20; ++i) {
            testTrap = new ArrayList<>();

            trapMessage = new HashMap<>();
            trapMessage.put(0, "1302");
            trapMessage.put(1, "2");
            trapMessage.put(2, "test");
            trapMessage.put(3, "testInternalMsg");
            testTrap.add(trapMessage);

            trapMessage = new HashMap<>();
            trapMessage.put(0, "1102");
            trapMessage.put(1, "2");
            trapMessage.put(2, "test1");
            trapMessage.put(3, "testInternalMsg1");
            testTrap.add(trapMessage);
            trapResp.sendTrap(testTrap);
        }
    }

    @Test
    public void testSendTrapInvalidPassword()
    {
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_PRIVACY.key, null);
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_CONTEXT_ENGINE_OID.key, null);
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAPS_PER_MINUTE.key, 10);

        SnmpResponseTrapSender trapResp = new SnmpResponseTrapSender(testConfs);
        List<Map<Integer, String>> testTrap = new ArrayList<>();
        HashMap<Integer, String> trapMessage = new HashMap<>();
        trapMessage.put(0, "test");
        testTrap.add(trapMessage);
        trapResp.sendTrap(testTrap);
    }

    @Test
    public void testSendTrapInvalidAddress()
    {
        CombinedConfiguration testConfs = CobraConfigurationUtils.newCombinedConfigurationWithSingleConfig();
        Configuration testConf = testConfs.getConfiguration(0);
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_HOST_ADDRESS.key, "127.0.0.1");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_HOST_PORT.key, null);

        SnmpResponseTrapSender trapResp = new SnmpResponseTrapSender(testConfs);
        List<Map<Integer, String>> testTrap = new ArrayList<>();
        HashMap<Integer, String> trapMessage = new HashMap<>();
        trapMessage.put(0, "test");
        testTrap.add(trapMessage);
        trapResp.sendTrap(testTrap);
    }

    @Test
    public void testSendTrapV2c()
    {
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_PROTOCOL.key, "V2C");
        testConf.setProperty(WebApiConfig.Property.SNMP_TRAP_V2_COMMUNITY.key, "public");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_USERNAME.key, "xyz");
        testConf.setProperty(WebApiConfig.Property.SNMP_NMS_PASSWORD.key, PASSWORD);

        SnmpResponseTrapSender trapResp = new SnmpResponseTrapSender(testConfs);

        List<Map<Integer, String>> testTrap = new ArrayList<>();
        HashMap<Integer, String> trapMessage = new HashMap<>();
        trapMessage.put(0, "1302");
        trapMessage.put(1, "2");
        trapMessage.put(2, "test");
        trapMessage.put(3, "testInternalMsg");
        testTrap.add(trapMessage);

        trapMessage = new HashMap<>();
        trapMessage.put(0, "1102");
        trapMessage.put(1, "2");
        trapMessage.put(2, "test1");
        trapMessage.put(3, "testInternalMsg1");
        testTrap.add(trapMessage);

        trapResp.sendTrap(testTrap);
    }
}