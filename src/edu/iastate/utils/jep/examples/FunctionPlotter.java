/*
 HTML code for applet:
 <applet code="org.nfunk.jepexamples.FunctionPlotter" width=300 height=320>
 <param name=initialExpression value="100 sin(x/3) cos(x/70)">
 </applet>
 */

package edu.iastate.utils.jep.examples;

import java.applet.Applet;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This applet is a demonstration of the possible applications of the JEP
 * mathematical expression parser.<p>
 * The FunctionPlotter class arranges the text field and GraphCanvas classes
 * and requests a repainting of the graph when the expression in the text
 * field changes. All plotting (and interaction with the JEP API) is preformed
 * in GraphCanvas class.
 */
public class FunctionPlotter
    extends Applet
{
    public FunctionPlotter()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /** The expression field */
    private java.awt.TextField exprField;

    /** The canvas for plotting the graph */
    private GraphCanvas graphCanvas;

    /**
     * Initializes the applet FunctionPlotter
     */
    public void init()
    {
        initComponents();
    }

    /**
     * Sets the layout of the applet window to BorderLayout, creates all
     * the components and associates them with event listeners if neccessary.
     */
    private void initComponents()
    {
        setLayout(new BorderLayout());
        setBackground(java.awt.Color.white);

        // get the initial expression from the parameters
        String expr = "100 sin(x/3) cos(x/70)"; //getParameter("initialExpression");

        // write the expression into the text field
        if (expr != null)
        {
            exprField = new java.awt.TextField(expr);
        }
        else
        {
            exprField = new java.awt.TextField("");
        }

        // adjust various settings for the expression field
        exprField.setBackground(java.awt.Color.white);
        exprField.setName("exprField");
        exprField.setFont(new java.awt.Font("Dialog", 0, 12));
        exprField.setForeground(java.awt.Color.black);
        exprField.addTextListener(new java.awt.event.TextListener()
        {
            public void textValueChanged(java.awt.event.TextEvent evt)
            {
                exprFieldTextValueChanged(evt);
            }
        }
        );

        add("North", exprField);

        // create the graph canvas and add it
        graphCanvas = new GraphCanvas(expr, exprField);
        add("Center", graphCanvas);
    }

    /**
     * Repaints the graphCanvas whenever the text in the expression field
     * changes.
     */
    private void exprFieldTextValueChanged(java.awt.event.TextEvent evt)
    {
        String newExpressionString = exprField.getText();
        graphCanvas.setExpressionString(newExpressionString);
        graphCanvas.repaint();
    }

    /**
     * This method is called if the applet is run as an standalone
     * program. It creates a frame for the applet and adds the applet
     * to that frame.
     */
    public static void main(String args[])
    {
        FunctionPlotter a = new FunctionPlotter();
        a.init();
        a.start();

        Frame f = new Frame("Evaluator");
        f.add("Center", a);
        f.setSize(400, 200);
        f.addWindowListener(
            new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        }
        );

        f.show();
    }

    private void jbInit() throws Exception
    {
    }

}
