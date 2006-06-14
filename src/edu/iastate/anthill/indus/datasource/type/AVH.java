package edu.iastate.anthill.indus.datasource.type;

import java.util.Vector;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import edu.iastate.anthill.indus.datasource.Configable;
import edu.iastate.anthill.indus.tree.SortTreeModel;
import edu.iastate.anthill.indus.tree.TreeNodeMoveEditing;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;
import edu.iastate.anthill.indus.tree.TypedTreeRender;

import edu.iastate.utils.gui.DragDropListener;
import edu.iastate.utils.string.SimpleXMLParser;

/**
 * @author Jie Bao
 * @since 1.0 2004-10-08
 */

public class AVH
    extends DAG implements Configable
{
    TypedTree treeAVT = null;
    String nameOfOrder = null;
    String template = null; // 2005-03-30

    public AVH()
    {
        super();
        this.supertype = "AVH";
    }

    public AVH(String name, String treetype)
    {
        super();
        this.name = name;
        this.supertype = "AVH";
        this.nameOfOrder = treetype;

        if ( ("AVH").equals(supertype))
        {
            TypedNode root = new TypedNode(name + "_AVH");
            root.setType(TypedNode.AVH);

            SortTreeModel model = new SortTreeModel(root);
            treeAVT = new TypedTree(model);
        }
        else
        {
            TypedNode root = new TypedNode("N/A");
            root.setType(TypedNode.ROOT);

            SortTreeModel model = new SortTreeModel(root);
            treeAVT = new TypedTree(model);
        }

        // TreeModelListener tml = treeAVT.createTreeModelListener();

        treeAVT.setEditable(false);
        treeAVT.setShowsRootHandles(true);
        treeAVT.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeAVT.addDrageDropListener(new MyDropListener(this));
        treeAVT.setEnableDragDrop(true);
    }

    public String toXML()
    {
        AVHSerializer writer = new AVHSerializer();
        writer.objToXML(this);
        return writer.getXML();
    }

    public void fromXML(String datatypeinXML)
    {
        // old xml format -used by Apr 1 2005
        if (datatypeinXML.indexOf("<order><child>") >= 0)
        {
           // new OldAVHSerializer(this).OldFromXML(datatypeinXML);
        }
        else // new format
        {
            AVHSerializer writer = new AVHSerializer();
            writer.configXML = SimpleXMLParser.parseXmlString(datatypeinXML);
            writer.xmlToObj(this);
        }
    }

    public void setTree(TypedTree tree)
    {
        treeAVT = tree;
        treeAVT.setEditable(false);
        treeAVT.setShowsRootHandles(true);
        treeAVT.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * @param supertype String
     * @return boolean
     * @since 2004-10-15
     */ public static boolean isAVH(String type, String supertype)
    {
        if (type.equals("AVH"))
        {
            return true;
        }
        else if (supertype == null)
        {
            return false;
        }
        else
        {
            return (supertype.equals("AVH"));
        }
    }

    public TypedTree getTreeAVH()
    {
        return treeAVT;
    }

    public String getNameOfOrder()
    {
        return nameOfOrder;
    }

    public void setNameOfOrder(String nameOfOrder)
    {
        this.nameOfOrder = nameOfOrder;
    }

    public JPanel getEditorPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        buildEditor();
        treeAVT.addMouseListener(editor);
        buildRender();
        treeAVT.setCellRenderer(render);
        p.add(treeAVT);
        return p;
    }

    protected TypedTreeRender render = new TypedTreeRender();

    public void buildRender()
    {
        render = new TypedTreeRender();
    }

    public void buildEditor()
    {
        editor = new AVHEditor(null, this);
    }

    protected AVHEditor editor;

    /**
     * @since 2005-04-20
     * @return AVHEditor
     */
    public AVHEditor getAVHEditor()
    {
        return editor;
    }

    public String print()
    {
        return treeAVT.toString();
    }

    public String getInformation()
    {
        String info = "";
        if (getSupertype() != null)
        {
            info += ", is subtype of '" + getSupertype() + "'";
        }
        if (getNameOfOrder() != null)
        {
            info += "', tree type: '" + getNameOfOrder() + "'";
        }
        return info;
    }

    public String toString()
    {
        return this.name + "[AVH]";
    }

    public void setName(String typeName)
    {
        this.name = typeName;
    }

    // 2005-03-30
    public static String parseTemplate(String datatypeinXML)
    {
        Vector vec = SimpleXMLParser.getNestedBlock("template", datatypeinXML, false);
        if (vec != null && vec.size() > 0)
        {
            return (String) vec.elementAt(0);
        }
        return null;
    }
}

// 2005-04-19
class MyDropListener
    extends DragDropListener
{
    AVH avh;
    public MyDropListener(AVH avh)
    {
        this.avh = avh;
    }

    public void onDrop(TreeNode selected, TreeNode dropTarget)
    {
        TreeNode oldParent = ( (TypedNode) selected).getParent();
        avh.treeAVT.getModel().removeNodeFromParent( (TypedNode) selected);
        avh.treeAVT.getModel().insertNodeInto( (TypedNode) selected,
                                              (TypedNode) dropTarget, 0);
        AVHEditor ae = avh.getAVHEditor();
        TreeNodeMoveEditing e = new TreeNodeMoveEditing(avh.treeAVT,
            (TypedNode) selected, (TypedNode) oldParent, (TypedNode) dropTarget);
        ae.addHistory(e);

        avh.modified = true;
    }

    public boolean canDrag(TreeNode selected)
    {
        return true;
    }
}
