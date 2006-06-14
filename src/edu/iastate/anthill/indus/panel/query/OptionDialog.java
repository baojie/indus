package edu.iastate.anthill.indus.panel.query;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import java.awt.*;

/**
 * Class for option dialog on first pane.
 *
 * @author	Jan Seda
 * @version	0.3.6
 */
public class OptionDialog
    extends JDialog implements ActionListener
{
    /** Action command for ok button. */
    private static final String OKBT_CMD = "0x1O2";
    /** Action command for cancel button. */
    private static final String CABT_CMD = "0x1C3";

    /** Pane with checkboxes. */
    JPanel paneBox = new JPanel();
    /** Checkbox for tables. */
    JCheckBox checkTab;
    /** Checkbox for views. */
    JCheckBox checkVi;
    /** Checkbox for views. */
    JCheckBox checkSys;
    /** Checkbox for synonyms. */
    JCheckBox checkSyn;
    /** Checkbox for sorting item or not. */
    JCheckBox sort;
    /** Default value for insets. */
    private int _insets = 10;
    /** Blank pane for layout. */
    JPanel blankcol = new JPanel();
    /** Ok button. */
    JButton butOk;
    /** Cancel button. */
    JButton butCanc;
    /** Array holding selected types of tables. */
    String[] selTabs;

    SelectFromPane parent;

    /**
     * Class constructor.
     *
     */
    public OptionDialog(SelectFromPane parent)
    {
        super((Frame)null, "Table options", true);
        this.parent = parent;

        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints1;

        // Font font = _builder.getFont();
        // Font newFont = new Font(font.getFontName(), font.getStyle(), font.getSize()-1);

        checkTab = new JCheckBox("Tables", true);
        // checkTab.setFont(newFont);
        checkVi = new JCheckBox("Views", true);
        // checkVi.setFont(newFont);
        checkSys = new JCheckBox("System tables", false);
        // checkSys.setFont(newFont);
        checkSyn = new JCheckBox("Synonyms", true);
        // checkSyn.setFont(newFont);

        sort = new JCheckBox(
            "List Tables and Columns in alphabetical order", false);
        // sort.setFont(newFont);

        // create pane with checkboxes
        paneBox.setLayout(new GridLayout(4, 1));
        paneBox.setBorder(BorderFactory.createLineBorder(Color.gray));
        paneBox.add(checkTab);
        paneBox.add(checkVi);
        paneBox.add(checkSys);
        paneBox.add(checkSyn);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        // gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridwidth = 1;
        gridBagConstraints1.gridheight = 2;
        gridBagConstraints1.insets = new java.awt.Insets(0, _insets, 0,
            _insets);
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(paneBox, gridBagConstraints1);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridwidth = 1;
        gridBagConstraints1.gridheight = 2;
        gridBagConstraints1.insets = new java.awt.Insets(_insets, _insets,
            _insets, _insets);
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(blankcol, gridBagConstraints1);

        butOk = new JButton("OK");
        butOk.addActionListener(this);
        butOk.setActionCommand(this.OKBT_CMD);
        // butOk.setFont(font);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 0;
        // gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridwidth = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, _insets,
            _insets);
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(butOk, gridBagConstraints1);

        butCanc = new JButton("Cancel");
        butCanc.addActionListener(this);
        butCanc.setActionCommand(this.CABT_CMD);
        // butCanc.setFont(font);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 1;
        // gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridwidth = 1;
        // gridBagConstraints1.insets = new java.awt.Insets (0, 0, _insets, 0);
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(butCanc, gridBagConstraints1);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, _insets, 0);
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(sort, gridBagConstraints1);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(this.CABT_CMD))
        {
            this.setVisible(false);
        }
        else if (e.getActionCommand().equals(this.OKBT_CMD))
        {
            // initialize string array
            selTabs = new String[4];
            if (checkTab.isSelected())
            {
                selTabs[0] = new String("TABLE");
            }
            if (checkVi.isSelected())
            {
                selTabs[1] = new String("VIEW");
            }
            if (checkSyn.isSelected())
            {
                selTabs[2] = new String("SYNONYM");
            }
            if (checkSys.isSelected())
            {
                selTabs[3] = new String("SYSTEM TABLE");
            }

            // set array to main class variable
            parent.tabTypes = selTabs;
            // update tree with a new values
            parent.createTree(parent._builder._conn, true);

            this.setVisible(false);
        }
    }
}
