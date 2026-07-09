package com.nuance.nmsp.servlet.nmas.upload;

import com.nuance.nmsp.servlet.util.ExcludeFromJacocoGeneratedReport;

import java.util.ArrayList;
import java.util.List;

@ExcludeFromJacocoGeneratedReport
public class DataBlock
{
    private int deleteall;
    private final List<Data> datalist;

    private boolean success;
    private String errorMsg;

    public DataBlock()
    {
        deleteall = 0;
        datalist = new ArrayList<>();
        success = true;
        errorMsg = "";
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setErrorMsg(int r, int c, String msg)
    {
        success = false;
        errorMsg = msg + " at line:" + r + " column:" + c;
    }

    public void setMissingTagError(int r, int c, String tag1, String tag2)
    {
        success = false;
        errorMsg = "Can not have a <" + tag1 + "> tag without a <" + tag2 + "> tag at line:" + r + " column:" + c;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public int getDeleteAll()
    {
        return deleteall;
    }

    public void setDeleteAll(boolean delete)
    {
        deleteall = (delete) ? 1 : 0;
    }

    public void addData(Data data)
    {
        if (data != null)
        {
            datalist.add(data);
        }
    }

    public List<Data> getDataList()
    {
        return datalist;
    }

    public String toString()
    {
        StringBuilder datablock = new StringBuilder();
        datablock.append("delete_all:" + deleteall + "\n");
        if (!(datalist.isEmpty()))
        {
            int size = datalist.size();
            int idx;
            Data d = null;
            datablock.append("data_list: elements " + size + "\n");
            for (idx = 0; idx < size; idx++)
            {
                d = datalist.get(idx);
                if (d != null)
                {
                    datablock.append("data: " + idx + "\n");
                    datablock.append(d);
                }
            }
        }
        return datablock.toString();
    }
}
