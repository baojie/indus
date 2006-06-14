package edu.iastate.anthill.indus.datasource.view;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.awt.HeadlessException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iastate.anthill.indus.datasource.Configable;

import edu.iastate.utils.log.Config;
import edu.iastate.utils.string.SimpleXMLParser;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-23</p>
 */
public class View
    implements Configable
{
    String name = new String();
    String localSchemaName = new String();
    Map datasourceMapping = new TreeMap(); // from String (datasource name)-> String (mapping name)

    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append("Name = " + name + "\n");
        buf.append("Local Schema Name = " + localSchemaName + "\n");
        buf.append("Remote Data Sources:\n");

        for (Iterator it = datasourceMapping.keySet().iterator();
             it.hasNext(); )
        {
            String ds = (String) it.next();
            String mapping = (String) datasourceMapping.get(ds);
            buf.append("    " + ds + " , with mapping " + mapping + "\n");
        }
        return buf.toString();
    }

    /**
     * write to xml string
     * @return String
     * @since 2005-03-23
     */
    public String toXML()
    {
        try
        {
            Document doc;
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().
                newDocumentBuilder();
            doc = builder.newDocument();

            Element view = doc.createElement("View");
            doc.appendChild(view);
            Element nameNode = doc.createElement("Name");
            nameNode.appendChild(doc.createTextNode(name));
            view.appendChild(nameNode);

            // save it
            Element localschemaNode = doc.createElement("LocalSchema");
            localschemaNode.appendChild(doc.createTextNode(localSchemaName));
            view.appendChild(localschemaNode);

            for (Iterator it = datasourceMapping.keySet().iterator();
                 it.hasNext(); )
            {
                String ds = (String) it.next();
                String mapping = (String) datasourceMapping.get(ds);

                Element dsNode = doc.createElement("DataSource");
                view.appendChild(dsNode);

                Element sourceNode = doc.createElement("Source");
                sourceNode.appendChild(doc.createTextNode(ds));
                dsNode.appendChild(sourceNode);

                Element mappingNode = doc.createElement("Mapping");
                mappingNode.appendChild(doc.createTextNode(mapping));
                dsNode.appendChild(mappingNode);

            }
            // output to some where...
            return SimpleXMLParser.documentToString(doc,"UTF-8");
        }
        catch (DOMException ex)
        {
        }
        catch (FactoryConfigurationError ex)
        {
        }
        catch (ParserConfigurationException ex)
        {
        }
        catch (HeadlessException ex)
        {
        }
        return null;

    }

    /**
     * read from xml string
     * @param xmlText String
     * @since 2005-03-23
     */
    public void fromXML(String xmlText)
    {
        // create a Document
        try
        {
            Document doc = SimpleXMLParser.parseXmlString(xmlText);
            if (doc != null)
            {
                String tag = "View";
                Node viewNode = XPathAPI.selectSingleNode(doc, tag);
                this.name = Config.getProperty(viewNode, "Name");
                Node n = XPathAPI.selectSingleNode(doc, tag);
                this.localSchemaName = Config.getProperty(viewNode,
                    "LocalSchema");

                datasourceMapping.clear();
                NodeList nodelist = XPathAPI.selectNodeList(viewNode,
                    "DataSource");
                for (int i = 0; i < nodelist.getLength(); i++)
                {
                    // Get child node
                    n = nodelist.item(i);
                    String ds = Config.getProperty(n, "Source");
                    String mapping = Config.getProperty(n, "Mapping");
                    datasourceMapping.put(ds, mapping);
                }
            }
        }
        catch (TransformerException ex)
        {
        }
    }

    /**
     * Test toXML, fromXML, toString
     * @author Jie Bao
     * @since 2005-03-23
     */
    public static void test()
    {
        View view = new View();

        view.name = "TestView";
        view.localSchemaName = "TestLocal";
        view.datasourceMapping.put("DS1", "Mapping1");
        view.datasourceMapping.put("DS2", "Mapping2");

        String str = view.toString();
        System.out.println(str);
        String xml = view.toXML();
        System.out.println(SimpleXMLParser.printXMLSkeleton(xml));

        View newview = new View();
        newview.fromXML(xml);
        String newxml = (String) SimpleXMLParser.printXMLSkeleton(newview.toXML());
        System.out.println("\nParsed: \n" + newxml);

    }

    public static void main(String[] args)
    {
        View.test();
    }

    public String getLocalSchemaName()
    {
        return localSchemaName;
    }

    public String getName()
    {
        return name;
    }

    public Map getDatasourceMapping()
    {
        return datasourceMapping;
    }

    public void setLocalSchemaName(String localSchemaName)
    {
        this.localSchemaName = localSchemaName;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getMapping(String schema)
    {
        return (String) datasourceMapping.get(schema);
    }

    public void setMapping(String schema, String mapping)
    {
        datasourceMapping.put(schema, mapping);
    }

    public final static String FROM_DATA_SOURCE = "from_data_source";

}
