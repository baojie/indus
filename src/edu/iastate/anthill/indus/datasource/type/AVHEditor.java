package edu.iastate.anthill.indus.datasource.type;

import java.util.Enumeration;

import java.awt.HeadlessException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.datasource.DataSourceNode;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTreeEditor;

public class AVHEditor
    extends TypedTreeEditor
{
    protected AVH avh;
    public boolean enableRename = true;
    public boolean enableInsertParent = true;
    public boolean enableDeleteButKeepChildren = true;

    public void setEnableRename(boolean enableRename)
    {
        this.enableRename = enableRename;
    }

    public void setEnableInsertParent(boolean enableInsertParent)
    {
        this.enableInsertParent = enableInsertParent;
    }

    public void setEnableDeleteButKeepChildren(boolean
                                               enableDeleteButKeepChildren)
    {
        this.enableDeleteButKeepChildren = enableDeleteButKeepChildren;
    }

    public AVHEditor()
    {
        super();
    }

    public AVHEditor(JPanel panel, AVH avh)
    {
        this.tree = avh.treeAVT;
        this.fatherPanel = panel;
        this.avh = avh;
    }

    protected void buildContextMenu(TypedNode selectedNode)
    {
        if (!avh.readOnly)
        {
            if (history.canUndo())
            {
                String label = "Undo " + history.topUndo();
                addMenuItem(label, IndusConstants.iconUndo,
                            new DefaultUndoAction());
            }

            if (history.canRedo())
            {
                String label = "Redo " + history.topRedo();
                addMenuItem(label, IndusConstants.iconRedo,
                            new DefaultRedoAction());
            }

            if (history.canRedo() || history.canUndo())
            {
                popup.add(new JSeparator());
            }
            // rename if enabled
            if (enableRename)
            {
                addMenuItem("Rename", IndusConstants.iconRename,
                            new RenameAction(selectedNode));
            }
            // Edit Comments for any node
            if (true) //selectedNode.getParent() != null)
            {
                addMenuItem("Edit Comments", IndusConstants.iconComment,
                            new DefaultEditCommentsAction(selectedNode));
            }

            popup.add(new JSeparator());

            // add sub node
            if (true) //selectedNode.getType() == TypedNode.AVH)
            {
                addMenuItem("Add sub value", IndusConstants.iconAddSub,
                            new CreateSubvalueAction(selectedNode));
            }
            // insert parent if not root
            if (selectedNode.getParent() != null && enableInsertParent)
            {
                addMenuItem("Add super value", IndusConstants.iconAddSup,
                            new CreateSuperValueAction(selectedNode));

            }

            popup.add(new JSeparator());

            // only show delete menu for not root
            if (selectedNode.getParent() != null)
            {
                addMenuItem("Delete", IndusConstants.iconDelete,
                            new DefaultDeleteAction(selectedNode));
            }
            // only delete all childrem menu for all but leaf
            if (selectedNode.getChildCount() != 0)
            {
                addMenuItem("Delete All Children", IndusConstants.iconDeleteSub,
                            new DefaultDeleteAllChildrenAction(selectedNode));

            }
            // Delete but keep children for all but leaf and root
            if (selectedNode.getChildCount() != 0 &&
                selectedNode.getParent() != null && enableDeleteButKeepChildren)
            {
                addMenuItem("Delete But Keep Children",
                            IndusConstants.iconDeleteSup,
                            new DefaultDeleteButKeepChildrenAction(selectedNode));
            }

        }
    }

    protected void changed(TypedNode theNode)
    {
        tree.getModel().reload(theNode);
        tree.expandNode(theNode);
        avh.modified = true;
        //tree.repaint();
    }

    protected String makeDefaultName(TypedNode selected)
    {
        return "";
    }

    protected TypedNode makeNewNode(TypedNode selected) throws
        HeadlessException
    {
        String defaultName = makeDefaultName(selected);
        String newName = JOptionPane.showInputDialog(
            "Give the name for new node", defaultName);
        if (newName == null)
        {
            return null;
        }

        if (!AVH.isLegalName(newName))
        {
            JOptionPane.showMessageDialog(null, "Name is not legal!");
            return null;
        }

        // make sure no duplicated names
        TypedNode n = (TypedNode) tree.getModel().getRoot();
        Enumeration ee = n.depthFirstEnumeration();
        while (ee.hasMoreElements())
        {
            TypedNode node = (TypedNode) ee.nextElement();
            String name = node.toString();
            if (name.equals(newName))
            {
                JOptionPane.showMessageDialog(null, "'" + newName +
                                              "' is used, please try again");
                return null;
            }
        }

        TypedNode newNode = new DataSourceNode(newName, DataSourceNode.AVH, null);

        return newNode;
    }

    /**
     * @since 2004-04-18
     */
    class CreateSuperValueAction
        extends DefaultInsertParentAction
    {
        public CreateSuperValueAction(TypedNode theNode)
        {
            super(theNode);
        }

        protected TypedNode getNewNode()
        {
            return makeNewNode(theNode);
        }
    }

    class CreateSubvalueAction
        extends DeafultCreateSubValueAction
    {
        public CreateSubvalueAction(TypedNode parent)
        {
            super(parent);
        }

        protected TypedNode getNewNode()
        {
            return makeNewNode(parent);
        }
    }

    protected class RenameAction
        extends DefaultRenameAction
    {
        public RenameAction(TypedNode theNode)
        {
            super(theNode);
        }

        protected String getNewUserObject() throws HeadlessException
        {
            String oldName = (String) theNode.getUserObject();
            String newName = JOptionPane.showInputDialog(
                "Give the name for new node", oldName);
            if (newName == null || oldName.equals(newName))
            {
                return null;
            }

            // validate the name
            if (!AVH.isLegalName(newName))
            {
                JOptionPane.showMessageDialog(null, "Name is not legal!");
                return null;
            }

            // make sure no duplicated names
            TypedNode n = (TypedNode) tree.getModel().getRoot();
            Enumeration ee = n.depthFirstEnumeration();
            while (ee.hasMoreElements())
            {
                TypedNode node = (TypedNode) ee.nextElement();
                String name = node.toString();
                if (name.equals(newName))
                {
                    JOptionPane.showMessageDialog(null, "'" + newName +
                                                  "' is used, please try again");
                    return null;
                }
            }
            return newName;
        }
    }
}
