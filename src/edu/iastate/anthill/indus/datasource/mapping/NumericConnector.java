package edu.iastate.anthill.indus.datasource.mapping;

import java.util.Vector;

import edu.iastate.utils.jep.JEP;

import edu.iastate.anthill.indus.datasource.Configable;

import edu.iastate.utils.string.SimpleXMLParser;

/**
 * @author Jie Bao
 * @since 1.0
 */
public class NumericConnector
    extends Connector implements Configable
{
    public String expression = ""; // like  (x-32)*5/9
    public String inverseExpression = ""; // like x * 9/5 +32

    public NumericConnector(String newname, String expression)
    {
        super(newname);
        this.expression = expression;
    }

    public String toText() {
        return toXML();
    }

    public void fromText(String text) {
        fromXML(text);        
    }

    public String toXML()
    {
        return "<connectorname>" + name + "</connectorname>" +
            "<expression>" + expression + "</expression>" +
            "<inverse>" + inverseExpression + "</inverse>";
    }

    public void fromXML(String textXML)
    {
        name = "";
        Vector vec = SimpleXMLParser.getNestedBlock("connectorname", textXML, false);
        if (vec != null && vec.size() > 0)
        {
            name = (String) vec.elementAt(0);
        }

        expression = "";
        vec = SimpleXMLParser.getNestedBlock("expression", textXML, false);
        if (vec != null && vec.size() > 0)
        {
            expression = (String) vec.elementAt(0);
        }

        inverseExpression = "";
        vec = SimpleXMLParser.getNestedBlock("inverse", textXML, false);
        if (vec != null && vec.size() > 0)
        {
            inverseExpression = (String) vec.elementAt(0);
        }
    }

    // 2005-03-28
    public Connector getMirror()
    {
        if ( (inverseExpression != null) && inverseExpression.length() > 0)
        {
            NumericConnector newConnector = new NumericConnector("Inverse-" +
                name, inverseExpression);
            newConnector.inverseExpression = expression;
            return newConnector;
        }
        return null;
    }

    /**
     * eval - evaluate the expression with given input value
     *
     * @param localValue String
     * @return String
     */
    public String eval(String inputValue)
    {
        JEP myParser = new JEP();
        double xValue = Double.parseDouble(inputValue);
        myParser.addVariable("x", xValue);
        myParser.parseExpression(expression);
        return myParser.getValue() + "";
    }
}
