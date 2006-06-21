package edu.iastate.anthill.indus.tree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import edu.iastate.utils.gui.JTreeEx;

/**
 * Generic tree data structure with typed node
 * 
 * @author Jie Bao
 * @since 2004-05-01
 */

public class TypedTree extends JTreeEx {
    public TypedTree(DefaultTreeModel model) {
        super(model);
    }

    public TypedTree() {
        super();
    }

    public TypedTree(TypedNode top) {
        super(top);
        this.setTop(top);
    }

    public String toString() {
        return toString(false);
    }

    public String toString(boolean toHTML) {
        // String blank = toHTML ? "&nbsp;&nbsp;&nbsp;&nbsp;" : " ";
        String endline = "|---";
        String line = toHTML ? "|&nbsp;&nbsp;&nbsp;" : "|   ";
        String crlf = toHTML ? "<BR></BR>" : "\n";

        if (getTop() == null) {
            return "";
        }
        String toPrint = "";

        Enumeration e = getTop().preorderEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode nn = (DefaultMutableTreeNode) e
                    .nextElement();
            String leading = line;

            if (nn.getLevel() == 0) {
                leading = "";
            } else if (nn.getLevel() == 1) {
                leading = endline;
            } else {
                leading = endline;
                for (int i = 0; i < nn.getLevel() - 1; i++) {
                    leading = line + leading;
                }
            }
            toPrint += leading + nn + crlf;
        }

