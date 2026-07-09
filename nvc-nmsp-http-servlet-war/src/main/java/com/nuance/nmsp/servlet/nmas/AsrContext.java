package com.nuance.nmsp.servlet.nmas;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;
import com.nuance.ncs.nmsp.pdx.NmspDictionaryImpl;

public class AsrContext
{
    protected String start = null;
    protected String stop = null;
    protected String text = null;
    protected String logInfo = null;
    protected String audioSource = null;
    protected boolean escapeNewLines = false;
    protected int nBestSize = 10;

    protected static final String START_KEY = "start";

    public String getStart()
    {
        return start;
    }

    public void setStart(String strStart)
    {
        this.start = strStart;
    }

    public String getEnd()
    {
        return stop;
    }

    public void setEnd(String strEnd)
    {
        this.stop = strEnd;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    protected String getLogInfo()
    {
        return logInfo;
    }

    public void setLogInfo(String logInfo)
    {
        this.logInfo = logInfo;
    }

    public String getAudioSource() { return audioSource; }

    public void setAudioSource(String audioSource) { this.audioSource = audioSource; }

    public boolean getEscapeNewLines() { return escapeNewLines; }

    public void setEscapeNewLines(boolean escapeNewLines) { this.escapeNewLines = escapeNewLines; }

    public int getnBestSize() { return nBestSize; }

    public void setnBestSize(int nBestSize) { this.nBestSize = nBestSize; }

    public NmspDictionary getDictionary(boolean useNlu)
    {
        NmspDictionary contextDictionary = new NmspDictionaryImpl();

        if (start == null && stop == null && text == null)
        {
            contextDictionary.addInteger(START_KEY, 0);
            contextDictionary.addInteger("end", 0);
            contextDictionary.addUTF8String("text", "");
        }
        else
        {
            if (start != null)
            {
                contextDictionary.addInteger(START_KEY, Integer.parseInt(start));
            }
            if (stop != null)
            {
                contextDictionary.addInteger("end", Integer.parseInt(stop));
            }
            if (text != null)
            {
                contextDictionary.addUTF8String("text", getText());
            }
            if (logInfo != null)
            {
                contextDictionary.addUTF8String("log_info", getLogInfo());
            }
        }

        if (useNlu)
        {
            NmspDictionary appDataDict = new NmspDictionaryImpl();
            contextDictionary.addDictionary("appserver_data", appDataDict);
            contextDictionary.addInteger("nlsml_results", 1);
        }

        return contextDictionary;
    }
}
