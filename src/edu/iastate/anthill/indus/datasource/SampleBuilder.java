package edu.iastate.anthill.indus.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.NumericConnector;
import edu.iastate.anthill.indus.datasource.mapping.InMemoryOntologyMapping;
import edu.iastate.anthill.indus.datasource.mapping.SchemaMapping;
import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.query.SQLQueryBuilder;
import edu.iastate.anthill.indus.query.SQLQueryTranslator;
import edu.iastate.anthill.indus.query.ZConstantEx;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;

/**
 * Class to create sample AVH, Schema, Mapping, Data Source, View for test purpose
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-25</p>
 */
public class SampleBuilder
{
    public SampleBuilder()
    {}

    protected static AVH buildSampleAVH_Location()
    {
        TypedTree ontology = new TypedTree();

        TypedNode top = new TypedNode("USA");

        TypedNode iowa = new TypedNode("Iowa");
        top.add(iowa);
        iowa.add(new TypedNode("Ames"));
        iowa.add(new TypedNode("DesMoines"));

        TypedNode va = new TypedNode("Virginia");
        top.add(va);
        va.add(new TypedNode("Richmond"));
        va.add(new TypedNode("Petersberg"));

        ontology.setTop(top);

        System.out.println("Ontology:\n" + ontology);

        AVH avhTree = new AVH("Location", "ISA");
        avhTree.setTree(ontology);

        return avhTree;
    }

    /**
     * @author Jie Bao
     * @since 2005-03-20
     */
    public static void testRewriteAtomWhere()
    {
        AVH avh = buildSampleAVH_Location();
        TypedTree ontology = avh.getTreeAVH();

        String attribute = "Location";

        SQLQueryTranslator qe = new SQLQueryTranslator();

        String[] ops = SQLQueryBuilder.AVH_OP;

        System.out.println("Test node with parent and children");
        String value = "Iowa";
        for (int i = 0; i < ops.length; i++)
        {
            ZExpression ze = qe.rewriteAtomWhere(attribute, ops[i], value,
                    ontology);
            System.out.println(ops[i] + " : " + ze);
        }

        System.out.println("Test root node");
        value = "USA";
        for (int i = 0; i < ops.length; i++)
        {
            ZExpression ze = qe.rewriteAtomWhere(attribute, ops[i], value,
                    ontology);
            System.out.println(ops[i] + " : " + ze);
        }

        System.out.println("Test leaf node");
        value = "Ames";
        for (int i = 0;; i++)
        {
            ZExpression ze = qe.rewriteAtomWhere(attribute, ops[i], value,
                    ontology);
            System.out.println(ops[i] + " : " + ze);
        }
    }

    protected static AVH buildSampleAVH_Nickname()
    {
        TypedTree ontology = new TypedTree();

        TypedNode gaia = new TypedNode("Gaia");
        TypedNode sam = new TypedNode("UncleSam");
        gaia.add(sam);

        TypedNode iowa = new TypedNode("Hawkeye");
        sam.add(iowa);
        TypedNode midiowa = new TypedNode("MidHawkeye");
        iowa.add(midiowa);

        midiowa.add(new TypedNode("Cyclone"));
        midiowa.add(new TypedNode("Capitol"));

        TypedNode va = new TypedNode("ForLovers");
        sam.add(va);
        va.add(new TypedNode("Colts"));
        va.add(new TypedNode("LastSouthernCity"));

        ontology.setTop(gaia);

        System.out.println("Ontology:\n" + ontology);

        AVH avhTree = new AVH("Nickname", "ISA");
        avhTree.setTree(ontology);

        return avhTree;
    }

    /**
     * build a sample ontology mapping. the ontologies are
     * @return InMemoryOntologyMapping
     * @since 2005-03-21
     */
    public static InMemoryOntologyMapping buildSampleOntologyMapping()
    {
        InMemoryOntologyMapping mapping = new InMemoryOntologyMapping("Location", "Nickname");

        mapping.addMapping("USA", SimpleConnector.EQU, "UncleSam");
        mapping.addMapping("Iowa", SimpleConnector.EQU, "Hawkeye");
        mapping.addMapping("DesMoines", SimpleConnector.EQU, "Capitol");
        mapping.addMapping("Virginia", SimpleConnector.ONTO, "Colts");
        mapping.addMapping("Ames", SimpleConnector.INTO, "UncleSam");
        mapping.addMapping("Ames", SimpleConnector.INTO, "Hawkeye");
        mapping.addMapping("Ames", SimpleConnector.INTO, "Cyclone");
        mapping.addMapping("Ames", SimpleConnector.UNEQU, "LastSouthernCity");
        mapping.addMapping("Richmond", SimpleConnector.UNEQU, "Cyclone");
        mapping.addMapping("Petersberg", SimpleConnector.EQU,
                "LastSouthernCity");        

        System.out.println("Ontology Mapping\n" + mapping.toString());
        return mapping;
    }

    public static Schema buildSampleLocalSchema()
    {
        Schema fromSchema = new Schema("Local");

        fromSchema.addAttribute("temp", "double", "real");
        fromSchema.addAttribute("item", "string", "varchar(20)");
        fromSchema.addAttribute("soldAt", "Location", "varchar(32)");
        fromSchema.addAttribute("produced_at", "Location", "varchar(32)");
        return fromSchema;
    }

