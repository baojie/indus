package edu.iastate.anthill.indus.datasource.type;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.iastate.anthill.indus.IndusBasis;

import edu.iastate.utils.Debug;
import edu.iastate.utils.io.FileUtils;
import edu.iastate.utils.string.SimpleXMLParser;

/**
 * @author Jie Bao
 * @since 2005-04-01
 */
public class AVHTest
{
    public static void main(String[] args) throws FileNotFoundException, SecurityException, IOException
    {
        AVH avh = new AVH("Test", "ISA");
        //avh.treeAVT.buildSampleTree();
        avh.treeAVT.fromFile("D:\\all.txt");
        
        String tree = avh.treeAVT.toString();
        System.out.println(tree);
        FileUtils.writeFile("D:\\ont.txt", tree) ;

        String xml = avh.toXML();
        System.out.println(xml);
        FileUtils.writeFile("D:\\ont.xml", xml) ;
        //System.out.println(SimpleXMLParser.printXMLSkeleton(xml));
        //IndusBasis.showXML(xml);
//
//        AVH avh1 = new AVH();
//        avh1.fromXML(xml);
//        IndusBasis.showXML(avh1.toXML());
//        Debug.pause();
    }
}
