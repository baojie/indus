package edu.iastate.anthill.indus.gui.panel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.iastate.utils.gui.GUIUtils;

public class ViewAtomDateSource
    extends JPanel
{
    public boolean deleted = false;

    JComboBox jComboBox1 = new JComboBox();
    GridLayout gridLayout1 = new GridLayout();
    JComboBox jComboBox2 = new JComboBox();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JButton btnDelete = new JButton();

    ViewPanelGUI parent;

    public ViewAtomDateSource(ViewPanelGUI parent, Object availableDS[], Object availableMapping[])
    {
        try
        {
            this.parent = parent;
            jbInit();
            GUIUtils.updateComboBox(jComboBox1, availableDS);
            if (jComboBox1.getItemCount() > 0)
            {
                jComboBox1.setSelectedIndex(0);
            }

            GUIUtils.updateComboBox(jComboBox2, availableMapping);
            if (jComboBox2.getItemCount() > 0)
            {
                jComboBox2.setSelectedIndex(0);
            }
            //jComboBox2.addItem("Add new mapping...");
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        this.setLayout(gridLayout1);
        jLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel1.setText("Data Source");
        jLabel2.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel2.setText("Mapping ");
        btnDelete.setText("Delete");
        btnDelete.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onDelete(e);
            }
        });
        this.add(jLabel1);
        this.add(jComboBox1, null);
        this.add(jLabel2);
        this.add(jComboBox2);
        this.add(btnDelete);
    }

    public void onDelete(ActionEvent e)
    {
        this.deleted = true;
        this.setEnabled(false);
        this.setVisible(false);
        jComboBox1.setEnabled(false);
        jComboBox2.setEnabled(false);
        btnDelete.setEnabled(false);
        parent.modified = true;
    }

    public String getDS()
    {
        return (String) jComboBox1.getSelectedItem();
    }

    public String getMapping()
    {
        return (String) jComboBox2.getSelectedItem();
    }

    public void setDS(String newDS)
    {
        jComboBox1.setSelectedItem(newDS);
    }

    public void setMapping(String newMapping)
    {
        jComboBox2.setSelectedItem(newMapping);
    }

}
