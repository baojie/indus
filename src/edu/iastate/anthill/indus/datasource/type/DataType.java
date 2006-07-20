package edu.iastate.anthill.indus.datasource.type;

import java.io.Serializable;
import java.util.Vector;

import javax.swing.JPanel;

import edu.iastate.anthill.indus.datasource.Configable;

import edu.iastate.utils.string.SimpleXMLParser;

/**
 * @author Jie Bao
 * @since 1.0 2004-10-28
 */
public abstract class DataType
    implements Configable, Serializable
{
    public boolean modified = false;
    public boolean readOnly = false;

    public static final String DEFAULT_TYPES =
        "integer;float;string;boolean;AVH";
//    "integer;string;float;boolean;date;AVH;DAG";
    String name = null;
    String supertype = null;

    public static boolean isLegalName(String name)
    {
        if (name == null)
        {
            return false;
        }
        else if (name.length() == 0)
        {
            return false;
        }
        else if (!name.matches("[a-zA-Z][\\s\\w\\-._]*"))
        {
            return false;
        }
        return true;
    }

    /**
     * @param supertype String
     * @return boolean
     * @since 2004-10-15
     */
    public static boolean isString(String type, String supertype)
    {
        if (type.equals("string"))
        {
            return true;
        }
        else if (supertype == null)
        {
            return false;
        }
        else
        {
            return supertype.equals("string");
        }
    }

    /**
     * Return all predefined types in list
     * @return Stirng[]
     * @since 2004-10-29
     */
    public static String[] getPredefinedTypes()
    {
        return DEFAULT_TYPES.split(";");
    }

    /**
     * @param supertype String
     * @return boolean
     * @since 2004-10-15
     */ public static boolean isNumber(String type, String supertype)
    {
        if (type.equals("integer") || type.equals("float"))
        {
            return true;
        }
        else if (supertype == null)
        {
            return false;
        }
        else
        {
            return (supertype.equals("integer") || supertype.equals("float"));
        }
    }

    public String getSupertype()
    {
        return supertype;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String typeName)
    {
        this.name = typeName;
    }

    /**
     * To judge if a type if a predefined type
     * @param typName String
     * @return boolean
     * @author Jie Bao
     * @since 2004-10-07
     */
    public static boolean isPredefinedType(String typName)
    {
        return (DEFAULT_TYPES.indexOf(typName) != -1);
    }

    /**
     * Find the supertype of the type XML
     * @param datatypeinXML String
     * @return String - get the super type
     * @since 2004-10-15
     */
    public static String parseSupertype(String datatypeinXML)
    {
        Vector vec = SimpleXMLParser.getNestedBlock("subTypeOf", datatypeinXML, false);
        if (vec.size() > 0)
        {
            return (String) vec.elementAt(0);
        }
        return null;
    }

    abstract public JPanel getEditorPane();

    abstract public String print();

    abstract public String toString();

    abstract public String toText();

    abstract public String getInformation(); 

}
