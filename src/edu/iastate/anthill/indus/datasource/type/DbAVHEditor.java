package edu.iastate.anthill.indus.datasource.type;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusMain;
import edu.iastate.anthill.indus.tree.TreeNodeInsertEditing;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;
import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.JStatusBar;
import edu.iastate.utils.gui.LabelledItemPanel;
import edu.iastate.utils.gui.StandardDialog;
import edu.iastate.utils.undo.BulkEditingAction;
import edu.iastate.utils.undo.EditingAction;

// popup menu on the tree
public class DbAVHEditor extends AVHEditor
{

    public DbAVHEditor(JPanel panel, AVH avh)
    {
        this.tree = avh.treeAVT;
        this.fatherPanel = panel;
        this.avh = avh;
    }

    public void mouseClicked(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            if (selPath != null)
            {
                tree.setSelectionPath(selPath);

                final TypedNode selectedNode = (TypedNode) selPath
                        .getLastPathComponent();

                //Debug.trace("double clicked on " + selectedNode);
                addOneLevel(selectedNode);
            }

        }
    }

    // 2005-04-24
    public void addAllLevel(TypedNode selectedNode)
    {
        DbAVH myDbAVH = (DbAVH) avh;
        addFromDB(selectedNode, myDbAVH, -1);
    }

    public void addOneLevel(TypedNode selectedNode)
    {
        DbAVH myDbAVH = (DbAVH) avh;
        addFromDB(selectedNode, myDbAVH, 1);
    }

    private void addFromDB(TypedNode selectedNode, DbAVH myDbAVH, int c)
    {
        String root = (String) selectedNode.getUserObject();
        TypedTree subTree = myDbAVH.templateTree.getTree(root, c);
        // if there is no new child
        if (subTree.getTop().getChildCount() > 0)
        {
            BulkEditingAction action = amendNode(tree, selectedNode, subTree
                    .getTop());
            action.summary = "Insert from template, root = '" + root
                    + "', cutoff = '" + c + "'";
            history.addAction(action);
            changed(selectedNode);
        }
    }

    protected void buildContextMenu(TypedNode selectedNode)
    {
        if (!avh.readOnly)
        {
            super.buildContextMenu(selectedNode);
            // if avh is DbAVH
            // build from a set of id for any DbAVH
            if (avh instanceof DbAVH && avh.template != null)
            {
                popup.add(new JSeparator());
                addMenuItem("Add subtree from template",
                        IndusConstants.iconDbTree, new AddFromTemplateAction(
                                selectedNode));
                addMenuItem("Add term set from template",
                        IndusConstants.iconDbSet, new AddSetFromTemplateAction(
                                selectedNode));
            }
        }
    }

    // 2005-03-31
    class AddFromTemplateAction implements ActionListener
    {
        TypedNode theNode;

        public AddFromTemplateAction(TypedNode theNode)
        {
            this.theNode = theNode;
        }

        public void actionPerformed(ActionEvent e)
        {
            //System.out.println( "getChildCount: "+theNode.getChildCount());

            final DbAVH myDbAVH = (DbAVH) avh;
            // ask for insert root and cutoff level on template tree
            // ask for root the cutoff
            String root = JOptionPane
                    .showInputDialog(
                            "Give the sub tree you want to be imported from template '"
                                    + avh.template
                                    + "'"
                                    + "\nLeave it blank if you want the '"
                                    + avh.template
                                    + "' root node"
                                    + "\n\nYou will be asked for the cutoff depth of the subtree in the next step",
                            theNode.getUserObject());
            if (root == null) { return; }

            if (root.trim().equals(""))
            {
                root = myDbAVH.templateTree.getRootId();
                String info = "You will import from '" + avh.template + "':"
                        + root;
                JOptionPane.showMessageDialog(null, info);
            }

            final String  cutoff = JOptionPane.showInputDialog(
                    "Give the cutoff depth of the new subtree"
                            + "\n put -1 here if you want the whole sub tree",
                    "1");
            if (cutoff == null) { return; }
            
            final String from = root;

            final JStatusBar statusBar = IndusMain.theInstance.statusBar;
            Thread t = new Thread() {
                public void run()
                {

                    int pb = statusBar.addProgressBar(true, 0, 0);
                    statusBar.updateProgressBar(pb, "Importing from '" + 
                            avh.template + "':" + from);

                    try
                    {
                        int c = Integer.parseInt(cutoff);

                        TypedTree subTree = myDbAVH.templateTree.getTree(from,
                                c);
                        //System.out.println(subTree);

                        // if there is no new child
                        if (subTree.getTop().getChildCount() == 0)
                        {
                            String info = "No child is found for " + theNode;
                            JOptionPane.showMessageDialog(null, info);
                        }
                        else
                        {
                            BulkEditingAction action = amendNode(tree, theNode,
                                    subTree.getTop());
                            action.summary = "Insert from template, root = '"
                                    + from + "', cutoff = '" + c + "'";
                            history.addAction(action);
                            changed(theNode);
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(null,
                                "Cutoff value is not correct!");
                        return;
                    }

                    statusBar.removeProgressBar(pb);
                }
            };
            t.start();

        }
    }

    /**
     * Merge newNode with oldNode
     * 
     * @since 2005-04-21
     * @param tree TypedTree
     * @param oldNode TypedNode
     * @param newNode TypedNode
     */
    public BulkEditingAction amendNode(TypedTree tree, TypedNode oldNode,
            TypedNode newNode)
    {
        DefaultTreeModel model = tree.getModel();
        BulkEditingAction bulk = new BulkEditingAction(oldNode);

        // get the existing children
        Vector oldSons = new Vector();
        int oldSonCount = oldNode.getChildCount();

        for (int j = 0; j < oldSonCount; j++)
        {
            TypedNode son = (TypedNode) oldNode.getChildAt(j);
            oldSons.add(son.getUserObject());
        }

        // add all children of new node to oldNode if it's new
        Vector newSons = new Vector();
        for (Enumeration e = newNode.children(); e.hasMoreElements();)
        {
            newSons.add(e.nextElement());
        }

        for (Enumeration e = newSons.elements(); e.hasMoreElements();)
        {
            TypedNode kid = (TypedNode) e.nextElement();
            //System.out.println(kid);
            // new id, insert it
            if (!oldSons.contains(kid.getUserObject()))
            {
                model.insertNodeInto(kid, oldNode, oldNode.getChildCount());
                EditingAction action = new TreeNodeInsertEditing(tree, kid,
                        oldNode);
                bulk.addAction(action);
            }
            else
            // old id , amend it
            {
                int idx = oldSons.indexOf(kid.getUserObject());
                TypedNode oldSon = (TypedNode) oldNode.getChildAt(idx);
                EditingAction action = amendNode(tree, oldSon, kid);
                bulk.addAction(action);
            }
        }

        return bulk;
    }

    // 2005-04-07
    class AddSetFromTemplateAction implements ActionListener
    {
        TypedNode theNode;

        public AddSetFromTemplateAction(TypedNode theNode)
        {
            this.theNode = theNode;
        }

        public void actionPerformed(ActionEvent e)
        {
            DbAVH myDbAVH = (DbAVH) avh;

            // ask for a set of id
            StandardDialog dlg = new StandardDialog();
            LabelledItemPanel myContentPane = new LabelledItemPanel();
            myContentPane.setBorder(BorderFactory.createEtchedBorder());
            myContentPane.addItem(" ", new JLabel(
                    "Please give a set of id, ONE id per line"));
            JTextArea idSet = new JTextArea(10, 20);
            myContentPane.addItem("ID Set", new JScrollPane(idSet,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
            dlg.setContentPane(myContentPane);
            dlg.pack();
            dlg.setVisible(true);

            if (!dlg.hasUserCancelled())
            {
                String all[] = idSet.getText().split("\n");
                Set ids = new HashSet(Arrays.asList(all));
                Debug.trace(ids);

                BulkEditingAction action = myDbAVH.templateTree.insertSet(
                        avh.treeAVT, ids);
                action.summary = "Add " + ids.size() + " terms from template";

                history.addAction(action);
                changed(theNode);
            }
        }
    }

} //}}}