    public static Schema buildSampleRemoteSchema()
    {
        Schema toSchema = new Schema("DS1");

        toSchema.addAttribute("temperature", "double", "real");
        toSchema.addAttribute("madeIn", "Nickname", "varchar(32)");
        toSchema.addAttribute("deliveredTo", "Nickname", "varchar(32)");
        toSchema.addAttribute("item", "string", "varchar(20)");
        toSchema.addAttribute("note", "string", "text");
        return toSchema;
    }

    /**
     * @return InMemoryOntologyMapping
     * @since 2005-03-21
     */
    public static DataSourceMapping buildSampleDataSourceMapping()
    {
        // create two schema
        Schema fromSchema = buildSampleLocalSchema();
        System.out.println("Local Schema\n" + fromSchema);

        Schema toSchema = buildSampleRemoteSchema();
        System.out.println("Remote Schema\n" + toSchema);

        // create a schema mapping
        SchemaMapping mySchemaMapping = new SchemaMapping(fromSchema.getName(),
                toSchema.getName());

        NumericConnector doubleIt = new NumericConnector("F2C", "(x-32)*0.5555");

        mySchemaMapping.addMapping("temp", doubleIt, "temperature");
        mySchemaMapping.addMapping("name", SimpleConnector.EQU, "item");
        mySchemaMapping
                .addMapping("soldAt", SimpleConnector.EQU, "deliveredTo");
        mySchemaMapping
                .addMapping("produced_at", SimpleConnector.EQU, "madeIn");

        System.out.println("Schema Mapping\n" + mySchemaMapping);

        // create an ontoloy mapping
        InMemoryOntologyMapping myOntologyMapping = buildSampleOntologyMapping();

        // create the data source mapping
        DataSourceMapping myDSMapping = new DataSourceMapping(fromSchema
                .getName(), toSchema.getName(), "testDSMapping");
        myDSMapping.setSchemaMapping(mySchemaMapping);
        myDSMapping.addAVHMapping(myOntologyMapping);

        //System.out.println(myDSMapping);
        return myDSMapping;
    }

    public static ZExpression buildSampleINClause(boolean isIn)
    {
        String op = isIn ? "IN" : "NOT IN";

        ZExpression valueSet = new ZExpression(",");
        valueSet.addOperand(new ZConstantEx("Iowa", ZConstantEx.AVH));
        valueSet.addOperand(new ZConstantEx("Ames", ZConstantEx.AVH));
        valueSet.addOperand(new ZConstantEx("Des Moines", ZConstantEx.AVH));

        ZConstantEx column = new ZConstantEx("producedAt",
                ZConstantEx.COLUMNNAME);

        ZExpression clause = new ZExpression(op);
        clause.addOperand(column);
        clause.addOperand(valueSet);
        return clause;
    }

    /**
     * @return Map
     * @since 2005-03-20
     */
    protected static Map buildSampleLocalAttributeToAVHMapping()
    {
        //prepare sample AVH
        AVH avhLocation = buildSampleAVH_Location();

        // prepare the attribute ->AVH mapping
        Map attributeToAVH = new HashMap();
        attributeToAVH.put("producedAt", avhLocation);
        attributeToAVH.put("soldAt", avhLocation);

        System.out.println("Mapping:" + attributeToAVH + "\n");
        return attributeToAVH;
    }

    /**
     * @since 2005-03-22
     */
    public static ZQuery buildSampleLocalQuery()
    {
        ZExpression[] test = new ZExpression[4];
        test[0] = SQLQueryBuilder.buildAttributeValuePair("produced_at", "=",
                "produced_at", ZConstantEx.COLUMNNAME);
        test[1] = SQLQueryBuilder.buildAttributeValuePair("temp", ">=", "50",
                ZConstantEx.NUMBER);
        test[2] = SQLQueryBuilder.buildAttributeValuePair("item", "=", "iPod",
                ZConstantEx.STRING);
        test[3] = SQLQueryBuilder.buildAttributeValuePair("produced_at", ">",
                "Iowa", ZConstantEx.AVH);
        ZExpression t1 = new ZExpression("AND", test[0], test[1]);
        ZExpression t2 = new ZExpression("AND", test[2], test[3]);
        ZExpression where = new ZExpression("OR", t1, t2);

        ZQuery localQuery = new ZQuery();
        Vector select = new Vector();
        ZSelectItem s = new ZSelectItem("produced_at");
        select.add(s);
        localQuery.addSelect(select);

        Vector from = new Vector();
        ZFromItem f = new ZFromItem("test1");
        from.add(f);
        localQuery.addFrom(from);

        localQuery.addWhere(where);

        return localQuery;
    }

    protected static Map buildSampleRemoteAttributeToAVHMapping()
    {
        //prepare sample AVH
        AVH avhNickname = buildSampleAVH_Nickname();

        // prepare the attribute ->AVH mapping
        Map attributeToAVH = new HashMap();
        attributeToAVH.put("madeIn", avhNickname);
        attributeToAVH.put("deliveredTo", avhNickname);
        System.out.println("Mapping:" + attributeToAVH + "\n");
        return attributeToAVH;
    }
}
