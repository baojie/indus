package edu.iastate.anthill.indus.datasource.type;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import edu.iastate.anthill.indus.datasource.Configable;
import edu.iastate.anthill.indus.datasource.DataSourceNode;
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

public class AVH extends DAG implements Configable {

    private static final long serialVersionUID = -205004159734123062L;

    TypedTree treeAVT = null;
    String nameOfOrder = null;
    public String template = null; // 2005-03-30

    public AVH() {
        super();
        this.supertype = "AVH";
    }

    public AVH(String name, String treetype) {
        super();
        this.name = name;
        this.supertype = "AVH";
        this.nameOfOrder = treetype; // e.g isa

        if (("AVH").equals(supertype)) {
            TypedNode root = new TypedNode(name + "_AVH");
            root.setType(TypedNode.AVH);

            SortTreeModel model = new SortTreeModel(root);
            treeAVT = new TypedTree(model);
        } else {
            TypedNode root = new TypedNode("N/A");
            root.setType(TypedNode.ROOT);

            SortTreeModel model = new SortTreeModel(root);
            treeAVT = new TypedTree(model);
        }

        // TreeModelListener tml = treeAVT.createTreeModelListener();

        treeAVT.setEditable(false);
        treeAVT.setShowsRootHandles(true);
        treeAVT.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeAVT.addDrageDropListener(new MyDropListener(this));
        treeAVT.setEnableDragDrop(true);
        treeAVT.setCellRenderer(render);
    }

    public String toXML() {
        AVHSerializer writer = new AVHSerializer();
        writer.objToXML(this);
        return writer.getXML();
    }

    public void fromXML(String datatypeinXML) {
        // old xml format -used by Apr 1 2005
        if (datatypeinXML.indexOf("<order><child>") >= 0) {
            //new OldAVHSerializer(this).OldFromXML(datatypeinXML);
        } else // new format
        {
            AVHSerializer writer = new AVHSerializer();
            writer.configXML = SimpleXMLParser.parseXmlString(datatypeinXML);
            writer.xmlToObj(this);
        }
    }

    // export the AVH to plain text
    //2006-06-20 Jie Bao
    public String toText() {
        StringBuffer buf = new StringBuffer();
        buf.append(";typename=" + name + "\n");
        if (supertype != null) {
            buf.append(";subTypeOf=" + supertype + "\n");
        }
        if (this.nameOfOrder != null) {
            buf.append(";ordername=" + nameOfOrder + "\n");
        }
        if (this.template != null) {
            buf.append(";template=" + template + "\n");
        }
        try {
            StringWriter w = new StringWriter();
            treeAVT.toStream(w);
            String text = w.toString();
            buf.append(text);
        } catch (IOException e) {

            e.printStackTrace();
        }
        //Debug.trace("before encoding: "+buf.length());
        
        return buf.toString();
    }

    /**
     * Read the AVH from plain text
     * 
     * @author baojie
     * @since 2006-06-20
     * @param datatypeinText
     */
    public void fromText(String datatypeinText) 
    {
        fromText(datatypeinText, true);
    }
    
    public void fromText(String datatypeinText, boolean full) {
        try {
            // read the meta information of the AVH
            BufferedReader in = new BufferedReader(new StringReader(
                    datatypeinText));
            String str;
            while ((str = in.readLine()) != null) {
                int pos = str.lastIndexOf('=');
                String value = str.substring(pos + 1);
                if (str.startsWith(";typename=")) {
                    name = value;
                } else if (str.startsWith(";subTypeOf=")) {
                    supertype = value;
                } else if (str.startsWith(";ordername=")) {
                    nameOfOrder = value;
                } else if (str.startsWith(";template=")) {
                    template = value;
                }
                if (!str.startsWith(";"))
                    break;
            }
            in.close();
            
            // read the tree
            if (full) {
                final String typeName = name;
                treeAVT = new TypedTree()
                {
                    public TypedNode newNode(String name, String comment)
                    {   
                        DataSourceNode newNode = new DataSourceNode(name,
                                DataSourceNode.AVH, typeName, comment);
                        return newNode;
                    }
                };
                treeAVT.fromText(datatypeinText);
            }
            
        } catch (IOException e) {
        }
    }

    public void setTree(TypedTree tree) {
        treeAVT = tree;
        treeAVT.setEditable(false);
        treeAVT.setShowsRootHandles(true);
        treeAVT.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * @param supertype String
     * @return boolean
     * @since 2004-10-15
     */
    public static boolean isAVH(String type, String supertype) {
        if (type.equals("AVH")) {
            return true;
        } else if (supertype == null) {
            return false;
        } else {
            return (supertype.equals("AVH"));
        }
    }

    public TypedTree getTreeAVH() {
        return treeAVT;
    }

    public String getNameOfOrder() {
        return nameOfOrder;
    }

    public void setNameOfOrder(String nameOfOrder) {
        this.nameOfOrder = nameOfOrder;
    }

    public JPanel getEditorPane() {
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

    public void buildRender() {
        render = new TypedTreeRender();
    }

    public void buildEditor() {
        editor = new AVHEditor(null, this);
    }

    protected AVHEditor editor;

    /**
     * @since 2005-04-20
     * @return AVHEditor
     */
    public AVHEditor getAVHEditor() {
        return editor;
    }

    public String print() {
        return treeAVT.toString();
    }

    public String getInformation() {
        String info = "";
        if (getSupertype() != null) {
            info += ", is subtype of '" + getSupertype() + "'";
        }
        if (getNameOfOrder() != null) {
            info += "', tree type: '" + getNameOfOrder() + "'";
        }
        info += ", number of terms: " + this.getSize();
        
        return info;
    }

    public String toString() {
        return this.name + "[AVH]";
    }

    public void setName(String typeName) {
        this.name = typeName;
    }

    // 2005-03-30
    public static String parseTemplate(String datatypeinXML) {
        Vector vec = SimpleXMLParser.getNestedBlock("template", datatypeinXML,
                false);
        if (vec != null && vec.size() > 0) {
            return (String) vec.elementAt(0);
        }
        return null;
    }
    
    /**
     * Get the size of the tree
     * 
     * @author baojie
     * @since 2006-06-20
     * @return
     */
    public int getSize()
    {
        int count =0;
        Enumeration e = treeAVT.getTop().preorderEnumeration();
        while (e.hasMoreElements()) {
            e.nextElement();
            count++;
        }
        return count;
    }
}

// 2005-04-19
class MyDropListener extends DragDropListener {
    AVH avh;

    public MyDropListener(AVH avh) {
        this.avh = avh;
    }

    public void onDrop(TreeNode selected, TreeNode dropTarget) {
        TreeNode oldParent = ((TypedNode) selected).getParent();
        avh.treeAVT.getModel().removeNodeFromParent((TypedNode) selected);
        avh.treeAVT.getModel().insertNodeInto((TypedNode) selected,
                (TypedNode) dropTarget, 0);
        AVHEditor ae = avh.getAVHEditor();
        TreeNodeMoveEditing e = new TreeNodeMoveEditing(avh.treeAVT,
                (TypedNode) selected, (TypedNode) oldParent,
                (TypedNode) dropTarget);
        ae.addHistory(e);

        avh.modified = true;
    }

    public boolean canDrag(TreeNode selected) {
        return true;
    }
}
