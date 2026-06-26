package com.nuance.nmsp.servlet.nmas.upload;

import com.nuance.nmsp.servlet.util.ExcludeFromJacocoGeneratedReport;

import java.util.ArrayList;
import java.util.List;

@ExcludeFromJacocoGeneratedReport
public class Contact
{

    private String firstname;
    private String lastname;
    private String fullname;
    private String nickname;
    private final List<String> locations;

    public Contact()
    {
        firstname = null;
        lastname = null;
        fullname = null;
        nickname = null;
        locations = new ArrayList<>();
    }

    public void addLocation(String loc)
    {
        locations.add(loc);
    }

    public String getFirstName()
    {
        return firstname;
    }

    public void setFirstName(String name)
    {
        firstname = name;
    }

    public String getLastName()
    {
        return lastname;
    }

    public void setLastName(String name)
    {
        lastname = name;
    }

    public String getFullName()
    {
        return fullname;
    }

    public void setFullName(String name)
    {
        fullname = name;
    }

    public String getNickName()
    {
        return nickname;
    }

    public void setNickName(String name)
    {
        nickname = name;
    }

    public List<String> getLocations()
    {
        return locations;
    }

    public String toString()
    {
        StringBuilder contact = new StringBuilder();

        if (firstname != null)
        {
            contact.append("firstname:" + firstname + "\n");
        }
        if (lastname != null)
        {
            contact.append("lastname:" + lastname + "\n");
        }
        if (fullname != null)
        {
            contact.append("fullname:" + fullname + "\n");
        }
        if (nickname != null)
        {
            contact.append("nickname:" + nickname + "\n");
        }

        if (!(locations.isEmpty()))
        {
            int size = locations.size();
            int idx;
            String loc = null;
            contact.append("locations list: " + size + "\n");
            for (idx = 0; idx < size; idx++)
            {
                loc = locations.get(idx);
                if (loc != null)
                {
                    contact.append("location: " + idx + " " + loc + "\n");
                }
            }
        }

        return contact.toString();
    }
}
