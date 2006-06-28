/**
 * 
 */
package edu.iastate.anthill.indus.datasource.mapping;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.utils.Utility;
import edu.iastate.utils.io.FileUtils;
import edu.iastate.utils.lang.Serialization;
import edu.iastate.utils.lang.StopWatch;
import edu.iastate.utils.string.Zip;

/**
 * Test mappings
 * 
 * @author baojie
 * @since 2006-06-22
 */
public class MappingTest
{
    // 2006-06-22 Jie Bao
    public static void testOntologyMappingMemoryUse() throws IOException
    {
        InMemoryOntologyMapping mapping = new InMemoryOntologyMapping("ont1",
                "ont2");

        int n = 1000000;
        for (int i = 0; i < n; i++)
        {
            mapping.addMapping(i + "", SimpleConnector.INTO, i + "");
        }

        byte[] b = Serialization.saveToByteArray(mapping);
        System.out.println("Number of mapping rules: " + n);
        System.out.println("Serialization space requirement: " + b.length + "("
                + Utility.formatSize(b.length) + ")");

        b = Zip.encodeByte(b);
        System.out.println("Zipped: " + b.length + "("
                + Utility.formatSize(b.length) + ")");
        b = Zip.decodeByte(b);
        System.out.println("UnZipped: " + b.length + "("
                + Utility.formatSize(b.length) + ")");

    }

    public static void testInMemorySearch()
    {
        InMemoryOntologyMapping mapping = new InMemoryOntologyMapping("ont1",
                "ont2");

        int n = 10 * 100 * 1000;
        for (int i = 0; i < n; i++)
        {
            mapping.addMapping(i + "", SimpleConnector.INTO, i + "");
        }
        StopWatch w = new StopWatch();
        w.start();
        String s = mapping.findFirstMappedTo((n - 1) + "").toTerm;
        w.stop();
        System.out.println(s);
        System.out.println(w.print());
        mapping.clear();

    }

    // 2006-06-26 Jie Bao
    public static void testParsingSpeed() throws FileNotFoundException,
            SecurityException, IOException
    {
        DataSourceMapping m = new DataSourceMapping();

        InMemoryOntologyMapping mapping = new InMemoryOntologyMapping("ont1",
                "ont2");

        int n = 20000;
        System.out.println("mappings " + n);

        for (int i = 0; i < n; i++)
        {
            mapping.addMapping(i + "", SimpleConnector.INTO, i + "");
        }
        m.addAVHMapping(mapping);
        String xml = m.toXML();

        FileUtils.writeFile("d:\\mapping.xml", xml);

        // load
        DataSourceMapping m1 = new DataSourceMapping();

        StopWatch w = new StopWatch();
        w.start();
        m1.fromXML(xml);
        w.stop();
        System.out.println(w.print());
    }

    public static void testImportPlainText() throws SecurityException,
            IOException
    {
        DataSourceMapping m = new DataSourceMapping();

        InMemoryOntologyMapping mapping = new InMemoryOntologyMapping(null,
                null);
        mapping.fromPlainText(new FileReader("D:\\scop2ec.txt"));
        m.addAVHMapping(mapping);
        String xml = m.toXML();
        FileUtils.writeFile("d:\\scop2ec.xml", xml);
    }

    // 2006-06-27
    public static void testDBStorage() throws FileNotFoundException,
            SecurityException, IOException
    {
        DataSourceMapping m = new DataSourceMapping();
        String xml = FileUtils.readFile("d:\\scop2ec.xml");
        m.fromXML(xml);
        
        IndusBasis.indusSystemDB.connect(IndusConstants.dbURL);
        //InfoWriter.writeMapping(m);
        //InfoReader.readMapping(m.name);
        
        System.out.println("testDBStorage done");
    }
    
    // 2006-06-27 Jie Bao
    public static void testJListScaleability()
    {
        int n = 100000;
        Object obj[] = new Object[n];
        for (int i = 0 ; i < n ; i++)
        {
            obj[i] = Math.random();
        }
        StopWatch w = new StopWatch();
        w.start();
        JList lst = new JList(obj);
        w.stop();
        System.out.println(w.print());     
        
        // show it
        w.start();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        JScrollPane scr = new JScrollPane(lst);
        frame.getContentPane().add(scr);
        frame.setVisible(true);
        w.stop();
        System.out.println(w.print());     
        
    }

    public static void main(String[] args) throws IOException
    {
        //testOntologyMappingMemoryUse();
        //testInMemorySearch();
        //testParsingSpeed();
        //testImportPlainText();
        testDBStorage();
        //testJListScaleability();
    }

}
