package com.nuance.cobra.utils.response;

import com.nuance.cobra.session.WebApiConfig;
import com.nuance.cobra.utils.Hostname;
import com.nuance.cobra.utils.encryption.OpenSSLDecryptor;
import com.nuance.cobra.utils.webapi.ThreadContextRunnable;
import com.nuance.plugin.v1.shareable.ISnmpTrapSender;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.snmp4j.*;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.nuance.cobra.utils.response.SnmpResponseTrapSender.TrapVersion.valueOf;

public class SnmpResponseTrapSender implements ISnmpTrapSender
{
    public enum TrapVersion {V2C, V3};

    public enum TrapSecurityEncryptionAlgorithm {
        usmNoPriv(SnmpConstants.usmNoPrivProtocol),
        usmDESProtocol(SnmpConstants.usmDESPrivProtocol),
        usmAesCfb128Protocol(SnmpConstants.usmAesCfb128Protocol),
        usmAesCfb192Protocol(SnmpConstants.oosnmpUsmAesCfb192Protocol),
        usmAesCfb256Protocol(SnmpConstants.oosnmpUsmAesCfb256Protocol);

        private OID oID;

        TrapSecurityEncryptionAlgorithm(OID newOID) {
            this.oID = newOID;
        }

        OID getOid () {
            return oID;
        }
    };

    private static final Logger logger = LogManager.getLogger(SnmpResponseTrapSender.class);

    private static final long THROTTLE_INTERVAL_SECONDS = 60;
    private static final long THROTTLE_INTERVAL_MILLISECONDS = TimeUnit.SECONDS.toMillis(THROTTLE_INTERVAL_SECONDS);

    public static final Integer OID_WEBAPI_CODE = 0;
    public static final Integer OID_WEBAPI_SEVERITY = 1;
    public static final Integer OID_WEBAPI_MESSAGE = 2;
    public static final Integer OID_WEBAPI_INTERNAL_MESSAGE = 3;

    private static final Integer JVM_HOSTNAME_OID = 0; //"jvm.host";
    private static final Integer JVM_PATH_OID = 1; //"jvm.path";
    private static final Integer COMPANY_NAME_OID = 2; //"company.name";
    private static final String COMPANY_NAME = ThreadContextRunnable.COMPANY_NAME;
    private static final Integer APP_NAME_OID = 3; //"app.name";
    private static final String APP_NAME = ThreadContextRunnable.APP_NAME;
    private static final Integer SESSION_ID_OID = 4; //"session.id";
    private static final String SESSION_ID = ThreadContextRunnable.SESSION_ID;

    private RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private Map<Integer, String> systemDescription = new LinkedHashMap<>();
    private String destinationAddress = null;
    private final CombinedConfiguration srvConf;

    private void encodeTrapData(PDU pdu, OID oidKey, Map<Integer, String> trapMessage)
    {
        for (Integer key : trapMessage.keySet())
        {
            OID sysDesc = new OID(oidKey);
            sysDesc.append(key);
            pdu.add(new VariableBinding(sysDesc, new OctetString("" + trapMessage.get(key))));
        }
    }

    private void prepareGlobalInfo()
    {
        String hostname = Hostname.getHostname();

        final String dir = System.getProperty("user.dir");
        /*
        WARNING Keep the insertion order. The order of the elements is mapped to OIDs in the trap.
         */
        systemDescription.put(JVM_HOSTNAME_OID, hostname);
        systemDescription.put(JVM_PATH_OID, dir);
        systemDescription.put(COMPANY_NAME_OID, ThreadContext.get(COMPANY_NAME));
        systemDescription.put(APP_NAME_OID, ThreadContext.get(APP_NAME));
        systemDescription.put(SESSION_ID_OID, ThreadContext.get(SESSION_ID));
    }

    public SnmpResponseTrapSender(CombinedConfiguration configurator)
    {
        srvConf = configurator;
        if (srvConf.getProperty(WebApiConfig.Property.SNMP_NMS_HOST_ADDRESS.key) == null ||
            srvConf.getProperty(WebApiConfig.Property.SNMP_NMS_HOST_PORT.key) == null)
        {
            //snmp is disabled
            destinationAddress = null;
            return;
        }
        destinationAddress = srvConf.getProperty(WebApiConfig.Property.SNMP_NMS_HOST_ADDRESS.key) + "/" +
                             srvConf.getProperty(WebApiConfig.Property.SNMP_NMS_HOST_PORT.key);

        prepareGlobalInfo();
    }

    public void sendTrap(List<Map<Integer, String>> trapMessage)
    {
        if (destinationAddress == null)
        {
            //snmp is disabled
            return;
        }

        switch (valueOf(srvConf.getString(WebApiConfig.Property.SNMP_TRAP_PROTOCOL.key).toUpperCase()))
        {
            case V2C:
                sendV2CTrap(trapMessage);
                break;
            case V3:
            default:
                sendV3Trap(trapMessage);
        }
    }

