package edu.iastate.anthill.indus.gui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.utils.Debug;

public abstract class TypePanelGUI extends IndusPane
{

    public TypePanelGUI()
    {
        super();
        //Debug.trace(this, "DataTypePanelGUI:DataTypePanelGUI");
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            Debug.trace(this, "haha , a bug");
            ex.printStackTrace();
        }

    }

    JSplitPane       jSplitPane1       = new JSplitPane();
    JPanel           leftPanel         = new JPanel();
    JPanel           rightPanel        = new JPanel();

    DefaultListModel model             = new DefaultListModel();
    public JList     listAllTypes      = new JList(model);
    JButton          btnNewType        = new JButton();
    JLabel           labelSelectedType = new JLabel();
    JPanel           treePanel         = new JPanel();
    BorderLayout     borderLayout1     = new BorderLayout();
    JPanel           buttonPanel       = new JPanel();
    JButton          btnSave           = new JButton();
    JScrollPane      jScrollPaneTree   = new JScrollPane();
    BorderLayout     borderLayout2     = new BorderLayout();
    FlowLayout       flowLayout1       = new FlowLayout();
    JButton          btnExportXML      = new JButton();
    JButton          btnImportText     = new JButton("Import(Text)");
    JButton          btnImportXML      = new JButton("Import(XML)");
    JButton          btnExportText     = new JButton();
    JButton          btnReload         = new JButton("Reload");

    void jbInit() throws Exception
    {
        //Debug.trace(this, "DataTypePanelGUI:jbInit");
        this.setLayout(new BorderLayout());

        // left panel
        leftPanel.setLayout(new BorderLayout());

        listAllTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listAllTypes.setCellRenderer(new TypeListCellRenderer());

        JScrollPane jp = new JScrollPane();
        jp.setBorder(BorderFactory.createTitledBorder("Registered types"));
        jp.getViewport().add(listAllTypes);

        btnSave.setText("Save Type");
        leftPanel.add(jp, BorderLayout.CENTER);

        // right panel
        rightPanel.setLayout(borderLayout1);

        // tree panel
        treePanel.setLayout(borderLayout2);
        treePanel.add(jScrollPaneTree, BorderLayout.CENTER);
        rightPanel.add(treePanel, BorderLayout.CENTER);

        // button
        btnNewType.setText("New Type");
        btnExportXML.setText("Export(XML)");
        btnExportText.setText("Export(Text)");

        buttonPanel.add(btnReload);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnNewType);
        buttonPanel.add(btnExportXML, null);
        buttonPanel.add(btnExportText, null);
        buttonPanel.add(btnImportXML, null);
        buttonPanel.add(btnImportText, null);
        buttonPanel.setLayout(flowLayout1);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        // the label
        labelSelectedType.setText("");
        rightPanel.add(labelSelectedType, BorderLayout.NORTH);

        // split panel
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.add(leftPanel, JSplitPane.LEFT);
        jSplitPane1.add(rightPanel, JSplitPane.RIGHT);
        jSplitPane1.setDividerLocation(0.3);
        this.add(jSplitPane1, BorderLayout.CENTER);
    }

    /**
     * Add icon to list based on super type
     * @author Jie Bao
     * @since 1.0 2004-10-15
     */
    class TypeListCellRenderer extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus)
        {
            Component retValue = super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            
            ImageIcon icon = (ImageIcon) typeIcon.get(value); 
            if(icon == null)
                icon = IndusConstants.iconDatatype;

            setIcon(icon);

            return retValue;
        }
    }

    HashMap<Object, ImageIcon> typeIcon = new HashMap<Object, ImageIcon>();
}
