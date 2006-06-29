package edu.iastate.anthill.indus.datasource.type;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.iastate.anthill.indus.tree.TypedTree;

import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.JTreeEx;

/**
 * A dialog to visualize an AVH tree
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-27</p>
 *
 */
public class AVHDialog extends JDialog
{

    AVH            myAVH;
    public boolean isOK          = false;
    public Object  selectedValue = null;

    JPanel         panel1        = new JPanel();
    JButton        btnCancel     = new JButton();
    JButton        btnOK         = new JButton();

    public AVHDialog(AVH avh, String defaultValue, JFrame frame)
    {
        super(frame, "AVH Dialog", true);

        this.myAVH = avh;
        if (defaultValue != null) this.selectedValue = defaultValue.trim();
        //Debug.trace("'"+this.selectedValue+"'");

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

    private void jbInit() throws Exception
    {
        this.getContentPane().setLayout(new BorderLayout());
        btnCancel.setToolTipText("");
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                onCancel(e);
            }
        });
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                onOK(e);
            }
        });

        // set default selection
        //Debug.trace(myAVH);

        TypedTree tree = myAVH.getTreeAVH();
        //Debug.trace(tree.getTop());
        tree.setExpandsSelectedPaths(true);
        DefaultMutableTreeNode n = tree.selectFirst(selectedValue);
        //Debug.trace(n);

        JPanel treeEditor = myAVH.getEditorPane();
        //      this.getContentPane().add(new JScrollPane(treeEditor),
        //                          BorderLayout.CENTER);
        this.getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);
        btnCancel.setText("Cancel");
        this.getContentPane().add(panel1, BorderLayout.SOUTH);
        btnOK.setText("OK");
        panel1.add(btnOK, null);
        panel1.add(btnCancel, null);

        if (n != null)
        {
            tree.scrollPathToVisible(tree.getPath(n)); // 2006-06-13, baojie
        }

        this.pack();
    }

    public void onCancel(ActionEvent e)
    {
        isOK = false;
        dispose();
    }

    public void onOK(ActionEvent e)
    {
        isOK = true;
        TreePath path = myAVH.getTreeAVH().getSelectionPath();
        if (path != null)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
                    .getLastPathComponent();
            selectedValue = node.getUserObject();
        }
        dispose();
    }

    // for test
    public static void main(String[] args)
    {
        AVH avh = new AVH("Test Tree", "ISA");
        TypedTree tree = new TypedTree();
        tree.buildSampleTree();
        avh.setTree(tree);

        //Debug.trace(avh.getTreeAVH().toString());

        AVHDialog dlg = new AVHDialog(avh,
                "http://semanticWWW.com/indus.owl#Iowa", null);
        dlg.setSize(800, 600);
        dlg.show();

        if (dlg.isOK)
        {
            Debug.trace("Click OK, selected value = " + dlg.selectedValue);
        }
        else
        {
            Debug.trace("The dialog is cancelled");
        }

    }
}
