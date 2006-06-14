package edu.iastate.anthill.indus.tree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.iastate.utils.gui.JTreeEx;

/**
 * Generic tree data structure with typed node
 *
 * @author Jie Bao
 * @since 2004-05-01
 */

public class TypedTree
    extends JTreeEx
{
    public TypedTree(DefaultTreeModel model)
    {
        super(model);
    }

    public TypedTree()
    {
        super();
    }

    public TypedTree(TypedNode top)
    {
        super(top);
        this.setTop(top);
    }

    public String toString()
    {
        return toString(false);
    }

    public String toString(boolean toHTML)
    {
        //String blank = toHTML ? "&nbsp;&nbsp;&nbsp;&nbsp;" : "    ";
        String endline = "|---";
        String line = toHTML ? "|&nbsp;&nbsp;&nbsp;" : "|   ";
        String crlf = toHTML ? "<BR></BR>" : "\n";

        if (getTop() == null)
        {
            return "";
        }
        String toPrint = "";

        Enumeration e = getTop().preorderEnumeration();
        while (e.hasMoreElements())
        {
            DefaultMutableTreeNode nn = (DefaultMutableTreeNode) e.nextElement();
            String leading = line;

            if (nn.getLevel() == 0)
            {
                leading = "";
            }
            else if (nn.getLevel() == 1)
            {
                leading = endline;
            }
            else
            {
                leading = endline;
                for (int i = 0; i < nn.getLevel() - 1; i++)
                {
                    leading = line + leading;
                }
            }
            toPrint += leading + nn + crlf;
        }

        if (toHTML)
        {
            return toPrint;
        }
        else
        {
            return toPrint;
        }
    }

    public void setTop(TypedNode top)
    {
        getModel().setRoot(top);
    }

    public TypedNode getTop()
    {
        return (TypedNode) getModel().getRoot();
    }

    public void buildSampleTree()
    {
        TypedNode top = new TypedNode("http://semanticWWW.com/indus.owl#USA");

        TypedNode iowa = new TypedNode(
            "http://semanticWWW.com/indus.owl#Iowa");
        top.add(iowa);
        iowa.add(new TypedNode(
            "http://semanticWWW.com/indus.owl#Ames"));
        iowa.add(new TypedNode(
            "http://semanticWWW.com/indus.owl#DesMoines"));

        TypedNode va = new TypedNode(
            "http://semanticWWW.com/indus.owl#Virginia");
        top.add(va);
        va.add(new TypedNode(
            "http://semanticWWW.com/indus.owl#Richmond"));
        va.add(new TypedNode(
            "http://semanticWWW.com/indus.owl#Petersberg"));

        setTop(top);

    }

    /**
     * replace the value of old node to that of the new node. if new node has
     *   children, they will be children of the old node.
     * @param tree TypedTree
     * @param node TypedNode
     * @param newNode TypedNode
     * @return TypedNode - the old node
     * @since 2005-03-31
     * @author Jie Bao
     */
    public static TypedNode amendNode(TypedTree tree, TypedNode node,
                                      TypedNode newNode)
    {
        try
        {
            DefaultTreeModel model = tree.getModel();

            // get the existing children
            Vector oldSons = new Vector();
            int oldSonCount = node.getChildCount();
            //System.out.println("node has children " + oldSonCount);

            for (int j = 0; j < oldSonCount; j++)
            {
                TypedNode son = (TypedNode) node.getChildAt(j);
                oldSons.add(son.getUserObject());
            }
            //System.out.println("oldSons" + oldSons);

            // add all children of new node to oldNode if it's new
            Vector newSons = new Vector();
            for (Enumeration e = newNode.children(); e.hasMoreElements(); )
            {
                newSons.add(e.nextElement());
            }
            for (Enumeration e = newSons.elements(); e.hasMoreElements(); )
            {
                TypedNode kid = (TypedNode) e.nextElement();
                System.out.println(kid);
                if (!oldSons.contains(kid.getUserObject()))
                {
                    model.insertNodeInto(kid, node, node.getChildCount());
                }
                //node.add(kid);
            }
            //Debug.trace(child + " children moved");


            // copy value of new node to old node
            //if (node.getParent() != null)
            //{
            //TypedNode parent = (TypedNode) node.getParent();
            //int index = parent.getIndex(node);
            //model.removeNodeFromParent(node);
            //Debug.trace("remove");
            //model.insertNodeInto(newNode, parent, index);
            //Debug.trace("insert");

            node.setUserObject(newNode.getUserObject());
            node.setComment(newNode.getComment());
            //}
            return node;

        }
        catch (Exception ex)
        {
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
ont
ont\Arts
ont\Languages
ont\Liberal Arts
ont\Physical Education
ont\Science
ont\Social Sciences
ont\Arts\Art
ont\Arts\Dance
ont\Arts\Music
ont\Arts\Theatre
ont\Arts\Art\Art History
ont\Arts\Art\Drawing
ont\Arts\Art\Sculpture
     *
     */
    public void fromFile(String filePath)
    {
    	
    	try {
    		HashMap<String, TypedNode> m = new HashMap<String, TypedNode>();
    		
    		BufferedReader in = new BufferedReader(new FileReader(filePath));
    		String str = in.readLine(); // the first line is the root
    		TypedNode top = new TypedNode(str);
    		m.put(str,top);
    		
    		while ((str = in.readLine()) != null) {
    			// create the node
    			TypedNode node = new TypedNode(str);
    			m.put(str,node);
    			// find parent
    			int i = str.lastIndexOf('\\');
    			if(i != -1)
    			{
    				String last = str.substring(i+1);
    				node.setUserObject(last);
    				//System.out.println(last);
    				
    				// split the uiuc data
    				// CHIN_411_Fourth-Year_Chinese ->
    				// CHIN_411 and Fourth-Year_Chinese
    			    for(int j = 0 ; j < last.length()-1; j++)
    			    {
    			    	// search for the first number
    			    	char c = last.charAt(j);
    			    	//System.out.print(c+"["+Character.isDigit(c)+"]");
    			    	if (Character.isDigit(c))
    			    	{
    			    		// search for the first _ after numbers
    			    		int k = last.indexOf('_',j);
    			    		if(k==-1)
    			    			continue;
    			    		String code = last.substring(0,k);
    			    		String name = last.substring(k+1);
    			    		//System.out.println(code + ":"+name);
    			    		node.setUserObject(code);
    			    		node.setComment(name);
    			    		//System.out.println(node);
    			    		break;
    			    	}
    			    	
    			    	
    			    }
    				
    				String parent = str.substring(0,i);
    				TypedNode parentNode = m.get(parent);
    				if (parentNode !=null)
    				{
    					parentNode.add(node);    					
    				}
    			}
    		}
    		in.close();
    		setTop(top);        	
    	} 
    	catch (IOException e) {
    	}  	
    }

// for test purpose
    public static void main(String[] args)
    {
        TypedTree t = new TypedTree();
        t.buildSampleTree();

        JFrame frame = new JFrame();
        frame.setSize(800, 600);

        JEditorPane pane = new JEditorPane();
        frame.getContentPane().add(new JScrollPane(pane));

        pane.setContentType("text/html");
        pane.setText(t.toString(true));

        frame.setVisible(true);

        // System.out.print(t);
    }
}
