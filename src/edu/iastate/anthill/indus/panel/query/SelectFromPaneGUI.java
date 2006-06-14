package edu.iastate.anthill.indus.panel.query;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultTreeModel;


public class SelectFromPaneGUI
    extends JPanel
{
    public SelectFromPaneGUI()
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

    /** Action command for > button. */
    protected static final String ADD_CMD = "0x6AO";
    /** Action command for < button. */
    protected static final String REMOVE_CMD = "0x7RO";
    /** Action command for << button. */
    protected static final String REMOVE_ALL_CMD = "0x8AA";

    /** Main panel of tab with tree, list and buttons. */
    protected JPanel mainPane = new JPanel();
    /** Option dialog for chosing table types. */
    OptionDialog optDialog;

    protected JButton btnAdd = new JButton();
    protected JButton btnRemove = new JButton();
    protected JButton btnRemoveAll = new JButton();

    protected JPanel southPane = new JPanel();
    protected JPanel blankrow = new JPanel();

    protected DefaultTreeModel treeModel;
    protected JTree sqlTree = new JTree(treeModel);

    public DefaultListModel listModel = new DefaultListModel();
    protected JList colList = new JList(listModel);

    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected JScrollPane jScrollPane2 = new JScrollPane();

    private void jbInit() throws Exception
    {
        // setFont (new Font (JSQLBuilder.fontName, 1, 11));
// setFont(_builder.getFont());

        setLayout(new BorderLayout());

        mainPane.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints1;

// initialize instance variable

        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        JLabel jLabel3 = new JLabel();

        jLabel1.setText(
            "What columns of data do you want to include in your query?");
        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        gridBagConstraints1.ipadx = 2;
        mainPane.add(jLabel1, gridBagConstraints1);

// blankrow.setMinimumSize(new Dimension(JSQLBuilder.width, 1));
// blankrow.setPreferredSize(new Dimension(JSQLBuilder.width, 7));
        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        mainPane.add(blankrow, gridBagConstraints1);

        jLabel2.setText("Available tables and columns:");
        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridwidth = 1;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        gridBagConstraints1.ipady = 10;
        mainPane.add(jLabel2, gridBagConstraints1);

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.weightx = 0.45;
        gridBagConstraints1.weighty = 1.0;
        gridBagConstraints1.gridwidth = 1;
        gridBagConstraints1.gridheight = 4;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        gridBagConstraints1.ipadx = 5;
        jScrollPane2.getViewport().setView(sqlTree);
        mainPane.add(jScrollPane2, gridBagConstraints1);
//		add(jScrollPane2, BorderLayout.CENTER);

        btnAdd.setText(">");
        btnAdd.setActionCommand(this.ADD_CMD);
        btnAdd.setEnabled(false);
        btnAdd.setToolTipText("Move selected column.");

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.weightx = 0.1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 19, 0, 19);
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        mainPane.add(btnAdd, gridBagConstraints1);

        btnRemove.setText("<");
        btnRemove.setActionCommand(this.REMOVE_CMD);
        btnRemove.setEnabled(false);
        btnRemove.setToolTipText("Remove selected column.");

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.weightx = 0.1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 19, 0, 19);
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        mainPane.add(btnRemove, gridBagConstraints1);

        btnRemoveAll.setText("<<");
        btnRemoveAll.setActionCommand(this.REMOVE_ALL_CMD);
        btnRemoveAll.setEnabled(false);
        btnRemoveAll.setToolTipText(
            "Move all chosen columns back to table(s).");

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 5;
        gridBagConstraints1.weightx = 0.1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 19, 0, 19);
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        mainPane.add(btnRemoveAll, gridBagConstraints1);

        jLabel3.setText("Columns in your query:");
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridwidth = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.ipady = 10;
        mainPane.add(jLabel3, gridBagConstraints1);

        colList.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        colList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.weightx = 0.45;
        gridBagConstraints1.weighty = 1.0;
        gridBagConstraints1.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = 4;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        gridBagConstraints1.ipadx = 1;
        jScrollPane1.getViewport().setView(colList);
        mainPane.add(jScrollPane1, gridBagConstraints1);

// add mainP to the tab
        add(mainPane, BorderLayout.CENTER);

//jPanel4.setBorder(BorderFactory.createLineBorder(Color.gray));
// add divider
        add(southPane, BorderLayout.SOUTH);
    }
}
