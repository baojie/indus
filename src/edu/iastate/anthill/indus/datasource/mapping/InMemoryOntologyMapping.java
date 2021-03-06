package edu.iastate.anthill.indus.datasource.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import edu.iastate.utils.string.SimpleXMLParser;

public class InMemoryOntologyMapping extends OntologyMapping

{
    {
        type = BridgeRule.AVH_COMMENT;
    }
    
    public Vector<BridgeRule> mapList = new Vector<BridgeRule>(); // BridgeRule list

    public InMemoryOntologyMapping(String from, String to)
    {
        super(from, to);
        
    }

    /**
     * <bridge>
     <term1>outlook_AVH</term1>
     <connector>
     <connectorname>Compatible</connectorname>
     </connector>
     <term2>Prec_AVH</term2>
     </bridge>

     * @param rs ResultSet
     * @param fromCol String
     * @param toCol String
     */

    // 2004-10-04
    public void fromXML(String textXML)
    {
        clear();
        //from
        Vector vec = SimpleXMLParser.getNestedBlock("from", textXML, false);
        if (vec.size() > 0)
        {
            from = (String) vec.elementAt(0);
        }
        //to
        vec = SimpleXMLParser.getNestedBlock("to", textXML, false);
        if (vec.size() > 0)
        {
            to = (String) vec.elementAt(0);
        }
        //termmapping
        vec = SimpleXMLParser.getNestedBlock("bridge", textXML, false);
        if (vec.size() > 0)
        {
            for (int i = 0; i < vec.size(); i++)
            {
                String bridgeXML = (String) vec.elementAt(i);
                BridgeRule b = new BridgeRule(from, to, bridgeXML);
                b.type = this.type;
                mapList.add(b);
            }
        }
    }

