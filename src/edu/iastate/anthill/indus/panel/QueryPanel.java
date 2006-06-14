package edu.iastate.anthill.indus.panel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import edu.iastate.anthill.indus.IndusGUI;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.NumericConnector;
import edu.iastate.anthill.indus.datasource.mapping.SchemaMapping;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.view.View;
import edu.iastate.anthill.indus.panel.query.ISQLBuilder;
import edu.iastate.anthill.indus.panel.query.SQLBuilderPane;
import edu.iastate.anthill.indus.query.SQLQueryBuilder;
import edu.iastate.anthill.indus.query.SQLQueryPlanner;

import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;

import Zql.ZFromItem;
import Zql.ZQuery;

/**
 * @author Jie Bao
 * @since 1.0
 */
public class QueryPanel
    extends QueryPanelGUI implements MessageHandler, ISQLBuilder
{
    BorderLayout borderLayout1 = new BorderLayout();

    public QueryPanel(IndusGUI parent)
    {
        super();
        try
        {
            this.parent = parent;
            jbInit();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * jbInit
     */
    private void jbInit()
    {
        messageMap();
        btnTranslate.setEnabled(false);
    }

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(btnRun, this, "onRun");
            MessageMap.mapAction(btnCreateSQL, this, "onCreateSQL");
            MessageMap.mapAction(this.btnTranslate, this, "onTranslate");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Translate query result into local terms
     *   NOTE: this function is not used right now
     *         the inverse mapping is done in query translator
     * @param e ActionEvent
     * @since 2005-03-28
     */
    public void onTranslate(ActionEvent e)
    {
        if (myZQuery != null)
        {
            // get the view name [from clause in ZQuery]
            // the query must be from only one table
            ZFromItem from = (ZFromItem) myZQuery.getFrom().elementAt(0);
            String viewName = from.getTable();
            View view = InfoReader.readView(viewName);
            //System.out.println(view);

            // the local schema
            String schemaName = view.getLocalSchemaName();
            Schema localSchema = InfoReader.readSchema(schemaName);
            //System.out.println(localSchema);

            Map attributeToSupertype = InfoReader.findAttributeSupertypeMapping(
                localSchema); // eg produecd_at -> AVH, id -> integer
            //System.out.println(attributeToSupertype);

            // get the data source -> mapping map
            Map datasourceMapping = view.getDatasourceMapping();

            Map dsName2Mapping = new HashMap();
            Map dsName2Schema = new HashMap();

            for (Iterator it = datasourceMapping.keySet().iterator();
                 it.hasNext(); )
            {
                String dsName = (String) it.next();

                // find the inverse mapping to that remote data source
                String mappingName = (String) datasourceMapping.get(dsName);
                DataSourceMapping mapping = InfoReader.readMapping(mappingName);
                dsName2Mapping.put(dsName, mapping);

                // find the remote schema
                String remoteSchemaName = (String) mapping.schemaMapping.to;
                Schema remoteSchema = InfoReader.readSchema(remoteSchemaName);
                dsName2Schema.put(dsName, remoteSchema);

            }
            //System.out.println(dsName2Mapping);
            //System.out.println(dsName2Schema);

            JTable table = this.dbPanel.getTable();

            int cols = table.getColumnCount();

            for (int i = 0; i < cols; i++)
            {
                // column name
                String columnName = table.getColumnName(i);
                if (columnName.equals(View.FROM_DATA_SOURCE))
                {
                    continue;
                }

                System.out.println(columnName);
                // column type
                String columnType = (String) attributeToSupertype.get(
                    columnName);
                System.out.println(columnType);

                if (columnType.equals("integer") || columnType.equals("float"))
                {
                    //  type = ZConstantEx.NUMBER;
                    //translateNumberColumn(columnName, i, dsName2Mapping);
                    // that should be done in QueryTranslator.translateQuery()
                }
                else if (columnType.equals("AVH") || columnType.equals("DAG"))
                {
                    //   type = ZConstantEx.AVH;
                    //translateAVHColumn(columnName,  localSchema.getType(columnName),  i, dsName2Mapping);
                }
                else
                {
                    // string, boolean, date , Do nothing!
                }
            }

            // the mapping
        }
    }

    /**
     * translateAVHColumn
     */
    private void translateAVHColumn(String columnName,
                                    String avhName,
                                    int columnIndex,
                                    Map dsName2Mapping)
    {
        System.out.println(dsName2Mapping);

// do the translation row by row
        JTable table = this.dbPanel.getTable();
        int rows = table.getRowCount();

        for (int i = 0; i < rows; i++)
        {
            String dsName = (String) table.getValueAt(i, this.dsOriginColIndex);
            DataSourceMapping mapping = (DataSourceMapping) dsName2Mapping.get(
                dsName);
            if (mapping != null)
            {
                // the usable inverse mapping
                String oldValue = (String) table.getValueAt(i, columnIndex);
                String newValue = mapping.findAVHFirstMappedTo(avhName,
                    oldValue, true);
                System.out.println(newValue);
                table.setValueAt(newValue, i, columnIndex);
            }
        }

    }

    /**
     * translateNumberColumn
     */
    private void translateNumberColumn(String columnName, int columnIndex,
                                       Map dsName2Mapping)
    {
        System.out.println(dsName2Mapping);

        // collect needed information
        Map ds2inverseBridge = new HashMap();
        for (Iterator it = dsName2Mapping.keySet().iterator();
             it.hasNext(); )
        {
            String dsName = (String) it.next();
            DataSourceMapping mapping = (DataSourceMapping) dsName2Mapping.get(
                dsName);
            SchemaMapping sMapping = mapping.schemaMapping;

            // find an applicable numeric bridge rule for each data source
            BridgeRule rules[] = sMapping.findAppliableMapping(columnName);
            NumericConnector translator = null;
            for (int i = 0; i < rules.length; i++)
            {
                if (rules[i].connector instanceof NumericConnector)
                {
                    translator = (NumericConnector) rules[i].connector.
                        getMirror();
                    break;
                }
            }

            if (translator != null)
            {
                ds2inverseBridge.put(dsName, translator);
                System.out.println(translator);
            }
        }

        // do the translation row by row
        JTable table = this.dbPanel.getTable();
        int rows = table.getRowCount();

        for (int i = 0; i < rows; i++)
        {
            String ds = (String) table.getValueAt(i, this.dsOriginColIndex);
            NumericConnector translator = (NumericConnector) ds2inverseBridge.
                get(ds);
            if (translator != null)
            {
                String oldValue = (String) table.getValueAt(i, columnIndex);
                String newValue = translator.eval(oldValue);
                System.out.println(newValue);
                table.setValueAt(newValue, i, columnIndex);
            }
        }

    }

    /**
     *
     * @param e ActionEvent
     * @since 2005-03-23
     */
    public void onCreateSQL(ActionEvent e)
    {
        try
        {
            final SQLBuilderPane sqlBuilder = new SQLBuilderPane(this,
                parent.indusCacheDB.db);
            frame = new JFrame("Query Builder");
            frame.getContentPane().add(sqlBuilder);
            frame.addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent evt)
                {
                    sqlBuilder.cancel();
                }
            });
            frame.setSize(600, 400);
            GUIUtils.centerWithinScreen(frame);
            frame.setVisible(true);
        }
        catch (Exception sqle)
        {
            sqle.printStackTrace();
        }
    }

    JFrame frame = new JFrame("Query Builder");

    public void onRun(ActionEvent e)
    {
        if (myZQuery != null)
        {
            // get the view name [from clause in ZQuery]
            // the query must be from only one table
            ZFromItem from = (ZFromItem) myZQuery.getFrom().elementAt(0);
            String viewName = from.getTable();

            // ask if the user want the result in local terms
            int answer = JOptionPane.showConfirmDialog(this,
                "Do you want the result shown in local terms?");
            boolean inLocalTerm = false;
            if (answer == JOptionPane.YES_OPTION)
            {
                inLocalTerm = true; ;
            }

            // run it
            SQLQueryPlanner planner = new SQLQueryPlanner(parent.indusCacheDB.db,
                parent.indusSystemDB.db);
            planner.doQuery(myZQuery, viewName, inLocalTerm);

            // show it on the GUI
            String select[] = SQLQueryBuilder.selectList(myZQuery);
            String col = "";
            for (int i = 0; i < select.length; i++)
            {
                col += select[i] + ",";
            }
            col += View.FROM_DATA_SOURCE;

            String strSQL = "SELECT " + col + " FROM " + viewName;
            dbPanel.setSQL(strSQL);

            // find the column index for FROM_DATA_SOURCE
            JTable table = this.dbPanel.getTable();
            int cols = table.getColumnCount();
            for (int i = 0; i < cols; i++)
            {
                if (table.getColumnName(i).equals(View.FROM_DATA_SOURCE))
                {
                    dsOriginColIndex = i;
                    break;
                }
            }

            btnTranslate.setEnabled(true);
        }
    }

    static int dsOriginColIndex = -1;

    public void created(boolean init)
    {
    }

    public void cancel()
    {
        frame.setVisible(false);
        frame.dispose();
    }

    public void finish(Object returnValue)
    {
        frame.setVisible(false);
        frame.dispose();
        //System.out.println(sql);
        //Debug.trace(sql);
        myZQuery = (ZQuery) returnValue;
        sqlInputArea.setSqlInput(myZQuery.toString() + ";");
        btnTranslate.setEnabled(false);
    }

    ZQuery myZQuery;

}
