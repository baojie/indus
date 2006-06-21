package edu.iastate.anthill.indus.datasource.mapping;

import java.util.Vector;

import edu.iastate.anthill.indus.datasource.Configable;

import edu.iastate.utils.string.SimpleXMLParser;

public class SimpleConnector
    extends Connector implements Configable
{
    static public SimpleConnector EQU = new SimpleConnector("Equal");
    static public SimpleConnector UNEQU = new SimpleConnector("Unequal");
    static public SimpleConnector INTO = new SimpleConnector("Into");
    static public SimpleConnector ONTO = new SimpleConnector("Onto");
    static public SimpleConnector INCOMP = new SimpleConnector("Incompatible");
    static public SimpleConnector COMP = new SimpleConnector("Compatible");

    SimpleConnector(String newname)
    {
        super(newname);
    }

    public String toText() {
        return toXML();
    }

    public void fromText(String text) {
        fromXML(text);        
    }

    /**
     * Export to XML
     * @return String
     * @author Jie Bao
     * @since 2004-10-16
     */
    public String toXML()
    {
        return "<connectorname>" + name + "</connectorname>";
    }

    /**
     * read from XML
     * @param textXML String
     * @author Jie Bao
     * @since 2004-10-16
     */
    public void fromXML(String textXML)
    {
        name = null;
        Vector vec = SimpleXMLParser.getNestedBlock("connectorname", textXML, false);
        if (vec.size() > 0)
        {
            name = (String) vec.elementAt(0);
        }
    }

    public Connector getMirror()
    {
        if (this.equals(EQU))
        {
            return EQU;
        }
        else if (this.equals(UNEQU))
        {
            return UNEQU;
        }
        else if (this.equals(UNEQU))
        {
            return UNEQU;
        }
        else if (this.equals(INTO))
        {
            return ONTO;
        }
        else if (this.equals(ONTO))
        {
            return INTO;
        }
        else if (this.equals(INCOMP))
        {
            return INCOMP;
        }
        else if (this.equals(COMP))
        {
            return COMP;
        }
        return this;
    }
}
