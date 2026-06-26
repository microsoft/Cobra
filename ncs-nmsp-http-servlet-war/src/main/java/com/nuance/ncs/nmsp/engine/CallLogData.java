package com.nuance.ncs.nmsp.engine;

import com.nuance.ncs.nmsp.servlet.NcsServletDefines;
import com.nuance.ncs.nmsp.servlet.response.ResponseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CallLogData
{
	private static final Logger log = LogManager.getLogger(CallLogData.class);

	private final TransactionEngine transactionEngine;
	private final String corsOrigin;
	private final ResponseManager responseManager;

	public CallLogData(TransactionEngine transactionEngine, ResponseManager responseManager, String corsOrigin) {
		this.transactionEngine = transactionEngine;
		this.responseManager = responseManager;
		this.corsOrigin = corsOrigin;
	}

	public boolean addCallLog(String jsonCallLog) {
		try {
			JSONObject jsonObject = (JSONObject) new JSONTokener(jsonCallLog).nextValue();
			// Serialize the JSON call log to bytes and send via BCP_LOG
			byte[] logBytes = jsonObject.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
			transactionEngine.sendCallLogBytes(logBytes);
			return true;
		} catch (Exception e) {
			log.error("addCallLog: error processing call log", e);
			return false;
		}
	}
}
