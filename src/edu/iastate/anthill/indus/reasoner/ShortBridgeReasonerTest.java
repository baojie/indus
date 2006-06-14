package edu.iastate.anthill.indus.reasoner;

import javax.swing.JFrame;

import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import javax.swing.*;

/**
 * @author Jie Bao
 * @since 2005-04-08
 */
public class ShortBridgeReasonerTest
{

    public static void main(String[] args)
    {
        testDistributedReasoner();
    }

    static JFrame frame = new JFrame();
    static JTabbedPane tab = new JTabbedPane();

    private static void prepareFrame()
    {
        frame.getContentPane().add(tab);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void showReasoner(String title, ShortBridgeReasoner r)
    {
         tab.add(title,new ReasonerPanel(r));
         tab.repaint();
    }

    private static ShortBridgeReasoner testDistributedReasoner()
    {
        prepareFrame();
        ShortBridgeReasoner r1 = buildSampleOntology1();
        r1.buildReasoner();
        showReasoner("Ontology 1",r1);
        ShortBridgeReasoner r2 = buildSampleOntology2();
        r2.buildReasoner();
        showReasoner("Ontology 2",r2);
        ShortBridgeReasoner r3 = buildSampleMapping();
        r3.buildReasoner();
        showReasoner("Mapping",r3);

        DistributedReasoner r = new DistributedReasoner();
        r.mergeReasoner(r1);
        r.mergeReasoner(r2);
        r.mergeReasoner(r3);
        showReasoner("Built by Distributed Reasoner",r.getReasoner());

        frame.show();

        return r.getReasoner();
    }

    // 2005-04-12
    private static ShortBridgeReasoner testCloneReasoner()
    {
        ShortBridgeReasoner reasoner = buildSampleOntology2();
        reasoner.buildReasoner();
        ShortBridgeReasoner copied = (ShortBridgeReasoner) reasoner.clone();
        return copied;
    }

    /**
     * build an ontology with two loops, two root
     * @return ShortBridgeReasoner
     */
    private static ShortBridgeReasoner buildSampleOntology1()
    {
        ShortBridgeReasoner reasoner = new ShortBridgeReasoner();

        reasoner.addRule(new ShortBridgeRule("A1", SimpleConnector.ONTO, "D1"));
        reasoner.addRule(new ShortBridgeRule("A1", SimpleConnector.ONTO, "B1"));
        reasoner.addRule(new ShortBridgeRule("B1", SimpleConnector.ONTO, "C1"));
        reasoner.addRule(new ShortBridgeRule("C1", SimpleConnector.ONTO, "A1"));
        reasoner.addRule(new ShortBridgeRule("D1", SimpleConnector.ONTO, "C1"));
        reasoner.addRule(new ShortBridgeRule("D1", SimpleConnector.ONTO, "F1"));
        reasoner.addRule(new ShortBridgeRule("C1", SimpleConnector.ONTO, "E1"));
        reasoner.addRule(new ShortBridgeRule("G1", SimpleConnector.ONTO, "C1"));
        reasoner.addRule(new ShortBridgeRule("H1", SimpleConnector.ONTO, "C1"));
        return reasoner;
    }

    // an inconsistent ontology with both EQU/UNEQU and COMP/INCOMP contradiction
    // 2005-04-08
    private static ShortBridgeReasoner buildSampleOntology2()
    {
        ShortBridgeReasoner reasoner = new ShortBridgeReasoner();

        reasoner.addRule(new ShortBridgeRule("A2", SimpleConnector.ONTO, "B2"));
        reasoner.addRule(new ShortBridgeRule("C2", SimpleConnector.INTO, "B2"));
        reasoner.addRule(new ShortBridgeRule("C2", SimpleConnector.ONTO, "D2"));
        reasoner.addRule(new ShortBridgeRule("D2", SimpleConnector.ONTO, "A2"));
        reasoner.addRule(new ShortBridgeRule("E2", SimpleConnector.INTO, "C2"));
        reasoner.addRule(new ShortBridgeRule("H2", SimpleConnector.INTO, "A2"));
        reasoner.addRule(new ShortBridgeRule("C2", SimpleConnector.INCOMP, "H2"));
        reasoner.addRule(new ShortBridgeRule("B2", SimpleConnector.UNEQU, "D2"));
        return reasoner;
    }

    private static ShortBridgeReasoner buildSampleMapping()
    {
        ShortBridgeReasoner reasoner = new ShortBridgeReasoner();

        reasoner.addRule(new ShortBridgeRule("A1", SimpleConnector.EQU, "A2"));

        return reasoner;
    }

}
