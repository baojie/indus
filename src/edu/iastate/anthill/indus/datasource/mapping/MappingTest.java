/**
 * 
 */
package edu.iastate.anthill.indus.datasource.mapping;

import java.io.IOException;

import edu.iastate.utils.Utility;
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

            int n = 10*100*1000;
            for (int i = 0; i < n; i++)
            {
                mapping.addMapping(i + "", SimpleConnector.INTO, i + "");
            }
            StopWatch w = new StopWatch();
            w.start();
            String s = mapping.findFirstMappedTo((n - 1) + "");
            w.stop();
            System.out.println(s);
            System.out.println(w.print());
            mapping.clear();
        

    }

    public static void main(String[] args) throws IOException
    {
        testOntologyMappingMemoryUse();
        //testInMemorySearch();
    }

}
