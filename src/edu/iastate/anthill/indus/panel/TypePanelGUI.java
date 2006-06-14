package edu.iastate.anthill.indus.panel;

import java.util.HashMap;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
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

import edu.iastate.utils.Debug;

public abstract class TypePanelGUI
    extends IndusPane
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

    JSplitPane jSplitPane1 = new JSplitPane();
    JPanel leftPanel = new JPanel();
    JPanel rightPanel = new JPanel();

    DefaultListModel model = new DefaultListModel();
    JList listAllTypes = new JList(model);
    JButton btnNewType = new JButton();
    JLabel labelSelectedType = new JLabel();
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel2 = new JPanel();
    JButton btnUpdateAVT = new JButton();
    JScrollPane jScrollPaneTree = new JScrollPane();
    BorderLayout borderLayout2 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    JButton btnExportType = new JButton();

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

        btnUpdateAVT.setText("Save Type");
        leftPanel.add(jp, BorderLayout.CENTER);

        // right panel
        rightPanel.setLayout(borderLayout1);

        // tree panel
        jPanel1.setLayout(borderLayout2);
        jPanel1.add(jScrollPaneTree, BorderLayout.CENTER);
        rightPanel.add(jPanel1, BorderLayout.CENTER);

        // button
        btnNewType.setText("New Type");
        btnExportType.setText("Export XML");
        jPanel2.add(btnUpdateAVT);
        jPanel2.add(btnNewType);
        jPanel2.add(btnExportType, null);
        jPanel2.setLayout(flowLayout1);
        rightPanel.add(jPanel2, BorderLayout.SOUTH);

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
    class TypeListCellRenderer
        extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
        {
            Component retValue = super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus
                );

            setIcon( (ImageIcon) typeIcon.get(value));

            return retValue;
        }
    }

    HashMap typeIcon = new HashMap();
}
