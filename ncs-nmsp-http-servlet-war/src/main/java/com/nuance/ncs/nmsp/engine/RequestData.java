package com.nuance.ncs.nmsp.engine;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.pdx.NmspParameter;
import com.nuance.ncs.nmsp.pdx.NmspParamType;
import com.nuance.ncs.nmsp.servlet.NcsServletDefaults;
import com.nuance.ncs.nmsp.servlet.NcsServletDefines;
import com.nuance.ncs.nmsp.servlet.utils.HexToArray;
import com.nuance.ncs.nmsp.servlet.utils.JsonPdxConverter;
import com.nuance.plugin.ncs.utils.ICodecResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("java:S1149")
public class RequestData
{
	private static final Logger log = LogManager.getLogger(RequestData.class);

	// Manager Data definitions
	private static final String JSON_KEY_APP_KEY = "appKey";
	private static final String JSON_KEY_APP_ID = "appId";
	private static final String JSON_KEY_UID = "uId";
	private static final String JSON_KEY_IN_CODEC = "inCodec";
	private static final String JSON_KEY_OUT_CODEC = "outCodec";
	private static final String JSON_KEY_MANAGER_PARAMS = "mgerPrms";

	// NMAS Data
	private static final String JSON_KEY_APP_NAME = "appName";

	// Command Data definitions
	private static final String JSON_KEY_CMD_NAME = "cmdName";
	private static final String JSON_KEY_APP_VERSION = "appVersion";
	private static final String JSON_KEY_LANGUAGE = "language";
	private static final String JSON_KEY_CARRIER = "carrier";
	private static final String JSON_KEY_DEVICE_MODEL = "deviceModel";
	private static final String JSON_KEY_CMD_TIMEOUT = "cmdTimeout";
	private static final String JSON_KEY_CMD_DICT = "cmdDict";

	// CORS Data
	private String corsOrigin;

	// Manager Data
	private String serverIP;
	private short serverPort;
	private byte[] appKey;
	private String appID;
	private String userId;
	private ICodecResource.Codec inCodec;
	private ICodecResource.Codec outCodec;
	private List<NmspParameter> parameters;

	private String outCodecMimeType;

	//NMAS Data
	private String appName;

	// Command data
	private String commandName;
	private String applicationVersion;
	private String dictationLanguage;
	private String carrier;
	private String phoneModel;
	private NmspDictionary optionalKeys;
	private long commandTimeout;

	private boolean success = true;

