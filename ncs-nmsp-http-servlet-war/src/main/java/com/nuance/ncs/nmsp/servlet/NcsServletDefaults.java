package com.nuance.ncs.nmsp.servlet;

import com.nuance.ncs.nmsp.util.EnvUtil;

public class NcsServletDefaults
{
	NcsServletDefaults() { throw new IllegalStateException("Utility class!"); }

	public static final String NMSPGW_HOST = EnvUtil.getOrDefault("NMSPGW_HOST", "127.0.0.1");
	public static final short NMSPGW_PORT = Short.parseShort(EnvUtil.getOrDefault("NMSPGW_PORT", "8911"));

	public static final int COMMAND_TIMEOUT = Integer.parseInt(EnvUtil.useDefault("COMMAND_TIMEOUT", "60000"));
	public static final String AUTHORIZED_NMAIDS = EnvUtil.useDefault("WHITELIST", ".*");
	public static final String AUTHORIZED_IPS = EnvUtil.useDefault("AUTHORIZED_IPS", ".*");
	public static final String AUTHORIZED_CROSS_ORIGINS = EnvUtil.useDefault("AUTHORIZED_CROSS_ORIGIN", "$^");
	public static final boolean	CLIENT_CALL_LOG_ENABLED = Boolean.parseBoolean(EnvUtil.useDefault("CLIENT_CALL_LOG_ENABLED", "TRUE"));
	public static final String NMSP_SSL_SOCKET_ENABLE = EnvUtil.getOrDefault("NMSPGW_SSL_SOCKET_ENABLE", "TRUE");
	public static final String NMSP_SSL_SELF_SIGNED_CERT = EnvUtil.getOrDefault("NMSPGW_SSL_SELF_SIGNED_CERT", "FALSE");

	public static final int FLUSH_TIME_MS = Integer.parseInt(EnvUtil.getOrDefault("FLUSH_TIME_MS", "10"));

	public static boolean getClientCallLogEnabled()
	{
		return CLIENT_CALL_LOG_ENABLED;
	}

	public static String getAuthorizedCrossOrigins()
	{
		return AUTHORIZED_CROSS_ORIGINS;
	}

	public static String getAuthorizedIps()
	{
		return AUTHORIZED_IPS;
	}
}
