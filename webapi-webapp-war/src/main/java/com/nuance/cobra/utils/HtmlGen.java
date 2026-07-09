package com.nuance.cobra.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HtmlGen
{
    private static final String DOCTYPE = "<!DOCTYPE html>\n";

    private static final String HTML = "html";

    public static final String AREA = "area";
    public static final String BASE = "base";
    public static final String BODY = "body";
    public static final String BR = "br";
    public static final String COL = "col";
    public static final String COMMAND = "command";
    public static final String EMBED = "embed";
    public static final String HEAD = "head";
    public static final String HR = "hr";
    public static final String IMG = "img";
    public static final String INPUT = "input";
    public static final String KEYGEN = "keygen";
    public static final String LINK = "link";
    public static final String META = "meta";
    public static final String PARAM = "param";
    public static final String SOURCE = "source";
    public static final String TABLE = "table";
    public static final String TD = "td";
    public static final String TH = "th";
    public static final String TITLE = "title";
    public static final String TR = "tr";
    public static final String TRACK = "track";
    public static final String WBR = "wbr";


    interface Node
    {
        String render();
    }

    public static class TextNode implements Node
    {
        final String text;

        public TextNode(String text)
        {
            this.text = text;
        }

        @Override
        public String render()
        {
            return text;
        }
    }

    public static class Attribute
    {
        final String name;
        final String value;

        public Attribute(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        String render()
        {
            return " " + name + "=\"" + value + "\"";
        }
    }

    public static class Element implements Node
    {
        private static final List<String> EMPTY_ELEMENTS = Arrays.asList(
                AREA, BASE, BR, COL, COMMAND, EMBED, HR, IMG,
                INPUT, KEYGEN, LINK, META, PARAM, SOURCE, TRACK, WBR
        );

        private static boolean close(String tag)
        {
            return ! EMPTY_ELEMENTS.contains(tag.toLowerCase(Locale.ENGLISH));
        }

        final String name;
        final boolean close;
        final List<Attribute> attributes = new ArrayList<>();
        final List<Node> children = new ArrayList<>();

        private Element(String name, Attribute ... attributes)
        {
            this.name = name;
            this.close = close(name);
            this.attributes.addAll(Arrays.asList(attributes));
        }

        public void append(Node ... nodes)
        {
            children.addAll(Arrays.asList(nodes));
        }

        public void addAttributes(Attribute ... attributes)
        {
            this.attributes.addAll(Arrays.asList(attributes));
        }

        public void insert(int position, Node ... nodes)
        {
            children.addAll(position, Arrays.asList(nodes));
        }


        public Element add(String name, Attribute ... attributes)
        {
            final Element element = new Element(name, attributes);
            append(element);

            return element;
        }

        public Element add(String name, String text, Attribute ... attributes)
        {
            final Element element = add(name, attributes);
            element.append(new TextNode(text));
            return element;
        }

        @Override
        public String render()
        {
            final StringBuilder sb = new StringBuilder();

            sb.append("<");
            sb.append(name);
            for (final Attribute attribute : attributes)
            {
                sb.append(attribute.render());
            }
            sb.append(">");

            for (final Node child : children)
            {
                sb.append(child.render());
            }

            if (close)
            {
                sb.append("</");
                sb.append(name);
                sb.append(">");
            }

            return sb.toString();
        }
    }

    public static class HtmlElement extends Element
    {
        private final HeadElement head;
        private final Element body;

        HtmlElement()
        {
            super(HTML);
            head = new HeadElement();
            body = new Element(BODY);
            append(head, body);
        }

        public HeadElement head()
        {
            return head;
        }

        public Element body()
        {
            return body;
        }

        @Override
        public String render()
        {
            return DOCTYPE + super.render();
        }
    }

    public static class HeadElement extends Element
    {
        HeadElement()
        {
            super(HEAD);
        }

        public void title(String title)
        {
            add(TITLE, title);
        }

        public void stylesheet(String href)
        {
            add(LINK, attr("rel", "stylesheet"), attr("type", "text/css"), attr("href", href));
        }
    }

    public static HtmlElement html()
    {
        return new HtmlElement();
    }

    public static Attribute attr(String name, String value)
    {
        return new Attribute(name, value);
    }

    public static Attribute cls(String value)
    {
        return attr("class", value);
    }

    public static Attribute id(String value)
    {
        return attr("id", value);
    }

}
