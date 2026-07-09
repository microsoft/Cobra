package com.nuance.nmsp.servlet.nmas.upload;

import com.nuance.nmsp.servlet.util.ExcludeFromJacocoGeneratedReport;

import java.util.ArrayList;
import java.util.List;

@ExcludeFromJacocoGeneratedReport
public class Action
{

    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    public static final String CLEAR_ALL = "clear_all";

    private final String actionString;
    private final List<Contact> contacts;
    private final List<String> words;

    public Action(String action)
    {
        action = action.toLowerCase();
        if (!(action.equals(ADD) || action.equals(REMOVE) || action.equals(CLEAR_ALL)))
        {
            action = ADD;
        }
        this.actionString = action;
        contacts = new ArrayList<>();
        words = new ArrayList<>();
    }

    public void addContact(Contact contact)
    {
        contacts.add(contact);
    }

    public void addWord(String word)
    {
        words.add(word);
    }

    public String getActionString()
    {
        return actionString;
    }

    public List<Contact> getContacts()
    {
        return contacts;
    }

    public List<String> getWords()
    {
        return words;
    }

    public String toString()
    {
        StringBuilder action = new StringBuilder();

        action.append("action:" + this.actionString + "\n");

        if (!(contacts.isEmpty()))
        {
            int size = contacts.size();
            int idx;
            Contact c = null;
            action.append("contact list: " + size + "\n");
            for (idx = 0; idx < size; idx++)
            {
                c = contacts.get(idx);
                if (c != null)
                {
                    action.append("contact: " + idx + "\n");
                    action.append(c);
                }
            }
        }

        if (!(words.isEmpty()))
        {
            int size = words.size();
            int idx;
            String word = null;
            action.append("word list: " + size + "\n");
            for (idx = 0; idx < size; idx++)
            {
                word = words.get(idx);
                if (word != null)
                {
                    action.append("word: " + idx + " " + word + "\n");
                }
            }
        }

        return action.toString();
    }
}
