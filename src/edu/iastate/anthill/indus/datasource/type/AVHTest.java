package edu.iastate.anthill.indus.datasource.type;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.iastate.utils.io.FileUtils;
import edu.iastate.utils.lang.Serialization;
import edu.iastate.utils.lang.StopWatch;
import edu.iastate.utils.string.SimpleXMLParser;
import edu.iastate.utils.string.Zip;

/**
 * @author Jie Bao
 * @since 2005-04-01
 */
public class AVHTest
{
    /**
     * test AVH.toXML() and AVH.fromXML()
     * @author baojie
     * @since 2005-04-01
     * @param avh
     */
    private static void testToFromXML(AVH avh)
    {
        String tree = avh.treeAVT.toString();
        System.out.println(tree);
        //FileUtils.writeFile("D:\\ont.txt", tree) ;

        String xml = avh.toXML();
        //System.out.println(xml);
        //FileUtils.writeFile("D:\\ont.xml", xml) ;
        System.out.println(SimpleXMLParser.printXMLSkeleton(xml));
        //IndusBasis.showXML(xml);

        AVH avh1 = new AVH();
        avh1.fromXML(xml);
        //IndusBasis.showXML(avh1.toXML());
    }

    /**
     * test AVH.toText() and AVH.fromText()
     * @author baojie
     * @since 2006-06-20
     * @param avh
     */
    private static void testToFromText(AVH avh)
    {
        System.out.println("1. Test AVH.toText()");

        String text = avh.toText();
        System.out.println(text);

        System.out.println("2. Test AVH.fromText()");
        AVH avh2 = new AVH();
        avh2.fromText(text);
        String text2 = avh2.toText();
        System.out.println(text2);

        //System.out.println(xml);
        //FileUtils.writeFile("D:\\ont.xml", xml) ;
        //System.out.println(SimpleXMLParser.printXMLSkeleton(xml));
        //IndusBasis.showXML(xml);
    }

    /**
     * Compare parsing speed in two storage format: plain text and XML 
       Plain text is much much faster than XML!!!
     */
    private static void compareParsingSpeed() throws FileNotFoundException,
            SecurityException, IOException
    {
        // test xml parsing
        String xmlFile = "D:\\test.xml";

        StopWatch w = new StopWatch();
        w.start();
        String xml = FileUtils.readFile(xmlFile);
        w.stop();
        System.out.println("XML reading time: " + w.print());

        w.start();
        AVH avh = new AVH();
        avh.fromXML(xml);
        w.stop();
        System.out.println("XML parsing time: " + w.print());
        System.out.println("Tree Size " + avh.getSize());

        // test plain text parsing
        String textFile = "D:\\test.txt";

        String text = avh.toText();
        FileUtils.writeFile(textFile, text);

        w.start();
        text = FileUtils.readFile(textFile);
        w.stop();
        System.out.println("Text reading time: " + w.print());

        w.start();
        avh = new AVH();
        avh.fromText(text);
        w.stop();
        System.out.println("Text parsing time: " + w.print());
        System.out.println("Tree Size " + avh.getSize());
        System.out.println("End of test");
    }
    
    /**
     * Compare storage size between zipped plain text and zipped binary
     *       conclusion: zipped plain text is better
     *  
     * Test sample: scop ontology (83k terms) 
     * Plain text        6,554,321 / 3,682,797 [shorthand mode]
       Zipped plain text 1,084,605 / 1,008,740 [shorthand mode] (smallest)
       Binary            7,155,838
       Zipped binary     1,313,176
       
       @author Jie Bao
       @since 2006-06-26
     */    
    public static void compareStroageSize() throws FileNotFoundException, SecurityException, IOException
    {
        String file = "D:\\test.txt";
        String text = FileUtils.readFile(file);
        
        AVH avh = new AVH();
        avh.fromText(text);
        
//        text = avh.toText();
//        FileUtils.writeFile(file, text);
//        text = FileUtils.readFile(file);
        
        
        System.out.println("Plain text "+ text.length());
        byte[] zippedPlainText = Zip.encodeByte(text);
        System.out.println("Zipped plain text "+ zippedPlainText.length);
        
        byte[] binary = Serialization.saveToByteArray(avh);
        System.out.println("Binary "+ binary.length);
        
        byte[] zippedBinary = Zip.encodeByte(binary);
        System.out.println("Zipped binary "+ zippedBinary.length);
        System.out.println("End of test");
    }

    public static void main(String[] args)
    {
        //AVH avh = new AVH("Test", "ISA");
        //avh.treeAVT.buildSampleTree();
        //testToFromXML(avh);

        try
        {
            //compareParsingSpeed();
            compareStroageSize();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
