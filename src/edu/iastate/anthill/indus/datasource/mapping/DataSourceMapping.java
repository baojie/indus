package edu.iastate.anthill.indus.datasource.mapping;

import java.util.Vector;

import edu.iastate.anthill.indus.datasource.Configable;

import edu.iastate.utils.string.SimpleXMLParser;
import edu.iastate.utils.string.TaggedText;

/**
 * Handle ontology mapping
 * @author Jie Bao
 * @since 1.0 2004-10-02
 */

public class DataSourceMapping
    implements Configable
{

    public final Connector[] defaultConnectors =
        {
        SimpleConnector.EQU, SimpleConnector.UNEQU, SimpleConnector.INTO,
        SimpleConnector.ONTO, SimpleConnector.INCOMP, SimpleConnector.COMP};

    public DataSourceMapping()
    {}

    public DataSourceMapping(String schema1, String schema2, String name)
    {
        schemaMapping = new SchemaMapping(schema1, schema2);
        if (name == null)
        {
            this.name = schema1 + "-" + schema2;
        }
        else
        {
            this.name = name;
        }
    }

    /**
     * @return String
     * @since 2004-10-12
     */
    public String getName()
    {
        return name;
    }

    /**
     * read mapping from the xml file
     * @param xmlFile String
     * @since 2004-10-04
     */
    public void fromXML(String xmlText)
    {
        try
        {
            // replace all "\n"
            //xmlText = xmlText.replaceAll("\\n","");

            clear();
            Vector vec = SimpleXMLParser.getNestedBlock("mapping", xmlText, false);

            if (vec.size() > 0)
            {
                String mapping = (String) vec.elementAt(0);
                //schema
                vec = SimpleXMLParser.getNestedBlock("schema", mapping, false);
                if (vec.size() > 0)
                {
                    String schema = (String) vec.elementAt(0);
                    SchemaMapping m = new SchemaMapping("", "");
                    m.fromXML(schema);
                    setSchemaMapping(m);
                }
                // name
                vec = SimpleXMLParser.getNestedBlock("mappingname", mapping, false);
                if (vec.size() > 0)
                {
                    name = (String) vec.elementAt(0);
                }
                else
                {
                    name = schemaMapping.from + "-" + schemaMapping.to;
                }

                //avh
                vec = SimpleXMLParser.getNestedBlock("avh", mapping, false);
                if (vec.size() > 0)
                {
                    for (int i = 0; i < vec.size(); i++)
                    {
                        String avh = (String) vec.elementAt(i);
                        InMemoryOntologyMapping m = new InMemoryOntologyMapping(null, null);
                        m.fromXML(avh);
                        avhMappingList.add(m);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public String toText() {
        return toXML();
    }

    public void fromText(String text) {
        fromXML(text);        
    }

    public String toXML()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("<mapping>");
        if (name != null)
        {
            buf.append("<mappingname>" + name + "</mappingname>");
        }
        if (schemaMapping != null)
        {
            buf.append("<schema>" + schemaMapping.toXML() + "</schema>");
        }
        for (int i = 0; i < avhMappingList.size(); i++)
        {
            buf.append("<avh>" +
                       ( (InMemoryOntologyMapping) avhMappingList.elementAt(i)).
                       toXML() +
                       "</avh>");
        }
        buf.append("</mapping>");
        return buf.toString();
    }

    public SchemaMapping schemaMapping = new SchemaMapping("", "");
    public Vector avhMappingList = new Vector(); // vector of InMemoryOntologyMapping
    public String name = null;

    public void setSchemaMapping(SchemaMapping schemaMapping)
    {
        this.schemaMapping = schemaMapping;
    }

    public void addAVHMapping(InMemoryOntologyMapping avhMapping)
    {
        avhMappingList.add(avhMapping);
    }

    public BridgeRule addSchemaMappingItem(String term1, Connector c,
                                           String term2)
    {
        return schemaMapping.addMapping(term1, c, term2);
    }

    public boolean deleteSchemaMappingItem(String term1, Connector c,
                                           String term2)
    {
        return schemaMapping.deleteMapping(term1, c, term2);
    }

    public BridgeRule addAVHMappingItem(String AVH1, String term1, Connector c,
                                        String AVH2, String term2)
    {
        // find the AVH mapping
        for (int i = 0; i < avhMappingList.size(); i++)
        {
            InMemoryOntologyMapping m = (InMemoryOntologyMapping) avhMappingList.elementAt(i);
            if (m.from.equals(AVH1) && m.to.equals(AVH2))
            {
                return m.addMapping(term1, c, term2);
            }
        }
        // not find
        InMemoryOntologyMapping m = new InMemoryOntologyMapping(AVH1, AVH2);
        BridgeRule t = m.addMapping(term1, c, term2);
        avhMappingList.add(m);
        return t;
    }

    /**
     * Delete a AVH mapping bridge rules
     *
     * @param AVH1 String
     * @param term1 String
     * @param AVH2 String
     * @param term2 String
     * @return boolean true- an item is delete, otherwise false
     */
    public boolean deleteAVHMappingItem(String AVH1, String term1, Connector c,
                                        String AVH2, String term2)
    {
        // find the AVH mapping
        for (int i = 0; i < avhMappingList.size(); i++)
        {
            InMemoryOntologyMapping m = (InMemoryOntologyMapping) avhMappingList.elementAt(i);
            if (m.from.equals(AVH1) && m.to.equals(AVH2))
            {
                m.deleteMapping(term1, c, term2);
                return true;
            }
        }
        return false;
    }

    /**
     * delete all mapping information
     * 2004-10-03
     */
    public void clear()
    {
        //schemaMapping.clear();
        name = null;
        schemaMapping = null;
        avhMappingList.removeAllElements();
    }

    /**
     * Find the first term in schema2 mapped to the term in schema1
     * @param schema_term1 String
     * @return String
     * @since 2004-10-13
     */
    public String findSchemaFirstMappedTo(String schema_term1)
    {
        return schemaMapping.findFirstMappedTo(schema_term1);
    }

    public String findSchemaFirstMappedFrom(String schema_term1)
    {
        return schemaMapping.findFirstMappedFrom(schema_term1);
    }

    // return result in AVH:term or term
    // 2004-10-13
    public String findAVHFirstMappedTo(String AVH1, String term1,
                                       boolean isShort)
    {
        // find the AVH1
        for (int i = 0; i < avhMappingList.size(); i++)
        {
            InMemoryOntologyMapping avhMapping = (InMemoryOntologyMapping) avhMappingList.
                elementAt(i);
            if (avhMapping.from.equals(AVH1))
            {
                String term2 = avhMapping.findFirstMappedTo(term1);
                if (term2 != null)
                {
                    if (isShort)
                    {
                        return term2;
                    }
                    else
                    {
                        return avhMapping.to + ":" + term2;
                    }
                }
            }
        }
        return null;
    }

    // 2004-10-15
    public String findAVHFirstMappedFrom(String AVH2, String term2,
                                         boolean isShort)
    {
        // find the AVH1
        for (int i = 0; i < avhMappingList.size(); i++)
        {
            InMemoryOntologyMapping avhMapping = (InMemoryOntologyMapping) avhMappingList.
                elementAt(i);
            if (avhMapping.to.equals(AVH2))
            {
                String term1 = avhMapping.findFirstMappedFrom(term2);
                if (term1 != null)
                {
                    if (isShort)
                    {
                        return term1;
                    }
                    else
                    {
                        return avhMapping.from + ":" + term1;
                    }
                }
            }
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
        if (schemaMapping != null)
        {
            vec.addAll(schemaMapping.getUserConnectors());
        }
        for (int i = 0; i < avhMappingList.size(); i++)
        {
            InMemoryOntologyMapping o = ( (InMemoryOntologyMapping) avhMappingList.elementAt(i));
            vec.addAll(o.getUserConnectors());
        }
        return vec;
    }

    /**
     * Find the right mapping given from and to AVH name
     * @param avhFrom String
     * @param avhTo String
     * @return InMemoryOntologyMapping
     * @author Jie Bao
     * @since 2005-03-21
     */
    public InMemoryOntologyMapping findAVHMapping(String avhFrom, String avhTo)
    {
        for (int i = 0; i < avhMappingList.size(); i++)
        {
            InMemoryOntologyMapping o = ( (InMemoryOntologyMapping) avhMappingList.elementAt(i));
            if (o.from.equals(avhFrom) && o.to.equals(avhTo))
            {
                return o;
            }
        }
        return null;
    }

    // 2005-03-28
    public DataSourceMapping getMirror()
    {
        DataSourceMapping inverse = new DataSourceMapping();
        inverse.name = "Inverse-" + this.name;
        inverse.schemaMapping = (SchemaMapping)this.schemaMapping.
            getSchemaMirror();

        for (int i = 0; i < this.avhMappingList.size(); i++)
        {
            InMemoryOntologyMapping m = (InMemoryOntologyMapping) avhMappingList.elementAt(i);
            inverse.avhMappingList.add(m.getMirror());
        }
        return inverse;
    }

    // for test purpose
    public static void main(String[] args)
    {
        DataSourceMapping mapping1 = new DataSourceMapping("A", "B", null);

        // schema mapping
        mapping1.addSchemaMappingItem("t1", SimpleConnector.ONTO, "t2");
        mapping1.addSchemaMappingItem("t4", SimpleConnector.EQU, "t5");
        mapping1.addAVHMappingItem("h1", "ht1", SimpleConnector.COMP, "h2",
                                   "ht2");
        mapping1.addAVHMappingItem("h3", "ht3", SimpleConnector.INCOMP, "h2",
                                   "ht2");

        String xml = mapping1.toXML();
        TaggedText tt = new TaggedText();
        tt.fromXML(xml);
        System.out.println(tt.print(0));

        DataSourceMapping mapping2 = new DataSourceMapping("A", "B", null);
        mapping2.fromXML(xml);

        xml = mapping1.getMirror().toXML();
        tt.fromXML(xml);
        System.out.println(tt.print(0));

    }
}
