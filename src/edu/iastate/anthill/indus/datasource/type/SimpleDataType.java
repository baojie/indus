package edu.iastate.anthill.indus.datasource.type;

import java.util.Vector;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import edu.iastate.anthill.indus.datasource.Configable;

import edu.iastate.utils.string.TaggedText;

/**
 * @author Jie Bao
 * @since 1.0
 */
public class SimpleDataType
    extends DataType implements Configable
{
    public SimpleDataType(String name, String supertype)
    {
        this.name = name;
        this.supertype = supertype;
    }

    public SimpleDataType()
    {}

    /**
     *
     * @param datatypeinXML String
     */
    public void fromXML(String datatypeinXML)
    {
        TaggedText root = new TaggedText();
        root.fromXML(datatypeinXML);

        Vector allChildren = root.getAllChildren();
        //Debug.trace(allChildren.size());
        for (int i = 0; i < allChildren.size(); i++)
        {
            TaggedText t = (TaggedText) allChildren.elementAt(i);
            if (t.getTag().equals("typename"))
            {
                name = t.getContent().elementAt(0).toString();
            }
            else if (t.getTag().equals("subTypeOf"))
            {
                supertype = t.getContent().elementAt(0).toString();
            }
        }
    }

    /**
     * getEditor
     *
     * @return JPanel
     * @todo Implement this edu.iastate.anthill.indus.applet.type.DataType
     *   method
     */
    public JPanel getEditorPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JTextArea("Predefined type cannot be edited"));
        return p;
    }

    /**
     * getInformation
     *
     * @return String
     * @todo Implement this edu.iastate.anthill.indus.applet.type.DataType
     *   method
     */
    public String getInformation()
    {
        String info = "";
        if (getSupertype() != null)
        {
            info += "is subtype of '" + getSupertype() + "'";
        }
        else if (isPredefinedType(this.name))
        {
            info += "is predefined type";

        }
        return info;
    }

    /**
     * print
     *
     * @return String
     * @todo Implement this edu.iastate.anthill.indus.applet.type.DataType
     *   method
     */
    public String print()
    {
        return getTaggedText().toString();
    }

    /**
     *
     * @return String
     * @todo Implement this edu.iastate.anthill.indus.applet.type.DataType
     *   method
     */
    public String toXML()
    {
        return getTaggedText().toXML();
    }

    TaggedText getTaggedText()
    {
        TaggedText root = new TaggedText("type", new Vector());
        root.addChild("typename", name);
        if (supertype != null)
        {
            root.addChild("subTypeOf", supertype);
        }
        return root;
    }

    public String toString()
    {
        return "Type: " + name + "\n" + getInformation();
    }

    /**
     * Save datatype to plain text
     * @author baojie
     * @since 2006-06-19
     */
    public String toText() {
        StringBuffer buf = new StringBuffer();
        buf.append(";typename="+name+"\n");
        if (supertype != null)
        {
            buf.append(";subTypeOf="+supertype+"\n");            
        }
        return buf.toString();
    }    
    /**
     * Read the datatype from plain text
     * 
     * @author baojie
     * @since 2006-06-19
     * @param datatypeinText
     */
    public void fromText(String datatypeinText)
    {
        try {
            BufferedReader in = new BufferedReader(new StringReader(datatypeinText));
            String str;
            while ((str = in.readLine()) != null) {
                int pos = str.lastIndexOf('=');
                String value = str.substring(pos+1);
                if (str.startsWith(";typename="))
                {
                    name = value;
                }else if (str.startsWith(";subTypeOf="))
                {
                    supertype = value;
                }
                if (!str.startsWith(";"))
                    break;
                
            }
            in.close();
        } catch (IOException e) {
        }
    }
}