        if (toHTML) {
            return toPrint;
        } else {
            return toPrint;
        }
    }

    public void setTop(TypedNode top) {
        getModel().setRoot(top);
    }

    public TypedNode getTop() {
        return (TypedNode) getModel().getRoot();
    }

    public void buildSampleTree() {
        TypedNode top = new TypedNode("USA");
        top.setComment("United States of America");

        TypedNode iowa = new TypedNode("Iowa");
        top.add(iowa);
        iowa.add(new TypedNode("Ames"));
        iowa.add(new TypedNode("DesMoines"));

        TypedNode va = new TypedNode(
                "Virginia");
        top.add(va);
        va.add(new TypedNode("Richmond"));
        va.add(new TypedNode("Petersberg"));

        setTop(top);

    }

    /**
     * replace the value of old node to that of the new node. if new node has
     * children, they will be children of the old node.
     * 
     * @param tree
     *            TypedTree
     * @param node
     *            TypedNode
     * @param newNode
     *            TypedNode
     * @return TypedNode - the old node
     * @since 2005-03-31
     * @author Jie Bao
     */
    public static TypedNode amendNode(TypedTree tree, TypedNode node,
            TypedNode newNode) {
        try {
            DefaultTreeModel model = tree.getModel();

            // get the existing children
            Vector oldSons = new Vector();
            int oldSonCount = node.getChildCount();
            // System.out.println("node has children " + oldSonCount);

            for (int j = 0; j < oldSonCount; j++) {
                TypedNode son = (TypedNode) node.getChildAt(j);
                oldSons.add(son.getUserObject());
            }
            // System.out.println("oldSons" + oldSons);

            // add all children of new node to oldNode if it's new
            Vector newSons = new Vector();
            for (Enumeration e = newNode.children(); e.hasMoreElements();) {
                newSons.add(e.nextElement());
            }
            for (Enumeration e = newSons.elements(); e.hasMoreElements();) {
                TypedNode kid = (TypedNode) e.nextElement();
                System.out.println(kid);
                if (!oldSons.contains(kid.getUserObject())) {
                    model.insertNodeInto(kid, node, node.getChildCount());
                }
                // node.add(kid);
            }
            // Debug.trace(child + " children moved");

            // copy value of new node to old node
            // if (node.getParent() != null)
            // {
            // TypedNode parent = (TypedNode) node.getParent();
            // int index = parent.getIndex(node);
            // model.removeNodeFromParent(node);
            // Debug.trace("remove");
            // model.insertNodeInto(newNode, parent, index);
            // Debug.trace("insert");

            node.setUserObject(newNode.getUserObject());
            node.setComment(newNode.getComment());
            // }
            return node;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 
     * Read the tree from a plain text file
     * 
     * @author baojie
     * @since 2006-06-13
     * 
     * Sample file: 
     * ont
     * ont\Arts:comments 
     * ont\Languages 
     * ont\LiberalArts 
     * ont\PhysicalEducation 
     * ont\Science 
     * ont\SocialSciences 
     * ont\Arts\Art 
     * ont\Arts\Dance
     * ont\Arts\Music 
     * ont\Arts\Theatre 
     * ont\Arts\Art\ArtHistory
     * ont\Arts\Art\Drawing 
     * ont\Arts\Art\Sculpture
     * 
     * all lines start with ';' are ignored
     *     text afer ':' are comments for a node
     * 
     */
    public void fromFile(String filePath) {
        try {
            fromStream(new FileReader(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author baojie
     * @since 2006-06-19
     * @param typeText
     */
    public void fromString(String typeText) {
        try {
            fromStream(new StringReader(typeText));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author baojie
     * @since 2006-06-13
     * @param is
     * @throws IOException
     */
    public void fromStream(Reader is) throws IOException {

        HashMap<String, TypedNode> m = new HashMap<String, TypedNode>();

        BufferedReader in = new BufferedReader(is);

        String str = in.readLine();
        // skip all comment lines , 2006-06-19 Jie Bao
        while (str.startsWith(";")) {
            str = in.readLine();
        }

        // the first non-comment line is the root
        TypedNode top = newNode(str,null);
        m.put(str, top);

        while ((str = in.readLine()) != null) {

            String fullName = str;
            String comments = null;

            int k = str.indexOf(':');
            if (k != -1) {
                fullName = str.substring(0, k);
                comments = str.substring(k + 1);
            }

            // create the node
            TypedNode node = newNode(fullName, comments);
            m.put(fullName, node);
            

            // find parent
            int i = fullName.lastIndexOf('\\');
            if (i != -1) {
                String last = fullName.substring(i + 1);
                node.setUserObject(last);
                // System.out.println(last);

                //                // split the uiuc data
                //                // CHIN_411_Fourth-Year_Chinese ->
                //                // CHIN_411 and Fourth-Year_Chinese
                //                for (int j = 0; j < last.length() - 1; j++) {
                //                    // search for the first number
                //                    char c = last.charAt(j);
                //                    // System.out.print(c+"["+Character.isDigit(c)+"]");
                //                    if (Character.isDigit(c)) {
                //                        // search for the first _ after numbers
                //                        int k = last.indexOf('_', j);
                //                        if (k == -1)
                //                            continue;
                //                        String code = last.substring(0, k);
                //                        String name = last.substring(k + 1);
                //                        // System.out.println(code + ":"+name);
                //                        node.setUserObject(code);
                //                        node.setComment(name);
                //                        // System.out.println(node);
                //                        break;
                //                    }
                //
                //                }

                String parent = fullName.substring(0, i);
                TypedNode parentNode = m.get(parent);
                if (parentNode != null) {
                    parentNode.add(node);
                }
            }
        }
        in.close();
        setTop(top);
    }
    
    // this function can be overriden
    public TypedNode newNode(String name, String comment)
    {   
        TypedNode n = new TypedNode(name);
        n.setComment(comment);
        return n ;
    }

    public void toStream(Writer w) throws IOException {

        if (getTop() == null) {
            return;
        }

        BufferedWriter out = new BufferedWriter(w);

        Enumeration e = getTop().preorderEnumeration();
        while (e.hasMoreElements()) {
            TypedNode nn = (TypedNode) e.nextElement();
            out.write(toText(nn) + "\n");
        }
        out.close();

    }

    /**
     * @author baojie
     * @since 2006-06-19
     * @param node
     * @return
     */
    private String toText(TypedNode node) {
        List<TypedNode> list = new ArrayList<TypedNode>();

        // Add all nodes to list
        while (node != null) {
            list.add(node);
            node = (TypedNode) node.getParent();
        }
        Collections.reverse(list);

        StringBuffer buf = new StringBuffer();
        TypedNode root = list.get(0);
        buf.append(root.getUserObject());

        for (int i = 1; i < list.size() - 1; i++) {
            TypedNode p = list.get(i);
            buf.append("\\" + p.getUserObject());
        }
        
        TypedNode p = list.get(list.size() - 1);
        if (p != root)
            buf.append("\\" + p.toString());// see TypedNode.toString

        return buf.toString();
    }
    
    // 2006-06-20 Jie Bao
    private void testToFromText()
    {
         try {
            System.out.println("1. Test toStream()");
            StringWriter w = new StringWriter();
            toStream(w);
            String text = w.toString();
            System.out.println("Text export of the tree: \n\n" +text);
            
            System.out.println("2. Test fromStream()");
            StringReader r = new StringReader(text);
            TypedTree t = new TypedTree();
            t.fromStream(r);
            System.out.println("Reconstructed Tree:\n\n"+t);
            
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
    
    // 2006-06-19 Jie Bao
    private void visualize() 
    {
        JFrame frame = new JFrame();
        frame.setSize(800, 600);

        JEditorPane pane = new JEditorPane();
        frame.getContentPane().add(new JScrollPane(pane));

        pane.setContentType("text/html");
        pane.setText(this.toString(true));

        frame.setVisible(true);
    }

    // for test purpose
    public static void main(String[] args) {
        TypedTree t = new TypedTree();
        t.buildSampleTree();
        //t.visualize();
        t.testToFromText();

        // System.out.print(t);
    }
}
