package edu.iastate.anthill.indus.datasource.mapping;

import java.util.Vector;

import edu.iastate.anthill.indus.datasource.Configable;
import edu.iastate.anthill.indus.reasoner.ShortBridgeRule;

import edu.iastate.utils.string.SimpleXMLParser;

public class BridgeRule
    implements Configable
{
    public String fromTerm, toTerm;
    public Connector connector;
    public String fromTerminology, toTerminology;
    String comments = "";

    BridgeRule(String fromTerminology, String toTerminology, String fromTerm,
               Connector c, String toTerm)
    {
        this.fromTerminology = fromTerminology;
        this.toTerminology = toTerminology;
        this.fromTerm = fromTerm;
        this.toTerm = toTerm;
        this.connector = c;
    }

    BridgeRule(String from, String to, String xml)
    {
        this.fromTerminology = from;
        this.toTerminology = to;
        fromXML(xml);
    }

    // 2004-10-17
    public BridgeRule getMirror()
    {
        BridgeRule b = new BridgeRule(toTerminology, fromTerminology,
                                      toTerm, connector.getMirror(), fromTerm);
        b.setComments(this.comments);
        return b;
    }

    public ShortBridgeRule getShort()
    {
        return new ShortBridgeRule(this);
    }

    // 2004-10-04
    public void fromXML(String textXML)
    {
        Vector vec = SimpleXMLParser.getNestedBlock("term1", textXML, false);
        if (vec.size() > 0)
        {
            fromTerm = (String) vec.elementAt(0);
        }
        vec = SimpleXMLParser.getNestedBlock("term2", textXML, false);
        if (vec.size() > 0)
        {
            toTerm = (String) vec.elementAt(0);
        }
        vec = SimpleXMLParser.getNestedBlock("connector", textXML, false);
        if (vec.size() > 0)
        {
            // try NumericConnector
            String xml = (String) vec.elementAt(0);
            if (xml.indexOf("<expression>") != -1)
            {
                connector = new NumericConnector(null, null);
            }
            else
            {
                connector = new SimpleConnector(null);
            }
            connector.fromXML(xml);
        }
    }

    public String toXML()
    {
        return "<bridge><term1>" + fromTerm + "</term1>" +
            "<connector>" + connector.toXML() + "</connector>" +
            "<term2>" + toTerm + "</term2></bridge>";
    }
    
    public String toText() {
        return toXML();
    }

    public void fromText(String text) {
        fromXML(text);        
    }


    public String toString()
    {
        return "[" + fromTerminology + ":" + fromTerm + "][" + connector + "][" +
            toTerminology + ":" +
            toTerm +
            "]";
    }

    public String getComments()
    {
        return comments;
    }

    public void setComments(String comments)
    {
        this.comments = comments;
    }

}
