package edu.iastate.anthill.indus.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import edu.iastate.utils.jep.examples.GraphCanvas;

import edu.iastate.anthill.indus.datasource.mapping.NumericConnector;

/**
 * @author Jie Bao
 * @since 1.0 2004-10-16
 */
public class MappingRuleDialog
    extends JDialog
{
    JPanel panel1 = new JPanel();

    public TextField exprField = new TextField("2x");
    public String expr = null;
    public String exprname = null;

    private GraphCanvas graphCanvas;

    JPanel jPanel1 = new JPanel();
    JButton Cancel = new JButton();
    JButton OK = new JButton();
    JPanel jPanel2 = new JPanel();
    JLabel jLabel1 = new JLabel();

    public int action = JOptionPane.CANCEL_OPTION;
    JLabel jLabel2 = new JLabel();
    public TextField nameField = new TextField();
    GridLayout gridLayout1 = new GridLayout();

    private MappingRuleDialog(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        try
        {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public MappingRuleDialog(NumericConnector t)
    {
        this(new Frame(), "Expression Dialog", false);
        if (t != null)
        {
            expr = t.expression;
            exprname = t.name;
            exprField.setText(expr);
            nameField.setText(exprname);
        }
    }

    private void jbInit() throws Exception
    {
        panel1.setLayout(new BorderLayout());
        exprField.setText(exprField.getText());
        exprField.addTextListener(new TextListener()
        {
            public void textValueChanged(TextEvent evt)
            {
                exprFieldTextValueChanged(evt);
            }
        });
        Cancel.setText("Cancel");
        Cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Cancel_actionPerformed(e);
            }
        });
        OK.setText("OK");
        OK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                OK_actionPerformed(e);
            }
        });

        // create the graph canvas and add it
        graphCanvas = new GraphCanvas(exprField.getText(), exprField);
        jPanel2.setLayout(gridLayout1);
        jLabel1.setText("Expression: y = ");
        jLabel2.setText("Expression Name");
        nameField.setText("expr1");
        nameField.addTextListener(new TextListener()
        {
            public void textValueChanged(TextEvent evt)
            {
                nameFieldTextValueChanged(evt);
            }
        });

        panel1.add(graphCanvas, BorderLayout.CENTER);
        this.getContentPane().add(panel1, BorderLayout.CENTER);
        this.getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(OK);
        jPanel1.add(Cancel);
        panel1.add(jPanel2, java.awt.BorderLayout.NORTH);
        jPanel2.add(jLabel1, null);
        jPanel2.add(exprField, null);
        jPanel2.add(jLabel2);
        jPanel2.add(nameField);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.setModal(true);

    }

    boolean valid1 = true;
    boolean valid2 = true;

    /**
     * Repaints the graphCanvas whenever the text in the expression field
     * changes.
     */
    private void exprFieldTextValueChanged(java.awt.event.TextEvent evt)
    {
        String newExpressionString = exprField.getText();
        valid1 = graphCanvas.setExpressionString(newExpressionString);
        graphCanvas.repaint();
    }

    private void nameFieldTextValueChanged(java.awt.event.TextEvent evt)
    {
        String newNameString = nameField.getText();

        valid2 = false;

        valid2 = (newNameString != null && newNameString.length() >= 0 &&
                  newNameString.matches("[\\w\\-._]+"));

        nameField.setForeground(valid2 ? Color.black : Color.red);
    }

    public static void main(String args[])
    {
        MappingRuleDialog dlg = new MappingRuleDialog(null);
        dlg.setSize(600, 300);
        dlg.show();
    }

    public void OK_actionPerformed(ActionEvent e)
    {
        if (valid1 && valid2)
        {
            action = JOptionPane.YES_OPTION;
            expr = exprField.getText();
            exprname = nameField.getText();
            this.dispose();
        }
        else
        {
            JOptionPane.showMessageDialog(this,
                                          "Expression or name is illegal!");
        }

    }

    public void Cancel_actionPerformed(ActionEvent e)
    {
        expr = null;
        this.dispose();
    }
}
