package edu.iastate.anthill.indus.panel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusGUI;
import edu.iastate.anthill.indus.agent.IndusHttpClient;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.anthill.indus.datasource.DataSourceNode;
import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.datasource.mapping.Connector;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.NumericConnector;
import edu.iastate.anthill.indus.datasource.mapping.OntologyMapping;
import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;

import edu.iastate.utils.Debug;
import edu.iastate.utils.Utility;
import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.anthill.indus.reasoner.MappingReasoner;

public abstract class MappingPanelAction extends MappingPanelGUI implements
		MessageHandler {
	public MappingPanelAction(IndusGUI parent) {
		super(parent);
		try {
			localInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
    static TypedNode lastColored1, lastColored2;

	private void localInit() {
		messageMap();
	}

	/**
	 * Find the type of node, for exmaple schema node of Weather1, Weather2,
	 * type node of outlook, people,
	 * 
	 * @param node
	 *            TypedNode
	 * @return String
	 */
	protected String findNodeType(TypedNode node) {
		// DB node
		if (node.getType() == DataSourceNode.DB) {
			return node.getLocalName();
		}
		// schema node
		else if ((node instanceof DataSourceNode)
				&& (node.getType() == DataSourceNode.ATTRIBUTE)) {
			TypedNode n = node;
			while (n != null) {
				if ((n instanceof DataSourceNode)
						&& (n.getType() == DataSourceNode.DB)) {
					return ((DataSourceNode) n).getLocalName();
				}
				n = (TypedNode) n.getParent();
			}
		} else // AVH node
		{
			TypedNode n = node;
			while (n != null) {
				// Debug.trace(this, n + " " + (n instanceof DataSourceNode) + "
				// " + n.getType());
				if ((n instanceof DataSourceNode)
						&& (n.getType() == DataSourceNode.AVH)) {
					// Debug.trace(this, ((DataSourceNode) n).getDatatype());
					return ((DataSourceNode) n).getDatatype();
				}
				n = (TypedNode) n.getParent();
			}
		}
		return null;
	}

	protected boolean modified = false;

	static protected final String SCHEMA_COMMENT = "SCHEMANODE";

	static protected final String AVH_COMMENT = "AVHNODE";

	JMenuItem itemEditConnector = new JMenuItem("Edit...");

	JMenuItem itemAddInverse = new JMenuItem("Add Inverse Function...");

	protected void createTreeFromSchema(String schemaName, int whichTree) {
		DataSourceNode root = new DataSourceNode(schemaName, TypedNode.DB,
				"Schema");

		Schema schema = InfoReader.readSchema(schemaName);

		if (schema != null) {
			Map attList = schema.getAttList();

			for (Iterator it = attList.keySet().iterator(); it.hasNext();) {
				// read an attribute
				String attrname = (String) it.next();
				String typeName = (String) attList.get(attrname);

				DataSourceNode node = new DataSourceNode(attrname,
						TypedNode.ATTRIBUTE, typeName);
				root.add(node);

				// read attribute value, build a tree
				DataType dt = InfoReader.readDataType(typeName,false);
				if (dt != null && dt instanceof AVH) {
					dt.readOnly = true;

					TypedTree subtree = ((AVH) dt).getTreeAVH();
					node.add(subtree.getTop());
				} else {
					// node.add(new DefaultMutableTreeNode(typeName + typevec));
				}
			}
		} else {
			Debug.trace(this, "Schema information is not available");
		}

		if (whichTree == 1) {
			tree1 = createTree(root, jScrollPane1, 1);
		} else {
			tree2 = createTree(root, jScrollPane2, 2);
		}
	}

	abstract public TypedTree createTree(TypedNode root, JScrollPane pane,
			final int whichtree);

	/**
	 * Create new mapping
	 * 
	 * @param e
	 *            ActionEvent
	 * @since 2004-10-04
	 */
	public void onNewMapping(ActionEvent e) {
		// ask for the first schema
		// get the list of all registered schema
		String[] data = InfoReader.getAllSchema();

		String s1 = (String) JOptionPane.showInputDialog(null,
				"Choose schema 1", "Input", JOptionPane.INFORMATION_MESSAGE,
				null, data, data[0]);
		if (s1 != null) {
			Vector data_without_s1 = Utility.Array2Vector(data);
			data_without_s1.remove(s1);

			if (data_without_s1.size() == 0) {
				JOptionPane
						.showMessageDialog(this, "No more schema to select!");
				return;
			}

			String s2 = (String) JOptionPane.showInputDialog(null,
					"Choose schema 2", "Input",
					JOptionPane.INFORMATION_MESSAGE, null, data_without_s1
							.toArray(), data_without_s1.elementAt(0));
			if (s2 != null) {
				String mappingName = JOptionPane.showInputDialog(this,
						"The name of new mapping", s1 + "-" + s2);
				if (mappingName != null) // User clicked OK
				{
					// valiate "text"
					if (!mappingName.matches("[\\w\\-]+")) {
						JOptionPane.showMessageDialog(this,
								"Name is not legal!");
						return;
					}
				} else {
					return;
				}

				// Debug.trace(this,mappingName);

				String[] allMappings = InfoReader.getAllMapping();
				if (allMappings != null) {
					if (Arrays.asList(allMappings).contains(mappingName)) {
						JOptionPane.showMessageDialog(this, "Mapping '"
								+ mappingName + "' already exists");
						return;
					}

					schema1.setText(s1);
					schema2.setText(s2);
					createTreeFromSchema(s1, 1);
					createTreeFromSchema(s2, 2);

					// clear current mapping
					clearMapping();
					myMapping = new DataSourceMapping(s1, s2, mappingName);

					// update ui
					mappingFileList.addItem(myMapping.getName());
					mappingFileList.setSelectedItem(myMapping.getName());

					// save it
					save();
					modified = false;
				} else {
					JOptionPane
							.showMessageDialog(this,
									"Cannot create new mapping - reading server fails!");
				}

			}
		}
	}

	/**
	 * Add a mapping
	 * 
	 * @param e
	 *            ActionEvent
	 * @version 2004-10-02
	 */
	public void onAddMapping(ActionEvent e) {
		TypedNode n1 = (TypedNode) tree1.getLastSelectedPathComponent();
		TypedNode n2 = (TypedNode) tree2.getLastSelectedPathComponent();
		Connector conn = (Connector) mappingConnectorsList.getSelectedValue();
		// Debug.trace(this, n1 + " " + conn + " " + n2);

		Connector sel = (Connector) mappingConnectorsList.getSelectedValue();

		// numeric mapping can be allowed only for numeric types
		if (sel instanceof NumericConnector) {
			if (n1.getType() == TypedNode.ATTRIBUTE) {
				String type1 = ((DataSourceNode) n1).getDatatype();
				String type2 = ((DataSourceNode) n2).getDatatype();
				String supertype1 = IndusHttpClient.getTopSuperType(type1);
				String supertype2 = IndusHttpClient.getTopSuperType(type2);
				if (!AVH.isNumber(type1, supertype1)
						|| !AVH.isNumber(type2, supertype2)) {
					JOptionPane.showMessageDialog(this, "Bridge " + sel
							+ "can only be applied on numeric attirbutes");
					return;
				}
			}
		}

		if (n1 != null && n2 != null && conn != null) {
			if (n1.getType() == n2.getType()) {
				// Debug.trace(this, "" + n1.getType());
				// schema to schema mapping
				if (n1.getType() == DataSourceNode.ATTRIBUTE) {
					BridgeRule t = myMapping.addSchemaMappingItem(n1
							.getUserObject().toString(), conn, n2
							.getUserObject().toString());
					t.setComments(SCHEMA_COMMENT);
					mappingRuleListModel.add(0, t);
					modified = true;
				} else if (n1.getType() == DataSourceNode.AVH) // AVH to AVH
																// mapping
				{
					String AVH1 = findNodeType(n1);
					String AVH2 = findNodeType(n2);
					// Debug.trace(this, AVH1);
					// Debug.trace(this, AVH2);
					if (AVH1 != null && AVH2 != null) {
						BridgeRule t = myMapping.addAVHMappingItem(AVH1, n1
								.getUserObject().toString(), conn, AVH2, n2
								.getUserObject().toString());
						t.setComments(AVH_COMMENT);
						mappingRuleListModel.add(0, t);
						modified = true;
					}
				}
			}
		}
		this.btnSaveMapping.setEnabled(mappingRuleListModel.getSize() > 0);
	}

	/**
	 * prompt to save changes when exit
	 * 
	 * @author Jie Bao
	 * @since 2004-10-12
	 */
	public void promptSave() {
		// prompt for save
		if (modified && myMapping != null) {
			int answer = JOptionPane.showConfirmDialog(null, "Mapping '"
					+ myMapping.getName()
					+ "' is changed, do you want to update it? ");
			if (answer == JOptionPane.YES_OPTION) {
				save();
			}
		}
	}

	public void onSaveMapping(ActionEvent e) {
		save();
	}

	protected boolean save() {
		// if inverse mapping is not given for some connector, can't save
		// 2005-03-29
		int num = mappingConnectorListModel.getSize();
		for (int i = 0; i < num; i++) {
			Connector cc = (Connector) mappingConnectorListModel
					.getElementAt(i);
			if (cc.getMirror() == null) {
				String info = "Connector " + cc.name
						+ " has no inverse mapping, please add it";
				JOptionPane.showMessageDialog(this, info);
				return false;
			}
		}

		if (myMapping != null && myMapping.getName() != null) {
			boolean suc = InfoWriter.writeMapping(myMapping);
			if (suc) {
				//String selected = (String) mappingFileList.getSelectedItem();
				String info = "Mapping " + myMapping.getName()
						+ " saved successfully";
				JOptionPane.showMessageDialog(this, info);
				modified = false;
				//readRegisteredMapping(selected);
				return true;
			}
		}
		JOptionPane.showMessageDialog(this, "Saving failed!");
		return false;
	}

	/**
	 * Export the XML of this mapping
	 * 
	 * @param e
	 *            ActionEvent
	 * @since 2004-10-08
	 */
	public void onExportMapping(ActionEvent e) {
		String from = myMapping.schemaMapping.from;
		String to = myMapping.schemaMapping.to;
		String currentMapping = from + "-" + to;

		String xml = myMapping.toXML();

		if (xml != null) {
			String url = IndusConstants.mappingBasisURL + currentMapping
					+ ".xml";
			// Debug.trace("This XML file is also available from " + url);
			IndusBasis.showXML(xml);
		} else {
			JOptionPane.showMessageDialog(this, "No mapping is defined");
		}
	}

	protected void readRegisteredMapping(Object defaultSelected) {
		String oldSelected = (String) mappingFileList.getSelectedItem();

		String data[] = InfoReader.getAllMapping();
		if (data == null) {
			return;
		}

		GUIUtils.updateComboBox(mappingFileList, data);

		if (defaultSelected != null) {
			mappingFileList.setSelectedItem(defaultSelected);
		} else if (oldSelected != null) {
			mappingFileList.setSelectedItem(oldSelected);
		} else if (mappingFileList.getItemCount() > 0) {
			mappingFileList.setSelectedIndex(0);
		}

	}

	/**
	 * Update aviable mapping list from the server
	 * 
	 * @param e
	 *            ActionEvent
	 * @since 2004-10-13
	 */
	public void onUpdateMappingList(ActionEvent e) {
		readRegisteredMapping(null);
	}

	/**
	 * If the mapping is OK?
	 * 
	 * @param e
	 *            ActionEvent
	 * @since 2005-04-11
	 */
	public void onValidate(ActionEvent e) {
		MappingReasoner reasoner = new MappingReasoner();

		boolean good = reasoner.isConsistent(this.myMapping);
		String ok = "The mapping is consistent";
		String bad = "The mapping is inconsistent";

		String info = (good ? ok : bad + "\nDetails: "
				+ reasoner.badRuleInformation);
		JOptionPane.showMessageDialog(this, info);

	}

	/**
	 * Delete selected mapping
	 * 
	 * @param e
	 *            ActionEvent
	 * @since 2004-10-13
	 */
	public void onDeleteMapping(ActionEvent e) {
		try {
			// get the selected mapping
			String mappingName = myMapping.getName();

			// Modal dialog with yes/no button
			int answer = JOptionPane.showConfirmDialog(this,
					"Are you sure to delete mapping '" + mappingName
							+ "'? The deletion can't be undone");
			if (answer != JOptionPane.YES_OPTION) {
				return;
			}

			boolean suc = InfoWriter.deleteMapping(mappingName);
			if (suc) {
				JOptionPane.showMessageDialog(this, "Mapping '" + mappingName
						+ "' is deleted successfully");
				myMapping.clear();
				this.mappingRuleListModel.clear();
				modified = false;
				readRegisteredMapping(null);
			} else {
				String info = "Delete Mapping '" + mappingName + "' failed!";
				JOptionPane.showMessageDialog(this, info);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Uupdate the Mapping Rule List from myMapping
	 * 
	 * @since 2004-10-04
	 * @param map
	 *            Mapping
	 */
	protected void updateMappingRuleList(DataSourceMapping map) {
		// Debug.traceWin(this, map.toXML());
		// clear current
		mappingRuleListModel.clear();
		// build new rule set
		OntologyMapping schemaMap = map.schemaMapping;
		if (schemaMap != null) {
			// Debug.trace(this, "" + schemaMap.mapList.size());
			for (int j = 0; j < schemaMap.mapList.size(); j++) {
				BridgeRule b = (BridgeRule) schemaMap.mapList.elementAt(j);
				b.setComments(SCHEMA_COMMENT);
				mappingRuleListModel.addElement(b);
			}
		}
		// Debug.trace(this, "" + map.avhMappingList.size());
		for (int i = 0; i < map.avhMappingList.size(); i++) {
			OntologyMapping avhMap = (OntologyMapping) map.avhMappingList
					.elementAt(i);
			for (int j = 0; j < avhMap.mapList.size(); j++) {
				BridgeRule b = (BridgeRule) avhMap.mapList.elementAt(j);
				b.setComments(AVH_COMMENT);
				mappingRuleListModel.addElement(b);
			}
		}
		
		// 2006-06-15 Jie Bao: sort the loaded mapping rules		
		GUIUtils.sortJList(mappingRuleList);
	}

	/**
	 * loadMapping
	 * 
	 * @param item
	 *            String
	 * @since 2003-10-03
	 */
	protected void loadMapping(String item) {
		// read information from the server
		DataSourceMapping m = InfoReader.readMapping(item);
		// Debug.traceWin(this,textXML);

		if (m != null) {
			clearMapping();
			// 1 parse it
			myMapping = m;
			// Debug.traceWin(this, myMapping.toXML());
			// 2 update GUI

			// 2.1 update schema list
			// Debug.trace(this, myMapping.toString());
			String from = myMapping.schemaMapping.from;
			String to = myMapping.schemaMapping.to;
			schema1.setText(from);
			schema2.setText(to);

			createTreeFromSchema(from, 1);
			createTreeFromSchema(to, 2);

			refreshBtn1.setEnabled(true);
			refreshBtn2.setEnabled(true);

			// 2.2 update mapping rule list
			updateMappingRuleList(myMapping);

			// 2.3 load user connectors
			updateConnectorList(myMapping);

			modified = false;
		} else {
			Debug.trace(this, "Mapping '" + item
					+ "' information is not available");
		}
	}

	/**
	 * upateConnectorList - load user defined connectors
	 * 
	 * @param myMapping
	 *            Mapping
	 * @since 2004-10-16
	 */
	private void updateConnectorList(DataSourceMapping myMapping) {
		mappingConnectorListModel.removeAllElements();

		int len = myMapping.defaultConnectors.length;
		for (int i = 0; i < len; i++) {
			mappingConnectorListModel.add(i, myMapping.defaultConnectors[i]);
		}

		Vector vec = myMapping.getUserConnectors();
		// Debug.trace(this, vec);
		for (int j = len; j < len + vec.size(); j++) {
			mappingConnectorListModel.add(j, vec.elementAt(j - len));
		}
		mappingConnectorsList.setSelectedIndex(0);

		// popup menu

		final JPopupMenu menu = new JPopupMenu();
		menu.add(itemEditConnector);
		menu.add(this.itemAddInverse);

		// Set the component to show the popup menu
		mappingConnectorsList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				showMenu(evt);
			}

			public void mouseReleased(MouseEvent evt) {
				showMenu(evt);
			}

			void showMenu(MouseEvent evt) {
				int row = mappingConnectorsList.locationToIndex(evt.getPoint());
				mappingConnectorsList.setSelectedIndex(row);

				if (evt.isPopupTrigger()
						&& !(mappingConnectorsList.getSelectedValue() instanceof SimpleConnector)) {
					menu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
		});
	}

	public void onEditConnector(ActionEvent e) {
		NumericConnector t = (NumericConnector) mappingConnectorsList
				.getSelectedValue();
		MappingRuleDialog dlg = new MappingRuleDialog(t);
		dlg.setSize(500, 300);
		dlg.nameField.setEnabled(false);
		// dlg.exprField.setEnabled(false);
		dlg.show();

		if (dlg.action == JOptionPane.YES_OPTION) {
			t.expression = dlg.expr;

			modified = true;
			btnSaveMapping.setEnabled(true);

		}
	}

	// 2004-10-16
	public void onRefresh1(ActionEvent e) {
		createTreeFromSchema(schema1.getText(), 1);
	}

	// 2004-10-16
	public void onRefresh2(ActionEvent e) {
		createTreeFromSchema(schema2.getText(), 2);
	}

	// 2004-10-16
	public void onNewConnector(ActionEvent e) {
		NumericConnector t = null;
		while (true) {
			MappingRuleDialog dlg = new MappingRuleDialog(t);
			dlg.setSize(500, 300);
			dlg.show();

			// Debug.trace(this, ""+(dlg.action == JOptionPane.YES_OPTION));
			if (dlg.action == JOptionPane.YES_OPTION) {
				t = new NumericConnector(dlg.exprname.trim(), dlg.expr);
				// if the name is used
				Vector existing = new Vector();
				for (int i = 0; i < mappingConnectorListModel.size(); i++) {
					existing.add(mappingConnectorListModel.elementAt(i)
							.toString());
				}
				// Debug.trace(existing);
				// Debug.trace(dlg.exprname);
				if (existing.contains(dlg.exprname)) {
					JOptionPane.showMessageDialog(this, "Name is used!");
				} else {
					// Debug.trace(this, dlg.exprname + " " + dlg.expr);
					mappingConnectorListModel.add(mappingConnectorListModel
							.getSize(), t);

					String info = "Connector "
							+ t.name
							+ " is created successfully"
							+ "\nHowever, it will be stored in the mapping file only if it is used by some mapping rules";
					JOptionPane.showMessageDialog(this, info);
					// modified = true;
					return;
				}
			} else {
				return;
			}
		}
	}

	/**
	 * Add inverse function of a numeric connector
	 * 
	 * @param e
	 *            ActionEvent
	 * @since 2005-03-28
	 */
	public void onAddInverse(ActionEvent e) {
		NumericConnector t = (NumericConnector) mappingConnectorsList
				.getSelectedValue();
		NumericConnector inverse = new NumericConnector("Inverse-" + t.name,
				t.inverseExpression);

		MappingRuleDialog dlg = new MappingRuleDialog(inverse);

		dlg.setSize(500, 300);
		dlg.nameField.setEnabled(false);
		// dlg.exprField.setEnabled(false);
		dlg.setVisible(true);

		if (dlg.action == JOptionPane.YES_OPTION) {
			t.inverseExpression = dlg.expr;

			modified = true;
			btnSaveMapping.setEnabled(true);

		}
	}

	/**
	 * Change selections on the two trees when selected a mapping rule
	 * 
	 * @author Jie Bao
	 * @since 2006-06-14
	 * @see onAddMapping
	 * 
	 * @param evt
	 */
	public void onSelectedRuleChanged(ListSelectionEvent evt) {
		// When the user release the mouse button and completes the selection,
		// getValueIsAdjusting() becomes false
		if (!evt.getValueIsAdjusting()) {
			JList list = (JList) evt.getSource();
			final BridgeRule currentSelected = (BridgeRule) list
					.getSelectedValue();

			// Get selected item
			if (currentSelected != null) {
				// Debug.trace(currentSelected);
				
				// change the selected connector
				Connector c = currentSelected.connector;
				
				for (int i = 0; i < mappingConnectorListModel.size(); i++) {
					Connector obj = (Connector) mappingConnectorListModel.elementAt(i);
					if (c.equals(obj))
					{
						//System.out.println(obj);
						mappingConnectorsList.setSelectedValue(obj, true);
						break;
					}
				}
				
				// change the selected term on tree1
				String term1 = currentSelected.fromTerm;
				String type = currentSelected.fromTerminology;
                if (term1 != null && type != null)
                {
                    TypedNode marked1 = markNode(tree1, type, term1);
                    //Debug.systrace(this, "Tree2 marks " + mappedTo);
                    if (marked1 != null)
                    {
                    	if (lastColored1!= null)
                    		lastColored1.setColor(Color.black);
                        lastColored1 = marked1;
                    }
                }
                
				// change the selected term on tree2
				String term2 = currentSelected.toTerm;
				String type2 = currentSelected.toTerminology;
                if (term2 != null && type2 != null)
                {
                    TypedNode marked2 = markNode(tree2, type2, term2);
                    //Debug.systrace(this, "Tree2 marks " + mappedTo);
                    if (marked2 != null)
                    {
                    	if (lastColored2!= null)
                    		lastColored2.setColor(Color.black);
                        lastColored2 = marked2;
                    }
                }
			}
		}
	}
	
    /**
     * Mark node with blue
     * @param t Tree
     * @param node String
     * @return TypedNode - the node actually marked
     * @author Jie Bao
     * @since 2004-10-13
     */
    TypedNode markNode(TypedTree t, String dataType, String nodeToFind)
    {
        if (nodeToFind != null)
        {
            TypedNode n = (TypedNode) TypedTree.findFirst(t, nodeToFind);
            String findType = dataType;

            if (n != null)
            {
                if (n.getType() == DataSourceNode.AVH)
                {
                    findType = findNodeType(n);
                }
                if (dataType.equals(findType))
                {
                    n.setColor(Color.blue);
                    t.expandNode(n);
                }
                return n;
            }
        }
        return null;
    }

	public void messageMap() {
		// button handler
		try {
			MessageMap.mapAction(this.btnSaveMapping, this, "onSaveMapping");
			MessageMap.mapAction(this.addBtn, this, "onAddMapping");
			MessageMap.mapAction(this.newBtn, this, "onNewMapping");
			MessageMap.mapAction(this.exportBtn, this, "onExportMapping");
			MessageMap.mapAction(this.deleteBtn, this, "onDeleteMapping");
			MessageMap.mapAction(this.btnUpdateMappingList, this,
					"onUpdateMappingList");
			MessageMap.mapAction(this.refreshBtn1, this, "onRefresh1");
			MessageMap.mapAction(this.refreshBtn2, this, "onRefresh2");
			MessageMap.mapAction(this.newConnectorBtn, this, "onNewConnector");
			MessageMap.mapAction(this.itemEditConnector, this,
					"onEditConnector");
			MessageMap.mapAction(this.itemAddInverse, this, "onAddInverse");
			MessageMap.mapAction(this.btnValidate, this, "onValidate");

			// 2006-06-14 Jie Bao, change tree selections
			mappingRuleList
					.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							onSelectedRuleChanged(e);
						}
					});
		} catch (Exception ex) {
		}
	}
}
