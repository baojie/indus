package edu.iastate.anthill.indus.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.iastate.anthill.indus.gui.IndusGUI;
import edu.iastate.anthill.indus.tree.TreePopupMenuListener;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;

/**
 * popup menu on the tree
 * @author Jie Bao
 * @since 1.0 2004-10-15
 */
public class MappingPopupMenuListener
    extends TreePopupMenuListener
{
    public MappingPopupMenuListener(MappingPanel panel, TypedTree tree)
    {
        this.tree = tree;
        this.fatherPanel = panel;
    }

    protected void buildContextMenu(TypedNode selectedNode)
    {
        if (selectedNode.getType() == TypedNode.DB ||
            selectedNode.getType() == TypedNode.ATTRIBUTE)
        {
            addMenuItem("Edit Schema", null,
                        new EditSchemaAction(selectedNode), popup, null);
        }
        else if (selectedNode.getType() == TypedNode.AVH)
        {
            addMenuItem("Edit Data Type", null,
                        new EditDataTypeAction(selectedNode), popup, null);
        }

    }

    protected void changed(TypedNode theNode)
    {
    }

    class EditSchemaAction
        implements ActionListener
    {
        TypedNode theNode;

        public EditSchemaAction(TypedNode theNode)
        {
            this.theNode = theNode;
        }

        public void actionPerformed(ActionEvent evt)
        {
            // goto schema edit panel
            MappingPanel p = ( (MappingPanel) fatherPanel);
            String schema = p.findNodeType(theNode);
            System.out.println(schema);

            IndusGUI par = ( (MappingPanel) fatherPanel).parent;
            par.switchToPane(par.paneSchema, schema);
        }
    }

    class EditDataTypeAction
        implements ActionListener
    {
        TypedNode theNode;

        public EditDataTypeAction(TypedNode theNode)
        {
            this.theNode = theNode;
        }

        public void actionPerformed(ActionEvent evt)
        {
            // goto schema edit panel
            MappingPanel p = ( (MappingPanel) fatherPanel);
            String type = p.findNodeType(theNode);
            System.out.println(type);

            IndusGUI par = ( (MappingPanel) fatherPanel).parent;
            par.switchToPane(par.paneOntology, type);

        }
    }

} //}}}
