package com.nuance.ncs.nmsp.servlet.utils;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;
import com.nuance.ncs.nmsp.pdx.NmspSequence;
import com.nuance.ncs.nmsp.pdx.NmspSequenceImpl;
import com.nuance.ncs.nmsp.pdx.NmspValueType;
import com.nuance.nmsp.servlet.nmas.MyQueryError;
import com.nuance.nmsp.servlet.nmas.MyQueryRetry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

@SuppressWarnings("java:S1168")
public class JsonPdxConverter {

	JsonPdxConverter() { throw new IllegalStateException("Utility class!"); }

	public static NmspDictionary jsonObjectToPdxDictionary(JSONObject jsonObject)
	{
		NmspDictionaryImpl retDict = new NmspDictionaryImpl();
		Iterator<?> keys = jsonObject.keys();

		while( keys.hasNext() ){
			String key = (String)keys.next();
			if( jsonObject.get(key) instanceof JSONObject ){
				retDict.addDictionary(key, jsonObjectToPdxDictionary(jsonObject.getJSONObject(key)));
			}
			else if( jsonObject.get(key) instanceof JSONArray ){
				byte[] byteArray = jsonArrayToByteArray(jsonObject.getJSONArray(key));
				if (byteArray != null) {
					retDict.addByteString(key, byteArray);
				}
				else
					retDict.addSequence(key, jsonArrayToPdxSequence(jsonObject.getJSONArray(key)));
			}
			else if( jsonObject.get(key) instanceof String ){
				retDict.addUTF8String(key, jsonObject.getString(key));
			}
			else if( jsonObject.get(key) instanceof Integer ){
				retDict.addInteger(key, jsonObject.getInt(key));
			}
		}
		return retDict;
	}

	public static NmspSequence jsonArrayToPdxSequence(JSONArray jsonArray)
	{
		NmspSequenceImpl retSeq = new NmspSequenceImpl();

		for(int i = 0; i < jsonArray.length(); i++ ){
			if( jsonArray.get(i) instanceof JSONObject ){
				retSeq.addDictionary(jsonObjectToPdxDictionary(jsonArray.getJSONObject(i)));
			}
			else if( jsonArray.get(i) instanceof JSONArray ){
				byte[] byteArray = jsonArrayToByteArray(jsonArray.getJSONArray(i));
				if (byteArray != null) {
					retSeq.addByteString(byteArray);
				}
				else
					retSeq.addSequence(jsonArrayToPdxSequence(jsonArray.getJSONArray(i)));
			}
			else if( jsonArray.get(i) instanceof String ){
				retSeq.addUTF8String(jsonArray.getString(i));
			}
			else if( jsonArray.get(i) instanceof Integer ){
				retSeq.addInteger(jsonArray.getInt(i));
			}
		}
		return retSeq;
	}

	public static byte[] jsonArrayToByteArray(JSONArray jsonArray)
	{
		byte[] retArray = new byte[jsonArray.length()];
		for(int i = 0; i < jsonArray.length(); i++ ){
			if( jsonArray.get(i) instanceof Integer ){
				retArray[i] = (byte) jsonArray.getInt(i);
			}
			else {
				return null;
			}
		}
		return retArray;
	}

	public static JSONObject pdxDictionaryToJsonObject(NmspDictionary dictionary)
	{
		JSONObject retJSON = new JSONObject();

		for (String key : dictionary.keySet())
		{
			NmspValueType type = dictionary.getType(key);
			switch (type)
			{
				case INTEGER:
					retJSON.put(key, dictionary.getInteger(key));
					break;
				case BYTESTRING:
					byte[] data = dictionary.getByteString(key);
					for (byte datum : data)
					{
						retJSON.accumulate(key, datum);
					}
					break;
				case UTF8STRING:
					retJSON.put(key, dictionary.getUTF8String(key));
					break;
				case ASCIISTRING:
					retJSON.put(key, dictionary.getAsciiString(key));
					break;
				case NULL:
					retJSON.put(key, 0);
					break;
				case DICTIONARY:
					retJSON.put(key, pdxDictionaryToJsonObject(dictionary.getDictionary(key)));
					break;
				case SEQUENCE:
					retJSON.put(key, pdxSequenceToJsonArray(dictionary.getSequence(key)));
					break;
				default:
					return null;
			}
		}
		return retJSON;
	}

	public static JSONArray pdxSequenceToJsonArray(NmspSequence sequence)
	{
		JSONArray retJSON = new JSONArray();

		for (int i = 0; i < sequence.size(); i++)
		{
			NmspValueType type = sequence.getType(i);
			switch (type)
			{
				case INTEGER:
					retJSON.put(i, sequence.getInteger(i));
					break;
				case BYTESTRING:
					byte[] data = sequence.getByteString(i);
					retJSON.put(byteArrayToJsonArray(data));
					break;
				case UTF8STRING:
					retJSON.put(i, sequence.getUTF8String(i));
					break;
				case ASCIISTRING:
					retJSON.put(i, sequence.getAsciiString(i));
					break;
				case NULL:
					retJSON.put(i, 0);
					break;
				case DICTIONARY:
					retJSON.put(i, pdxDictionaryToJsonObject(sequence.getDictionary(i)));
					break;
				case SEQUENCE:
					retJSON.put(i, pdxSequenceToJsonArray(sequence.getSequence(i)));
					break;
				default:
					return null;
			}
		}
		return retJSON;
	}

	public static JSONArray byteArrayToJsonArray(byte[] byteArray)
	{
		JSONArray retArray = new JSONArray();
		for(int i = 0; i < byteArray.length; i++ ){
			retArray.put(i, byteArray[i]);
		}
		return retArray;
	}

	public static JSONObject queryErrorToJsonObject(MyQueryError queryError)
	{
		JSONObject retJSON = new JSONObject();

		retJSON.put("Error", queryError.getError());
		retJSON.put("Description", queryError.getDescription());

		return retJSON;
	}

	public static JSONObject queryRetryToJsonObject(MyQueryRetry queryRetry)
	{
		JSONObject retJSON = new JSONObject();

		retJSON.put("Cause", queryRetry.getCause());
		retJSON.put("Name", queryRetry.getName());
		retJSON.put("Prompt", queryRetry.getPrompt());

		return retJSON;
	}
}
