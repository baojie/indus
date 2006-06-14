package edu.iastate.anthill.indus.iterator.go;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.sql.pgJDBCUtils;
import edu.iastate.utils.string.ParserUtils;

/**
 * @author Jie Bao
 * @since 1.0 2005-02-18
 */
public class GOIterator
    extends IndusDB
{
    public GOIterator()
    {
    }

    // parsing a OBO file
    // the OBO format is explained at http://www.geneontology.org/GO.format.shtml#oboflat
    /* example of OBO file:
     format-version: GO_1.0
     !any comment here
     typeref: relationship.types
     subsetdef: goslim "Generic GO Slim"
     version: $Revision: 1.1 $
     date: April 18th, 2003
     saved-by: jrichter
     remark: Example file

     [Term]
     id: GO:0003674
     name: molecular_function
     def: "The action characteristic of a gene product." [GO:curators]
     subset: goslim

     [Term]
     id: GO:0016209
     name: antioxidant activity
     is_a: GO:0003674
     def: "Inhibition of the reactions brought about by dioxygen or peroxides. \
     Usually the antioxidant is effective because it can itself be more easily \
     oxidized than the substance protected. The term is often applied to \
     components that can trap free radicals, thereby breaking the chain \
     reaction that normally leads to extensive biological damage." \
     [ISBN:0198506732]    */

    public void loadToDB()
    {
        String url = "http://www.geneontology.org/ontology/gene_ontology.obo";

        // that could be a large file, so we read it line by line
        BufferedReader in = IOUtils.openInputStream(url);
        String default_namespace = "gene_ontology";

        try
        {
            // parse it
            String str;
            // read header section

            pgJDBCUtils.updateDatabase(db, "DELETE FROM go_header"); //delete all old header information

            while ( (str = in.readLine()) != null)
            {
                // read until a blank line
                if (str.matches("\\s*"))
                {
                    break;
                }

                TagValuePair tagvalue = parseTagValuePair(str);

                if (tagvalue.tag.compareTo("default_namespace") == 0)
                {
                    default_namespace = tagvalue.value;
                }

                addHeadEntry(tagvalue.tag, tagvalue.value);
            }

            // read all other sections
            Vector strings = new Vector();
            while ( (str = in.readLine()) != null)
            {
                strings.add(str);
                // read until a blank line
                if (str.matches("\\s*"))
                {
                    // process
                    //System.out.println(strings);
                    String stanza = ( (String) strings.elementAt(0)).trim();
                    if (stanza.startsWith("[Term]"))
                    {
                        addTermEntry(strings, default_namespace);
                    }
                    else if (stanza.startsWith("[Typedef]"))
                    {
                        addTypedefEntry(strings);
                    }

                    // clear
                    strings.removeAllElements();
                    //break;
                }
            }

            // close the stream
            in.close();

            addGoRoot();
        }
        catch (IOException ex)
        {
        }

    }

    /**
     * Add a typdef item
     * eg:
     *    [Typedef]
     *    id: part_of
     *    name: part of
     *    is_transitive: true
     *
     * @param strings Vector
     * @return boolean
     * @since 2005-02-20
     */
    boolean addTypedefEntry(Vector strings)
    {
        String id = null, name = null, domain = "", range = "",
            is_cyclic = "false", is_transitive = "false",
            is_symmetric = "false";
        Iterator it = strings.iterator();
        while (it.hasNext())
        {
            String str = (String) it.next();
            TagValuePair tagvalue = parseTagValuePair(str);
            //System.out.println(tagvalue.tag);
            if (tagvalue.tag.compareTo("id") == 0)
            {
                //id: part_of
                id = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("name") == 0)
            {
                //name: part of
                name = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("is_transitive") == 0)
            {
                //is_transitive: true
                is_transitive = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("is_symmetric") == 0)
            {
                //is_transitive: true
                is_symmetric = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("is_cyclic") == 0)
            {
                //is_transitive: true
                is_cyclic = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("domain") == 0)
            {
                //is_transitive: true
                domain = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("range") == 0)
            {
                //is_transitive: true
                range = tagvalue.value;
            }
        }

        // table go_alt_id
        String tableName = "go_typedef";
        String[] fields = new String[]
            {
            "id", "name", "domain", "range", "is_cyclic", "is_transitive",
            "is_symmetric"};
        String[] values = new String[]
            {
            id, name, domain, range, is_cyclic, is_transitive, is_symmetric};
        pgJDBCUtils.insertDatabase(db, tableName, fields, values);

        return true;
    }

    /**
     * @since 2005-02-18
     * @param strings Vector
     */
    boolean addTermEntry(Vector strings, String default_namespace)
    {
        Iterator it = strings.iterator();

        String go_id = null, name = null, namespace = default_namespace,
            def = "", def_xref = "", comment = "", is_obsolete = "false";
        Vector alt_id = new Vector(), // of string(7)
            subset = new Vector(), // of string(50)
            synonym = new Vector(), // of TagValuePair
            related_synonym = new Vector(), // ditto
            exact_synonym = new Vector(), // ditto
            broad_synonym = new Vector(), // ditto
            narrow_synonym = new Vector(), // ditto
            xref_analog = new Vector(), //of string
            xref_unknown = new Vector(), // of string
            is_a = new Vector(), // of TagValuePair
            relationship = new Vector(), //of TagValuePair
            use_term = new Vector(); // of TagValuePair

        while (it.hasNext())
        {
            String str = (String) it.next();
            TagValuePair tagvalue = parseTagValuePair(str);
            //System.out.println(tagvalue.tag);
            if (tagvalue.tag.compareTo("id") == 0)
            {
                // eg: id: GO:0000041
                go_id = filterGO_ID(tagvalue.value);
            }
            else if (tagvalue.tag.compareTo("name") == 0)
            {
                //eg: name: transition metal ion transport
                name = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("alt_id") == 0)
            {
                // alt_id: GO:0006594
                alt_id.add(filterGO_ID(tagvalue.value));
            }
            else if (tagvalue.tag.compareTo("namespace") == 0)
            {
                //namespace: biological_process
                namespace = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("def") == 0)
            {
                //def: "Interacting selectively with transfer RNA." [GO:ai]
                TagValuePair t = parseXref(tagvalue.value);
                def = t.tag;
                def_xref = t.value;
            }
            else if (tagvalue.tag.compareTo("comment") == 0)
            {
                // comment: This term was made ...
                comment = tagvalue.value;
            }

            else if (tagvalue.tag.compareTo("subset") == 0)
            {
                // subset: gosubset_prok
                subset.add(tagvalue.value);
            }
            else if (tagvalue.tag.compareTo("synonym") == 0)
            {
                //  synonym: "The Bug" [VEH:391840]
                TagValuePair t = parseXref(tagvalue.value);
                synonym.add(t);

            }
            else if (tagvalue.tag.compareTo("related_synonym") == 0)
            {
                //related_synonym: "Type 1" []
                TagValuePair t = parseXref(tagvalue.value);
                related_synonym.add(t);

            }
            else if (tagvalue.tag.compareTo("exact_synonym") == 0)
            {
                //  exact_synonym: "VW Bug" [VW:0283, TPT:938VWB]
                TagValuePair t = parseXref(tagvalue.value);
                exact_synonym.add(t);
            }
            else if (tagvalue.tag.compareTo("broad_synonym") == 0)
            {
                //broad_synonym: "glutamine amidotransferase\:cyclase" []
                TagValuePair t = parseXref(tagvalue.value);
                broad_synonym.add(t);
            }
            else if (tagvalue.tag.compareTo("narrow_synonym") == 0)
            {
                //narrow_synonym: "TRAP complex" []
                TagValuePair t = parseXref(tagvalue.value);
                narrow_synonym.add(t);
            }
            else if (tagvalue.tag.compareTo("xref_analog") == 0)
            {
                //xref_analog: EC:3.1.3.21
                xref_analog.add(tagvalue.value);
            }
            else if (tagvalue.tag.compareTo("xref_unknown") == 0)
            {
                // xref_unknown: EC:1.14.13.78
                xref_unknown.add(tagvalue.value);
            }
            else if (tagvalue.tag.compareTo("is_a") == 0)
            {
                //is_a: GO:0006118 ! electron transport
                TagValuePair t = new TagValuePair("is_a",
                                                  filterGO_ID(tagvalue.value));
                //System.out.println(t.tag+":"+t.value);
                is_a.add(t);
            }
            else if (tagvalue.tag.compareTo("relationship") == 0)
            {
                // eg: relationship: part_of GO:0016236 ! macroautophagy
                String s[] = tagvalue.value.split("\\s");
                if (s.length == 2)
                {
                    relationship.add(new TagValuePair(s[0], filterGO_ID(s[1]))); // it includes two words!
                }
            }
            else if (tagvalue.tag.compareTo("is_obsolete") == 0)
            {
                // is_obsolete: true
                is_obsolete = tagvalue.value;
            }
            else if (tagvalue.tag.compareTo("use_term") == 0)
            {
                TagValuePair t = new TagValuePair("use_term",
                                                  filterGO_ID(tagvalue.value));
                use_term.add(t);
            }
            else // unsupportted tags
            {

            }

        } // while
        if (go_id == null || name == null)
        {
            return false;
        }
        else
        {
            // insert information into database
            System.out.println("GO:" + go_id);

            // table go_term
            String tableName = "go_term";
            String fields[] = new String[]
                {
                "go_id", "name", "namespace", "is_obsolete", "def", "comment",
                "def_xref"};
            String values[] = new String[]
                {
                go_id, name, namespace, is_obsolete, def, comment, def_xref};
            pgJDBCUtils.insertDatabase(db, tableName, fields, values);

            // table go_alt_id
            tableName = "go_alt_id";
            fields = new String[]
                {
                "go_id", "alt_id"};
            for (int i = 0; i < alt_id.size(); i++)
            {
                values = new String[]
                    {
                    go_id, (String) alt_id.elementAt(i)};
                pgJDBCUtils.insertDatabase(db, tableName, fields, values);
            }

            // table go_subset
            tableName = "go_subset";
            fields = new String[]
                {
                "go_id", "subset"};
            for (int i = 0; i < subset.size(); i++)
            {
                values = new String[]
                    {
                    go_id, (String) subset.elementAt(i)};
                pgJDBCUtils.insertDatabase(db, tableName, fields, values);
            }

            // go_synonym
            addSynonym(go_id, "general", synonym);
            addSynonym(go_id, "related", related_synonym);
            addSynonym(go_id, "exact", exact_synonym);
            addSynonym(go_id, "broad", broad_synonym);
            addSynonym(go_id, "narrow", narrow_synonym);

            //  go_xref_analog
            tableName = "go_xref_analog";
            fields = new String[]
                {
                "go_id", "xref"};
            for (int i = 0; i < xref_analog.size(); i++)
            {
                values = new String[]
                    {
                    go_id, (String) xref_analog.elementAt(i)};
                pgJDBCUtils.insertDatabase(db, tableName, fields, values);
            }

            // go_xref_unknow
            tableName = " go_xref_unknow";
            fields = new String[]
                {
                "go_id", "xref"};
            for (int i = 0; i < xref_unknown.size(); i++)
            {
                values = new String[]
                    {
                    go_id, (String) xref_unknown.elementAt(i)};
                pgJDBCUtils.insertDatabase(db, tableName, fields, values);
            }

            // go_relationship
            addRelationship(go_id, is_a);
            addRelationship(go_id, relationship);
            addRelationship(go_id, use_term);

            tableName = "";
            fields = new String[]
                {};
            values = new String[]
                {};

            return true;
        }
    }

    /**
     * @param go_id String
     * @param source Vector
     */
    void addRelationship(String go_id, Vector source)
    {
        String tableName = "go_relationship";
        String[] fields = new String[]
            {
            "go_id1", "relation", "go_id2"};
        String[] values;
        for (int i = 0; i < source.size(); i++)
        {
            TagValuePair t = (TagValuePair) source.elementAt(i);
            values = new String[]
                {
                go_id, t.tag, t.value};

            //System.out.println(values[0] + "," + values[1] + "," + values[2]);

            pgJDBCUtils.insertDatabase(db, tableName, fields, values);
        }
    }

    /**
     * @param go_id String
     * @param type String   "general",  "related", "exact","broad",  "narrow"
     * @param source Vector
     */
    void addSynonym(String go_id, String type, Vector source)
    {
        String tableName = "go_synonym";
        String fields[] = new String[]
            {
            "go_id", "syn_type", "synonym", "synonym_xref"};
        for (int i = 0; i < source.size(); i++)
        {
            TagValuePair t = (TagValuePair) source.elementAt(i);
            String values[] = new String[]
                {
                go_id, type, t.tag, t.value};
            pgJDBCUtils.insertDatabase(db, tableName, fields, values);
        }
    }

    TagValuePair parseXref(String xref)
    {
        //def: "Interacting selectively with transfer RNA." [GO:ai]
        String def = ParserUtils.findFirst("\\\".*\\\"", xref); // find words in "  ...  "
        if (def != null)
        {
            def = def.substring(1, def.length() - 1);
        }
        //System.out.println("          " + def);
        String def_xref = ParserUtils.findFirst("\\[.*?\\]", xref); // find words in [ ..... ]
        if (def_xref != null)
        {
            def_xref = def_xref.substring(1, def_xref.length() - 1);
        }
        //System.out.println("          " + def_xref);
        return new TagValuePair(def, def_xref);

    }

    /**
     *
     * @param id String eg: GO:0012509
     * @return String eg:  0012509
     */
    String filterGO_ID(String id)
    {
        return id.replaceAll("GO:", "");
    }

    void addHeadEntry(String tag, String details)
    {
        // add it into the database
        String sss =
            "INSERT INTO go_header (tag, details)" +
            " VALUES (" + pgJDBCUtils.toDBString(tag) + "," +
            pgJDBCUtils.toDBString(details) + ")";
        System.out.println("     " + sss);
        pgJDBCUtils.updateDatabase(db, sss);
    }

    public static void test()
    {
        GOIterator loader = new GOIterator();

        loader.connect();
        loader.clearAllData();
        loader.loadToDB();
        loader.disconnect();
    }

    TagValuePair parseTagValuePair(String str)
    {
        // remove all words after '!' . they are comments
        int comments = str.indexOf("!");
        if (comments > 0)
        {
            str = str.substring(0, comments);
        }

        // replace all "'", since it will be used as database term limitier
        str = str.replaceAll("'", "\"");

        // split by ':'
        int limiter = str.indexOf(":");
        if (limiter > 0)
        {
            String tag = str.substring(0, limiter).trim();
            String value = str.substring(limiter + 1).trim();
            //System.out.println(str + " = " + tag + " + " + value);
            return new TagValuePair(tag, value);
        }
        else
        {
            return new TagValuePair(str, "");
        }

    }

    class TagValuePair
    {
        String tag;
        String value;
        public TagValuePair(String tag, String value)
        {
            this.tag = tag;
            this.value = value;
        }

    }

    void clearAllData()
    {
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_alt_id");
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_header");
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_relationship");
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_subset");
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_synonym");
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_term");
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_typedef");
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_xref_analog");
        pgJDBCUtils.updateDatabase(db, "DELETE FROM go_xref_unknow");
    }

    /**
     * Gene Ontology has no single root, but three nodes for biological_process,
     * cellular_component, molecular_function . We add a new node 0000000 as the
     * root for all of them
     *
     * @author Jie Bao
     * @since 2005-03-31
     */
    void addGoRoot()
    {
        String sql = "INSERT INTO go_relationship (go_id1, relation, go_id2) VALUES ('0008150', 'is_a', '0000000');";
        pgJDBCUtils.updateDatabase(db, sql); // biological_process

        sql = "INSERT INTO go_relationship (go_id1, relation, go_id2) VALUES ('0005575', 'is_a', '0000000');";
        pgJDBCUtils.updateDatabase(db, sql); // cellular_component

        sql = "INSERT INTO go_relationship (go_id1, relation, go_id2) VALUES ('0003674', 'is_a', '0000000');";
        pgJDBCUtils.updateDatabase(db, sql); // molecular_function

        sql = "INSERT INTO go_term (go_id, name) VALUES ('0000000', 'gene_ontology');";
        pgJDBCUtils.updateDatabase(db, sql); // go root
    }

}