    private void sendV3Trap(List<Map<Integer, String>> trapMessage)
    {
        try
        {
            Address targetAddress = GenericAddress.parse(destinationAddress);
            TransportMapping<?> transport = new DefaultUdpTransportMapping();

            OctetString nmsLocalEngineID = new OctetString(MPv3.createLocalEngineID());
            OctetString nmsContextEngineID = new OctetString(MPv3.createLocalEngineID());
            OctetString nmsContextName = new OctetString("");

            if (srvConf.getProperty(WebApiConfig.Property.SNMP_LOCAL_ENGINE_OID.key) != null) {
                nmsLocalEngineID = new OctetString(DatatypeConverter.parseHexBinary(srvConf.getString(WebApiConfig.Property.SNMP_LOCAL_ENGINE_OID.key)));
            }

            if (srvConf.getProperty(WebApiConfig.Property.SNMP_NMS_CONTEXT_NAME.key) != null)
            {
                nmsContextName = new OctetString(srvConf.getString(WebApiConfig.Property.SNMP_NMS_CONTEXT_NAME.key));
            }

            if (srvConf.getString(WebApiConfig.Property.SNMP_NMS_CONTEXT_ENGINE_OID.key) != null)
            {
                nmsContextEngineID = new OctetString(DatatypeConverter.parseHexBinary(srvConf.getString(WebApiConfig.Property.SNMP_NMS_CONTEXT_ENGINE_OID.key)));
            }

            USM usm = new USM(SecurityProtocols.getInstance(), nmsLocalEngineID, 0);
            SecurityModels.getInstance()
                    .addSecurityModel(usm);

            transport.listen();

            OctetString securityName = new OctetString(srvConf.getString(WebApiConfig.Property.SNMP_NMS_USERNAME.key));
            OctetString securityPass = null;
            OctetString privacy = null;
            OID privacyID = null;
            OID authProtocol = null;

            int securityLevel = SecurityLevel.NOAUTH_NOPRIV;

            String nmsPassword = srvConf.getString(WebApiConfig.Property.SNMP_NMS_PASSWORD.key);
            if (nmsPassword != null) {
                nmsPassword = OpenSSLDecryptor.decrypt(nmsPassword);
            }
            else {
                // check if cleartext password is used (supported for on prem deployments) if encrypted one not avail.
                nmsPassword = srvConf.getString(WebApiConfig.Property.SNMP_NMS_CLEARTEXT_PASSWORD.key);
            }

            if (nmsPassword != null) {
                securityLevel = SecurityLevel.AUTH_NOPRIV;

                authProtocol = SnmpConstants.usmHMACSHAAuthProtocol;
                securityPass = new OctetString(nmsPassword);

                String nmsPrivacy = srvConf.getString(WebApiConfig.Property.SNMP_NMS_PRIVACY.key);
                if (nmsPrivacy != null) {
                    nmsPrivacy = OpenSSLDecryptor.decrypt(nmsPrivacy);
                }
                else {
                    // check if cleartext privacy passphrase is used (supported for on prem deployments) if encrypted one not avail.
                    nmsPrivacy = srvConf.getString(WebApiConfig.Property.SNMP_NMS_PRIVACY_CLEARTEXT.key);
                }

                if (nmsPrivacy != null) {
                    TrapSecurityEncryptionAlgorithm algorithm =
                            TrapSecurityEncryptionAlgorithm.valueOf(srvConf.getString(WebApiConfig.Property.SNMP_TRAP_SECURITY_ENCRYPTION_PROTOCOL.key));
                    privacyID = algorithm.getOid();
                    privacy = new OctetString(nmsPrivacy);
                    securityLevel = SecurityLevel.AUTH_PRIV;
                }
            }

            Snmp snmp = new Snmp(transport);
            snmp.setLocalEngine(nmsLocalEngineID.getValue(), 0, 0);

            snmp.getUSM()
                    .addUser(securityName, nmsLocalEngineID,
                            new UsmUser(securityName, authProtocol, securityPass, privacyID, privacy));

            // Create Target
            UserTarget target = new UserTarget(targetAddress, securityName, nmsLocalEngineID.getValue());
            target.setRetries(srvConf.getInt(WebApiConfig.Property.SNMP_TRAP_SEND_RETRIES.key));
            target.setTimeout(srvConf.getInt(WebApiConfig.Property.SNMP_TRAP_SEND_TIMEOUT.key));
            target.setVersion(SnmpConstants.version3);
            target.setSecurityLevel(securityLevel);

            // Create PDU for V3
            ScopedPDU pdu = new ScopedPDU();
            pdu.setType(ScopedPDU.NOTIFICATION);
            pdu.setContextEngineID(nmsContextEngineID);
            pdu.setContextName(nmsContextName);

            pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(runtimeBean.getUptime())));
            encodeTrapData(pdu, SnmpConstants.sysDescr, systemDescription);

            //Add Payload
            for (Map<Integer, String> element : trapMessage)
            {
                OID oid = new OID(srvConf.getString(WebApiConfig.Property.SNMP_NMS_TRAP_OID.key));
                encodeTrapData(pdu, oid, element);
                pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
            }

            // Send the PDU
            snmp.send(pdu, target);

            transport.close();
            snmp.close();
        }
        catch (Exception e)
        {
            logger.warn("Error in Sending Trap to (IP:Port)=> " + destinationAddress, e);
        }
    }

    private void sendV2CTrap(List<Map<Integer, String>> trapMessage)
    {
        // Create PDU
        PDU trap = new PDU();
        trap.setType(PDU.TRAP);

        OID trapIOD = new OID(srvConf.getString(WebApiConfig.Property.SNMP_NMS_TRAP_OID.key));
        trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, trapIOD));
        trap.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(runtimeBean.getUptime())));
        encodeTrapData(trap, SnmpConstants.sysDescr, systemDescription);

        //Add Payload
        for (Map element : trapMessage)
        {
            OID oid = new OID(srvConf.getString(WebApiConfig.Property.SNMP_NMS_TRAP_OID.key));
            encodeTrapData(trap, oid, element);
            trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
        }

        // Specify receiver
        Address targetAddress = new UdpAddress(destinationAddress);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(srvConf.getString(WebApiConfig.Property.SNMP_TRAP_V2_COMMUNITY.key)));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(targetAddress);

        // Send
        Snmp snmp = null;
        try {
            snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.send(trap, target, null, null);
        } catch (IOException e) {
            logger.warn("Exception caught at {}", e.getStackTrace());
        }
    }
}
