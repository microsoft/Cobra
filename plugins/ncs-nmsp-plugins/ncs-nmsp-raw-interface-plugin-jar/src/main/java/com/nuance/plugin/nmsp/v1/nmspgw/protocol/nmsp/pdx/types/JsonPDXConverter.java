package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.Iterator;

@SuppressWarnings("java:S1168")
public class JsonPDXConverter
{
    JsonPDXConverter() { throw new IllegalStateException("Utility class!"); }

    public static Dictionary jsonObjectToPdxDictionary(JSONObject jsonObject)
    {
        Dictionary retDict = new PDXDictionary();
        Iterator<?> keys = jsonObject.keys();

        while( keys.hasNext() ){
            String key = (String)keys.next();
            if( jsonObject.get(key) instanceof JSONObject ){
                retDict.addDictionary(key, jsonObjectToPdxDictionary(jsonObject.getJSONObject(key)));
            }
            else if( jsonObject.get(key) instanceof JSONArray){
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

    public static Sequence jsonArrayToPdxSequence(JSONArray jsonArray)
    {
        Sequence retSeq = new PDXSequence();

        for(int i = 0; i < jsonArray.length(); i++ ){
            if( jsonArray.get(i) instanceof JSONObject ){
                retSeq.addDictionary( jsonObjectToPdxDictionary(jsonArray.getJSONObject(i)));
            }
            else if( jsonArray.get(i) instanceof JSONArray ){
                byte[] byteArray = jsonArrayToByteArray(jsonArray.getJSONArray(i));
                if (byteArray != null) {
                    retSeq.addByteString( byteArray);
                }
                else
                    retSeq.addSequence( jsonArrayToPdxSequence(jsonArray.getJSONArray(i)));
            }
            else if( jsonArray.get(i) instanceof String ){
                retSeq.addUTF8String( jsonArray.getString(i));
            }
            else if( jsonArray.get(i) instanceof Integer ){
                retSeq.addInteger( jsonArray.getInt(i));
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

    public static JSONObject pdxDictionaryToJsonObject(Dictionary dictionary)
    {
        JSONObject retJSON = new JSONObject();

        Enumeration<?> keys = dictionary.keys();

        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();

            switch (dictionary.getType(key))
            {
                case PDXClass.INT:
                    retJSON.put(key, dictionary.getInteger(key));
                    break;
                case PDXClass.BYTES:
                    byte[] data = dictionary.getByteString(key);
                    for (byte datum : data)
                    {
                        retJSON.accumulate(key, datum);
                    }
                    break;
                case PDXClass.UTF8:
                    retJSON.put(key, dictionary.getUTF8String(key));
                    break;
                case PDXClass.ASCII:
                    retJSON.put(key, dictionary.getAsciiString(key));
                    break;
                case PDXClass.NULL:
                    retJSON.put(key, 0);
                    break;
                case PDXClass.DICT:
                    retJSON.put(key, pdxDictionaryToJsonObject(dictionary.getDictionary(key)));
                    break;
                case PDXClass.SEQ:
                    retJSON.put(key, pdxSequenceToJsonArray(dictionary.getSequence(key)));
                    break;
                default:
                    return null;
            }
        }
        return retJSON;
    }

    public static JSONArray pdxSequenceToJsonArray(Sequence sequence)
    {
        JSONArray retJSON = new JSONArray();

        for (int i = 0; i < sequence.size(); i++)
        {
            switch (sequence.getType(i))
            {
                case PDXClass.INT:
                    retJSON.put(i, sequence.getInteger(i));
                    break;
                case PDXClass.BYTES:
                    byte[] data = sequence.getByteString(i);
                    retJSON.put(byteArrayToJsonArray(data));
                    break;
                case PDXClass.UTF8:
                    retJSON.put(i, sequence.getUTF8String(i));
                    break;
                case PDXClass.ASCII:
                    retJSON.put(i, sequence.getAsciiString(i));
                    break;
                case PDXClass.NULL:
                    retJSON.put(i, 0);
                    break;
                case PDXClass.DICT:
                    retJSON.put(i, pdxDictionaryToJsonObject(sequence.getDictionary(i)));
                    break;
                case PDXClass.SEQ:
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
}
