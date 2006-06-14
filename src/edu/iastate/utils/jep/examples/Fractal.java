/*****************************************************************************
 JEP - Java Math Expression Parser 2.24
      December 30 2002
      (c) Copyright 2002, Nathan Funk
      See LICENSE.txt for license information.

 *****************************************************************************/

/*
 <applet code="org.nfunk.jepexamples.Fractal" width=300 height=320>
 <param name=initialExpression value="z*z+c">
 </applet>
 */
package edu.iastate.utils.jep.examples;

import java.applet.Applet;

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;

public class Fractal
    extends Applet implements ActionListener
{

    private TextField exprField, itField;
    private Button button, button2;
    private ComplexCanvas complexCanvas;

    /** Initializes the applet Fractal */
    public void init()
    {
        initComponents();
    }

    private void initComponents()
    {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;

        // Expression field
        String expr = getParameter("initialExpression");
        if (expr != null)
        {
            exprField = new TextField(expr);
        }
        else
        {
            exprField = new TextField("");
        }

        exprField.setBackground(java.awt.Color.white);
        exprField.setName("exprField");
        exprField.setFont(new Font("Dialog", 0, 11));
        exprField.setForeground(Color.black);
        exprField.addTextListener(new java.awt.event.TextListener()
        {
            public void textValueChanged(java.awt.event.TextEvent evt)
            {
                exprFieldTextValueChanged(evt);
            }
        }
        );

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        gridbag.setConstraints(exprField, c);
        add(exprField);

        // RENDER BUTTON
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.2;
        button = new Button("Render");
        gridbag.setConstraints(button, c);
        add(button);
        button.addActionListener(this);

        // Iterations field
        itField = new TextField("20");
        itField.addTextListener(new java.awt.event.TextListener()
        {
            public void textValueChanged(java.awt.event.TextEvent evt)
            {
                itFieldTextValueChanged(evt);
            }
        }
        );

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        gridbag.setConstraints(itField, c);
        add(itField);

        // CANVAS
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weighty = 1;
//		button2 = new Button("test");

        complexCanvas = new ComplexCanvas(expr, exprField);
        gridbag.setConstraints(complexCanvas, c);
        add(complexCanvas);
    }

    private void exprFieldTextValueChanged(java.awt.event.TextEvent evt)
    {
        String newExpressionString = exprField.getText();
        complexCanvas.setExpressionString(newExpressionString);
        //complexCanvas.repaint();
    }

    private void itFieldTextValueChanged(java.awt.event.TextEvent evt)
    {
        Integer newIterationsValue = new Integer(itField.getText());
        complexCanvas.setIterations(newIterationsValue.intValue());
        //complexCanvas.repaint();
    }

    public void actionPerformed(ActionEvent ae)
    {
        String str = ae.getActionCommand();
        if (str.equals("Render"))
        {
            String newExpressionString = exprField.getText();
            complexCanvas.setExpressionString(newExpressionString);
            complexCanvas.repaint();
        }
    }
}
