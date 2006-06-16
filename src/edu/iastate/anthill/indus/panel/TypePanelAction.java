package edu.iastate.anthill.indus.panel;

import java.util.Arrays;
import java.util.Vector;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.agent.IndusHttpClient;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.datasource.type.DAG;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.anthill.indus.datasource.type.DbAVH;
import edu.iastate.anthill.indus.datasource.type.SimpleDataType;
import edu.iastate.anthill.indus.iterator.DB2TreeFactory;

import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.ProgressBarWin;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;

/**
 *
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-29</p>
 */
public abstract class TypePanelAction
    extends TypePanelGUI implements MessageHandler
{
    public TypePanelAction()
    {
        super();
        try
        {
            jbInit2();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    JMenuItem menuDelete = new JMenuItem("Delete");
    JMenuItem menuCopyType = new JMenuItem("Copy Type");
    final JPopupMenu menu = new JPopupMenu();

    void jbInit2() throws Exception
    {
        messageMap();

        menu.add(menuDelete);
        menu.add(menuCopyType);
    }

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(this.btnNewType, this, "onCreateNewType");
            MessageMap.mapAction(this.btnUpdateAVT, this, "onUpdateType");
            MessageMap.mapAction(this.btnExportType, this, "onExport");
            MessageMap.mapAction(this.menuDelete, this, "onDeleteType");
            MessageMap.mapAction(this.menuCopyType, this, "onCopyType");

            listAllTypes.addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    onSelectedTypeChanged(e);
                }
            });
            // Set the component to show the popup menu
            listAllTypes.addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent evt)
                {
                    int row = listAllTypes.locationToIndex(evt.getPoint());
                    listAllTypes.setSelectedIndex(row);

                    if (evt.isPopupTrigger())
                    {
                        menu.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }

                public void mouseReleased(MouseEvent evt)
                {
                    if (evt.isPopupTrigger())
                    {
                        menu.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            });

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void onSelectedTypeChanged(ListSelectionEvent evt)
    {
        // When the user release the mouse button and completes the selection,
        // getValueIsAdjusting() becomes false
        if (!evt.getValueIsAdjusting())
        {
            JList list = (JList) evt.getSource();
            promptSave();
            final String currentSelectedType = (String) list.getSelectedValue();

            // Get all selected items
            if (currentSelectedType != null)
            {
                Thread t = new Thread()
                {
                    public void run()
                    {
                        //load it
                        loadType(currentSelectedType);

                        // enable / disenable "deletetype" button
                        // deleting of predefined type is not allowed

                        btnUpdateAVT.setEnabled(!AVH.isPredefinedType(
                            currentSelectedType));

                    }
                };
                t.start();

            }
        }
    }

    /**
     * If the type is AVT, create a tree for it
     *
     * @param selected String
     * @version 2004-09-30
     */
    protected void loadType(String selected)
    {
        int pb = parent.statusBar.addProgressBar(true, 0, 0);
        parent.statusBar.updateProgressBar(pb, "Loading type " + selected);

        DataType dt;
        dt = InfoReader.readDataType(selected);
        parent.statusBar.removeProgressBar(pb);

        if (dt != null)
        {
            currentType = dt;
            String info = "Selected Type : '" + (String) selected + "', " +
                currentType.getInformation();
            labelSelectedType.setText(info);
            
            String  supertype = currentType.getSupertype();
            ImageIcon icon = IndusConstants.iconDatatype;
            
		    if (AVH.isAVH( selected, supertype))
		    {
		        icon = IndusConstants.iconTreetype;
		    }
		    else if (DataType.isNumber( selected, supertype))
		    {
		        icon = IndusConstants.iconNumber;
		    }
		    else if (DataType.isString( selected, supertype))
		    {
		        icon = IndusConstants.iconString;
		    }
		    typeIcon.put(selected, icon);

            //Debug.trace(currentType.getClass());

            jScrollPaneTree.getViewport().removeAll();
            jScrollPaneTree.getViewport().add(currentType.getEditorPane(), null);
            currentType.modified = false;
        }
        else
        {
            String info = "Type definition is not available for '"
                + selected + "'";
            JOptionPane.showMessageDialog(this, info);
        }
    }

    DataType currentType = null;

    public void onCreateNewType(ActionEvent e)
    {
        // ==== 1. ask for name =======
        String used[] = InfoReader.getAllType();
        String name = askForName(used);

        if (name == null)
        {
            return;
        }

        // ======== 2. select father type =======
        String data[] = DataType.getPredefinedTypes();
        String fatherType = (String) JOptionPane.showInputDialog(this,
            "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE, null,
            data, data[0]);

        if (fatherType == null)
        {
            return;
        }

        // ======== 3. create new type ===========
        String treetype = null;
        //         === 3.1 AVH ====
        if (fatherType.equals("AVH"))
        {
            // select AVH tree type eg. partof, isa
            while (true)
            {
                treetype = JOptionPane.showInputDialog(
                    "Please give the type of the tree", "ISA");
                if (treetype == null)
                {
                    return;
                }

                // validate the name
                if (!AVH.isLegalName(treetype))
                {
                    JOptionPane.showMessageDialog(this, "Name is not legal!");
                }
                else
                {
                    break;
                }
            }

            // select template
            String temp[] = DB2TreeFactory.allTemplete;
            String template = (String) JOptionPane.showInputDialog(this,
                "Choose one tree template, or click cancel if you don't need any template" +
                "\n\n A template is a tree predefined by INDUS. If you selected a template to\n" +
                " the new AVH, you can copy the whole or part of the template into your new AVH",
                "Input", JOptionPane.INFORMATION_MESSAGE, null,
                temp, temp[0]);

            if (temp == null)
            {
                currentType = new AVH(name, treetype);
            }
            else
            {
                currentType = new DbAVH(name, treetype, template);
            }
        }
        //  === 3.1 DAG ====
        else if (fatherType.equals("DAG"))
        {
            currentType = new DAG(name);
        }
        else
        {
            currentType = new SimpleDataType(name, fatherType);
        }

        // ======== 4. submit the new type to server and update GUI ========
        if (currentType != null && save())
        {
            // update UI
            updateTypesList();
            listAllTypes.setSelectedValue(name, true);
        }
        currentType.modified = false;

    }

    public void onUpdateType(ActionEvent e)
    {
        save();
    }

    // export AVT to xml
    boolean save()
    {
        // Debug.trace(currentType.print());
        String typeName = null;
        try
        {
            if (currentType != null)
            {
                if (currentType.readOnly)
                {

                    JOptionPane.showMessageDialog(this,
                                                  "This is a read only type, cannot be updated");
                    return true;
                }

                typeName = currentType.getName();
                //Debug.trace(typeName);
                if (typeName != null)
                {
                    boolean suc = InfoWriter.writeType(currentType);
                    if (suc)
                    {
                        currentType.modified = false;
                        
                        // 2006-06-15, update the data type cache
                        InfoReader.updateDataTypeCache(typeName,currentType);
                                                
                        JOptionPane.showMessageDialog(this,
                            "Type '" + typeName + "' saved successfully");
                        return true;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(this,
                            "Cannot communicate with server, saving failed!");
                        return false;
                    }
                }
            }
            JOptionPane.showMessageDialog(this,
                                          "No current type! saving failed");
            return false;

        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Type '" + typeName +
                                          "' can't be updated");
            ex.printStackTrace();
            System.out.println(currentType.print());
        }
        return false;
    }

    /**
     * Copy a type
     * @param e ActionEvent
     * @since 2005-03-29
     */
    public void onCopyType(ActionEvent e)
    {
        String typeName = (String) listAllTypes.getSelectedValue();
        // predefined types cannot be copied
        if (DataType.isPredefinedType(typeName))
        {
            String info = "Predefined type cannot be copied";
            JOptionPane.showMessageDialog(this, info);
            return;
        }

        String used[] = InfoReader.getAllType();
        String name = askForName(used);

        if (name != null)
        {
            DataType dt = InfoReader.readDataType(typeName);
            dt.setName(name);
            if (InfoWriter.writeType(dt))
            {
                updateTypesList();
                String info = "Type " + name + " is copied from " + typeName +
                    " sucessfully";
                JOptionPane.showMessageDialog(this, info);
            }
        }

    }

    /**
     * Delete a datatype
     * predefined type is not allowed to be deleted
     *
     * @param e ActionEvent
     */
    public void onDeleteType(ActionEvent e)
    {
        try
        {
            // get the selected type
            String typeName = (String) listAllTypes.getSelectedValue();
            if (!AVH.isPredefinedType(typeName))
            {

                // Modal dialog with yes/no button
                int answer = JOptionPane.showConfirmDialog(this,
                    "Are you sure to delete type '" + typeName +
                    "'? The deletion can't be undone");
                if (answer != JOptionPane.YES_OPTION)
                {
                    return;
                }

                boolean suc = InfoWriter.deleteType(typeName);
                if (suc)
                {
                    JOptionPane.showMessageDialog(this, "Type '" + typeName +
                                                  "' is deleted successfully");
                    currentType.modified = false;
                    updateTypesList();
                }
                else
                {
                    JOptionPane.showMessageDialog(
                        this, "Delete type '" + typeName + "' failed! --");
                }
            }
            else
            {
                JOptionPane.showMessageDialog(
                    this,
                    "Predefined type '" + typeName + "' cannot be deleted");
            }
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * readRegisteredType
     */
    protected void updateTypesList()
    {
    	Object types[] = InfoReader.getAllType();
        Vector dbTypes = new Vector(Arrays.asList(types));
        //dbTypes.add("MIPS");
        //dbTypes.add("SCOP");
        //dbTypes.add("GO");
        types = dbTypes.toArray();
        
        if (types != null)
        {
            //final ProgressBarWin win = new ProgressBarWin(new JFrame(),
            //    "Updating available types:", 100, false, 0, types.length);
            //win.start();
        	
        	final Object types1[] = types;
            listAllTypes.setListData(types);

            Thread t = new Thread()
            {
                public void run()
                {
                	setTypeIcon(types1);
                }
            } ;
            t.start() ;
            

            //win.stop();

            listAllTypes.repaint();
        }
    }

	private void setTypeIcon(Object[] types) {
		// set icons, 2004-10-15
		typeIcon.entrySet().clear();
		// Debug.trace(this, typeIcon);
		for (int i = 0; i < types.length; i++)
		{
			ImageIcon icon = IndusConstants.iconDatatype;
		    
//					    //win.step();
//		    String supertype = IndusHttpClient.
//		        getTopSuperType(types[i].toString());
//		    //Debug.systrace(null, types[i] + ":" + supertype);
//
//		    if (AVH.isAVH( (String) types[i], supertype))
//		    {
//		        icon = IndusConstants.iconTreetype;
//		    }
//		    else if (DataType.isNumber( (String) types[i], supertype))
//		    {
//		        icon = IndusConstants.iconNumber;
//		    }
//		    else if (DataType.isString( (String) types[i], supertype))
//		    {
//		        icon = IndusConstants.iconString;
//		    }
		    typeIcon.put(types[i], icon);
		}
	}

    /**
     * Export the XML for the type
     * @param e ActionEvent
     * @author Jie Bao
     * @since 2004-10-08
     */
    public void onExport(ActionEvent e)
    {
        if (this.currentType != null)
        {
            String xml = currentType.toXML();
            String url = IndusConstants.typeBasisURL + currentType.getName() +
                ".xml";
            //Debug.trace("This XML file is also available from " + url);
            IndusBasis.showXML(xml);
        }
        else
        {
            JOptionPane.showMessageDialog(
                this, "No type is defined");
        }
    }

}