	@SuppressWarnings("java:S3776")
	public RequestData(String jsonData, String headerAppId, String headerAppKey, String headerUId, String headerOrigin)
	{
		corsOrigin = headerOrigin;

		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) new JSONTokener(jsonData).nextValue();
		}
		catch (Exception e) {
			log.error("RequestData: JSON parsing error. The JSON data is invalid.");
			success = false;
			return;
		}

		serverIP = NcsServletDefaults.NMSPGW_HOST;
		serverPort = NcsServletDefaults.NMSPGW_PORT;

		try {
			commandTimeout = jsonObject.getLong(JSON_KEY_CMD_TIMEOUT);
		}
		catch (JSONException e) {
			commandTimeout = NcsServletDefaults.COMMAND_TIMEOUT;
			log.info("RequestData: Explicit timeout not specified. Using default value.");
		}

		parameters = getDefaultManagerParameters();
		try {
			JSONArray mgerParmArray = jsonObject.getJSONArray(JSON_KEY_MANAGER_PARAMS);

			if(mgerParmArray != null) {
				for ( int i = 0 ; i < mgerParmArray.length() ; i++ ) {
					JSONObject paramObject = mgerParmArray.getJSONObject(i);
					String name = paramObject.getString("name");
					byte[] value = paramObject.getString("value").getBytes(StandardCharsets.UTF_8);
					NmspParamType type = getParmType(paramObject.getString("type"));
					parameters.add(new NmspParameter(name, value, type));
				}
			}
		} catch (Exception e) {
			log.info("RequestData: Error parsing manager parameters. They will be ignored");
		}

		try {
			optionalKeys = JsonPdxConverter.jsonObjectToPdxDictionary(jsonObject.getJSONObject(JSON_KEY_CMD_DICT));
		}
		catch (JSONException e) {
			optionalKeys = new NmspDictionaryImpl(); //empty dictionary
			log.info("RequestData: Optional keys not specified. Sending empty dictionary.");
		}

		// mandatory parameters
		try {
			if(headerAppKey != null) {
				appKey = HexToArray.hexStringToByteArray(headerAppKey);
			}
			else{
				appKey = HexToArray.hexStringToByteArray(jsonObject.getString(JSON_KEY_APP_KEY));
			}

			if(headerAppId != null) {
				appID = headerAppId;
			}
			else {
				appID = jsonObject.getString(JSON_KEY_APP_ID);
			}

			if(headerUId != null) {
				userId = headerUId;
			}
			else {
				userId = jsonObject.getString(JSON_KEY_UID);
			}

			//Mandatory
			commandName = jsonObject.getString(JSON_KEY_CMD_NAME);
			dictationLanguage = jsonObject.getString(JSON_KEY_LANGUAGE);
			appName = jsonObject.getString(JSON_KEY_APP_NAME);

			//Optional
			String[] inCodecOutCodec = getInOutCodecs(jsonObject, commandName);
			String inCodecJsonStringValue = inCodecOutCodec[0];
			String outCodecJsonStringValue = inCodecOutCodec[1];

			Map<String, NcsServletDefines.Pair<ICodecResource.Codec, String>> codecMap = NcsServletDefines.getCodecMap();
			inCodec = codecMap.get(inCodecJsonStringValue).codec;
			outCodec = codecMap.get(outCodecJsonStringValue).codec;
			outCodecMimeType = codecMap.get(outCodecJsonStringValue).mimeType;

			applicationVersion = jsonObject.optString(JSON_KEY_APP_VERSION, "0.0.0");
			carrier = jsonObject.optString(JSON_KEY_CARRIER, "unspecified");
			phoneModel = jsonObject.optString(JSON_KEY_DEVICE_MODEL, "unspecified");
		}
		catch (JSONException e)
		{
			log.error("RequestData: Missing JSON argument error. A mandatory JSON argument is missing.");
			success = false;
		}
		catch (Exception e) // Possible exception is if appKey doesn't contain an even number of chars.
		{
			log.error("RequestData: Error interpreting JSON data. Make sure the appKey is correct, and has an even" +
					"number of chars.");
			success = false;
		}
	}

	static String[] getInOutCodecs(JSONObject jsonObject, String theCommandName)
	{
		String inCodecJsonStringValue = jsonObject.optString(JSON_KEY_IN_CODEC);
		String outCodecJsonStringValue = jsonObject.optString(JSON_KEY_OUT_CODEC);

		//inCodec and outCodec are blank
		if(inCodecJsonStringValue.isBlank() && outCodecJsonStringValue.isBlank())
		{
			inCodecJsonStringValue = "PCM_16_16K";
		}

		//outCodec is blank, but inCodec is set
		if(outCodecJsonStringValue.isBlank())
		{
			outCodecJsonStringValue = inCodecJsonStringValue;
		}

		//inCodec is blank, but outCodec is set
		if(inCodecJsonStringValue.isBlank())
		{
			inCodecJsonStringValue = outCodecJsonStringValue;
		}

		//For TTS cmds, the output codec seems to be ignored
		//so set the inCodec to the outCodec
		if(theCommandName.toUpperCase().contains("_TTS_"))
		{
			inCodecJsonStringValue = outCodecJsonStringValue;
		}

		return new String[] { inCodecJsonStringValue, outCodecJsonStringValue };
	}

	protected List<NmspParameter> getDefaultManagerParameters()
	{
		List<NmspParameter> mgrParameters = new ArrayList<>();
		// SSL settings are handled at Netty pipeline level; kept here only for API compatibility
		mgrParameters.add(new NmspParameter("IdleSessionTimeout", "90".getBytes(), NmspParamType.SDK));
		mgrParameters.add(new NmspParameter("SSL_Socket_Enable", NcsServletDefaults.NMSP_SSL_SOCKET_ENABLE.getBytes().clone(), NmspParamType.SDK));
		mgrParameters.add(new NmspParameter("SSL_SelfSigned_Cert", NcsServletDefaults.NMSP_SSL_SELF_SIGNED_CERT.getBytes().clone(), NmspParamType.SDK));
		return mgrParameters;
	}

	NmspParamType getParmType(String s) {
		if(s.equals("SDK"))    return NmspParamType.SDK;
		if(s.equals("NMSP"))   return NmspParamType.NMSP;
		if(s.equals("APP"))    return NmspParamType.APP;
		if(s.equals("NSS"))    return NmspParamType.NSS;
		if(s.equals("SLOG"))   return NmspParamType.SLOG;
		if(s.equals("NSSLOG")) return NmspParamType.NSSLOG;
		if(s.equals("GWLOG"))  return NmspParamType.GWLOG;
		if(s.equals("SVSP"))   return NmspParamType.SVSP;
		if(s.equals("SIP"))    return NmspParamType.SIP;
		if(s.equals("SDP"))    return NmspParamType.SDP;
		return null;
	}

	public boolean succeeded() { return success; }
	public String getServerIP() { return serverIP; }
	public short getServerPort() { return serverPort; }
	public byte[] getAppKey() { return appKey; }
	public String getAppID() { return appID; }
	public String getUserId() { return userId; }
	public ICodecResource.Codec getInCodec() { return inCodec; }
	public ICodecResource.Codec getOutCodec() { return outCodec; }
	public List<NmspParameter> getParameters() { return parameters; }
	public String getAppName() { return appName; }
	public String getCommandName() { return commandName; }
	public String getApplicationVersion() { return applicationVersion; }
	public String getDictationLanguage() { return dictationLanguage; }
	public String getCarrier() { return carrier; }
	public String getPhoneModel() { return phoneModel; }
	public NmspDictionary getOptionalKeys() { return optionalKeys; }
	public long getCommandTimeout() { return commandTimeout; }
	public String getOutCodecMimeType() { return outCodecMimeType; }
	public String getCorsOrigin() { return corsOrigin; }
}
