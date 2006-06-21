package edu.iastate.anthill.indus.datasource.type;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.utils.io.FileUtils;
import edu.iastate.utils.lang.StopWatch;
import edu.iastate.utils.string.SimpleXMLParser;

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

    public static void main(String[] args)
    {
        //AVH avh = new AVH("Test", "ISA");
        //avh.treeAVT.buildSampleTree();
        //testToFromXML(avh);

        try
        {
            compareParsingSpeed();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
