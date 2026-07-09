package com.nuance.nmsp.servlet.nmas.upload;

import com.nuance.nmsp.servlet.util.ExcludeFromJacocoGeneratedReport;

import java.util.ArrayList;
import java.util.List;

@ExcludeFromJacocoGeneratedReport
public class Data
{

    public static final String CONTACTS = "contacts";
    public static final String CUSTOM_WORDS = "custom_words";
    public static final String HISTORY = "history";

    private String id;
    private String checksum;
    private String oldChecksum;
    private String type;
    private final List<Action> actions;

    public Data()
    {
        id = null;
        checksum = null;
        type = null;
        actions = new ArrayList<>();
    }

    public void clearData()
    {
        id = null;
        checksum = null;
        type = null;
        actions.clear();
    }

    public boolean haveId()
    {
        return (id != null);
    }

    public boolean haveChecksum()
    {
        return (checksum != null);
    }

    public boolean haveType()
    {
        return (type != null);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        type = type.toLowerCase();
        if (type.equals(CONTACTS) || type.equals(CUSTOM_WORDS) || type.equals(HISTORY))
        {
            this.type = type;
        }
        else
        {
            this.type = CONTACTS;
        }
    }

    public String getChecksum()
    {
        return checksum;
    }

    public void setChecksum(String checksum)
    {
        this.checksum = checksum;
    }

    public String getOldChecksum()
    {
        return oldChecksum;
    }

    public void setOldChecksum(String oldChecksum)
    {
        this.oldChecksum = oldChecksum;
    }

    public void addAction(Action action)
    {
        if (action != null)
        {
            actions.add(action);
        }
    }

    public List<Action> getActions()
    {
        return actions;
    }

    public String toString()
    {
        StringBuilder data = new StringBuilder();

        data.append("id:" + id + "\n");
        data.append("checksum:" + checksum + "\n");
        data.append("type:" + type + "\n");

        if (!(actions.isEmpty()))
        {
            int size = actions.size();
            int idx;
            Action act = null;
            data.append("action list: " + size + "\n");
            for (idx = 0; idx < size; idx++)
            {
                act = actions.get(idx);
                if (act != null)
                {
                    data.append("action: " + idx + "\n");
                    data.append(act);
                }
            }
        }
        return data.toString();
    }

}