    public String toXML()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < mapList.size(); i++)
        {
            buf.append(((BridgeRule) mapList.elementAt(i)).toXML());
        }
        return "<from>" + from + "</from>" + "<to>" + to + "</to>"
                + buf.toString();
    }

    public String toText()
    {
        return toXML();
    }

    public void fromText(String text)
    {
        fromXML(text);
    }

    /**
     * Read mapping from plain text e.g.
     * 
     * from=ont1
     * to=ont2
     * term1, Equal, term2
     * term3, Into, term4
     * ....
     * 
     * @author baojie
     * @since 2006-06-27
     * 
     * @param input
     */
    public void fromPlainText(Reader input)
    {

        try
        {
            BufferedReader in = new BufferedReader(input);
            String str = in.readLine();

            while ((str = in.readLine()) != null)
            {
                if (str.startsWith(";"))
                    continue;
                else if(str.startsWith("from="))
                    this.from = str.replace("from=", "");
                else if (str.startsWith("to="))
                    this.to = str.replace("to=", "");
                else
                // term1 , connector , term2
                {
                    String s[] = str.split(",");
                    if (s.length == 3)
                    {
                        SimpleConnector c = new SimpleConnector(s[1].trim());
                        BridgeRule b = new BridgeRule(from, to, s[0].trim(), c,
                                s[2].trim());
                        b.type = BridgeRule.AVH_COMMENT;
                        mapList.add(b);
                    }
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return String
     * @author Jie Bao
     * @since 2005-03-20
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("From: " + from + "\nTo: " + to + "\n");

        for (int i = 0; i < mapList.size(); i++)
        {
            buf.append(((BridgeRule) mapList.elementAt(i)).toString() + "\n");
        }
        return buf.toString();
    }

    // 2004-10-04
    public void clear()
    {
        from = null;
        to = null;
        mapList.removeAllElements();
    }

    public BridgeRule addMapping(String term1, Connector c, String term2)
    {
        BridgeRule t = new BridgeRule(from, to, term1, c, term2);
        mapList.add(t);
        return t;
    }

    /**
     * @param term1 String
     * @param term2 String
     * @return boolean true if delete, false if not found
     * @since 2004-10-03
     */
    public boolean deleteMapping(String term1, Connector c, String term2)
    {
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            if (m.fromTerm.equals(term1) && m.toTerm.equals(term2)
                    && m.connector.equals(c))
            {
                mapList.remove(m);
                return true;
            }
        }
        return false;
    }

    public BridgeRule[] findMapping(String term1, String term2)
    {
        Vector vec = new Vector();
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            if (m.fromTerm.equals(term1) && m.toTerm.equals(term2))
            {
                vec.add(m);
            }
        }
        BridgeRule[] rules = new BridgeRule[vec.size()];
        for (int j = 0; j < rules.length; j++)
        {
            rules[j] = (BridgeRule) vec.elementAt(j);
        }
        return rules;
    }

    // 2005-03-29
    public BridgeRule[] findAppliableMapping(String term1)
    {
        Vector vec = new Vector();
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            if (m.fromTerm.equals(term1))
            {
                vec.add(m);
            }
        }
        BridgeRule[] rules = new BridgeRule[vec.size()];
        for (int j = 0; j < rules.length; j++)
        {
            rules[j] = (BridgeRule) vec.elementAt(j);
        }
        return rules;
    }

    /**
     * Find the first term that this term mapped to
     * @param term1 String
     * @return BridgeRule
     * @since 2004-10-03
     */
    public BridgeRule findFirstMappedTo(String term1)
    {
        //Debug.trace(this, term1);
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            //Debug.trace(this, m.term1 + "->" + m.term2);

            if (m.fromTerm.equals(term1)) { return m; }
        }
        return null;
    }

    /**
     * Find the first term that this term mapped from
     * @param term2 String
     * @return BridgeRule
     * @since 2004-10-03
     */
    public BridgeRule findFirstMappedFrom(String term2)
    {
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            if (m.toTerm.equals(term2)) { return m; }
        }
        return null;
    }

    /**
     *
     * @return Vector
     * @since 2004-10-16
     */
    public Vector getUserConnectors()
    {
        Vector vec = new Vector();
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            if (!(m.connector instanceof SimpleConnector))
            {
                vec.add(m.connector);
            }
        }
        return vec;
    }

    /**
     * Find bridge rules with the given term as from_term
     * @param fromTerm String
     * @return Vector - of BridgeRule
     * @author Jie Bao
     * @since 2005-03-21
     */
    public Vector<BridgeRule> findMapped(String fromTerm)
    {
        Vector<BridgeRule> foundBridge = new Vector<BridgeRule>();
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            if (m.fromTerm.compareTo(fromTerm) == 0)
            {
                foundBridge.add(m);
            }
        }
        return foundBridge;
    }

    /**
     * @since 2005-03-29
     * @return Vector
     */
    public Vector getEQU()
    {
        Vector foundBridge = new Vector();
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            if (m.connector.equals(SimpleConnector.EQU))
            {
                foundBridge.add(m);
            }
        }
        return foundBridge;
    }

    /**
     * Find the first bridge that is EQU with the given term
     * @param fromTerm String
     * @return BridgeRule
     * @author Jie Bao
     * @since 2005-03-21
     */
    public BridgeRule findEqual(String fromTerm)
    {
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            if (m.fromTerm.compareTo(fromTerm) == 0
                    && m.connector.equals(SimpleConnector.EQU)) { return m; }
        }
        return null;
    }

    /**
     * Find the first bridge rule that is EQU or COMP or a numerical mapping
     * @param fromTerm String
     * @return BridgeRule
     * @author Jie Bao
     * @since 2005-03-22
     */
    public BridgeRule findCompatibleOrEqual(String fromTerm)
    {
        //Debug.trace("Try to find rule for : " + fromTerm);
        for (int i = 0; i < mapList.size(); i++)
        {
            BridgeRule m = (BridgeRule) mapList.elementAt(i);
            //Debug.trace("one rule: " + m + "  " + m.fromTerm);
            boolean equ = (m.connector.equals(SimpleConnector.EQU));
            //Debug.trace("equ =" + equ);
            boolean cmpt = (m.connector.equals(SimpleConnector.COMP));
            //Debug.trace("cmpt =" + cmpt);
            boolean num = m.connector instanceof NumericConnector;
            //Debug.trace("num =" + num);
            boolean isFrom = m.fromTerm.equals(fromTerm);
            //Debug.trace("isFrom =" + isFrom);

            if (isFrom && (equ || cmpt || num))
            {
                //Debug.trace("find! " + m);
                return m;
            }
        }
        return null;
    }

    /**
     * Get the inverse mapping
     * @return InMemoryOntologyMapping
     * @since 2005-03-38
     */
    InMemoryOntologyMapping getMirror()
    {
        InMemoryOntologyMapping inverse = new InMemoryOntologyMapping(this.to,
                this.from);
        for (int i = 0; i < mapList.size(); i++)
        {
            inverse.mapList
                    .add(((BridgeRule) mapList.elementAt(i)).getMirror());
        }
        return inverse;
    }

}
