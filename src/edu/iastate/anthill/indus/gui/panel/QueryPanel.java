package edu.iastate.anthill.indus.gui.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import Zql.ZFromItem;
import Zql.ZQuery;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.NumericConnector;
import edu.iastate.anthill.indus.datasource.mapping.SchemaMapping;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.view.View;
import edu.iastate.anthill.indus.gui.IndusGUI;
import edu.iastate.anthill.indus.gui.query.ISQLBuilder;
import edu.iastate.anthill.indus.gui.query.SQLBuilderPane;
import edu.iastate.anthill.indus.query.SQLQueryPlanner;
import edu.iastate.anthill.indus.query.ZqlUtils;
import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.utils.lang.Serialization;

/**
 * @author Jie Bao
 * @since 1.0
 */
public class QueryPanel extends QueryPanelGUI implements MessageHandler,
        ISQLBuilder
{
    JFrame       frame         = new JFrame("Query Builder");

    ZQuery       myZQuery;

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

    public void cancel()
    {
        frame.setVisible(false);
        frame.dispose();
    }

    public void created(boolean init)
    {}

    public void finish(Object returnValue)
    {
        frame.setVisible(false);
        frame.dispose();
        //System.out.println(sql);
        //Debug.trace(sql);
        myZQuery = (ZQuery) returnValue;
        localSQL.setSqlInput(myZQuery.toString() + ";");
    }

    /**
     * jbInit
     */
    private void jbInit()
    {
        messageMap();
    }

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(btnRun, this, "onRun");
            MessageMap.mapAction(btnCreateSQL, this, "onCreateSQL");
            //MessageMap.mapAction(this.btnTranslate, this, "onTranslate");

            // 2006-06-30
            MessageMap.mapAction(btnLoad, this, "onLoad");
            MessageMap.mapAction(btnSave, this, "onSave");

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
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
            frame.addWindowListener(new WindowAdapter() {
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

    // 2006-06-30 Jie Bao
    public void onLoad(ActionEvent e)
    {
        //      save as 
        final String title = "Load query";
        final String extension = "zql";
        final String description = "INDUS query";

        String fileName = getFileName(title, extension, description, false);
        // get the ontology from the database
        if (fileName != null)
        {
            try
            {
                myZQuery = (ZQuery) Serialization.loadFromFile(fileName);
                localSQL.setSqlInput(myZQuery.toString() + ";");
            }
            catch (Exception e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    public void onRun(ActionEvent e)
    {
        if (myZQuery != null)
        {
            // ask if the user want the result in local terms
            int answer = JOptionPane.showConfirmDialog(this,
                    "Do you want the result shown in local terms?");
            boolean inLocalTerm = false;

            if (answer == JOptionPane.CANCEL_OPTION)// Jie Bao 2006-06-30
            { return; }

            if (answer == JOptionPane.YES_OPTION)
            {
                inLocalTerm = true;
            }

            // run it in a new thread
            final boolean b = inLocalTerm;
            Thread t = new Thread() {
                public void run()
                {
                    int pb = parent.statusBar.addProgressBar(true, 0, 0);
                    parent.statusBar.updateProgressBar(pb, "Running query...");

                    runQuery(b);

                    parent.statusBar.removeProgressBar(pb);
                }
            };
            t.start();
        }
        else
        {
            Debug.trace("No query to run");
        }
    }

    // run a query
    // 2005-05-23 Jie Bao - created
    // 2006-06-30 Jie Bao - modified
    private void runQuery(boolean inLocalTerm)
    {
        //Debug.trace("runQuery");
        // clear old query result
        remoteSQL.removeAll();
        resultPane.setSQL(null);
        
        // get the view name [from clause in ZQuery]
        // the query must be from only one table
        ZFromItem from = (ZFromItem) myZQuery.getFrom().elementAt(0);
        String viewName = from.getTable();

        btnRun.setEnabled(false);
        SQLQueryPlanner planner = new SQLQueryPlanner(parent.indusCacheDB.db,
                parent.indusSystemDB.db);

        Map<String, String> queries = planner.doQuery(myZQuery, viewName,
                inLocalTerm);

        // show translated queries
        for (String dataSourceName : queries.keySet())
        {
            String sql = queries.get(dataSourceName);
            model.addRow(new Object[] { dataSourceName, sql });
        }

        // show it on the GUI
        String select[] = ZqlUtils.selectList(myZQuery);
        String col = "";
        for (int i = 0; i < select.length; i++)
        {
            col += select[i] + ",";
        }
        col += View.FROM_DATA_SOURCE;

        String strSQL = "SELECT " + col + " FROM " + viewName;
        resultPane.setSQL(strSQL);

        //updateDsColumn();

        btnRun.setEnabled(true);
    }

    // 2006-06-30 Jie Bao
    public void onSave(ActionEvent e)
    {
        if (myZQuery != null)
        {
            //          save as 
            final String title = "Save query";
            final String extension = "zql";
            final String description = "INDUS Query";

            String fileName = getFileName(title, extension, description, true);

            if (fileName != null)
            {
                try
                {
                    Serialization.saveToFile(myZQuery, fileName);
                }
                catch (IOException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
    }

    static int dsOriginColIndex = -1;

    /**
     * @deprecated
     */
    class unused
    {        
        private void updateDsColumn()
        {
            // find the column index for FROM_DATA_SOURCE
            JTable table = resultPane.getTable();
            int cols = table.getColumnCount();
            for (int i = 0; i < cols; i++)
            {
                if (table.getColumnName(i).equals(View.FROM_DATA_SOURCE))
                {
                    dsOriginColIndex = i;
                    break;
                }
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

                Map attributeToSupertype = InfoReader
                        .findAttributeSupertypeMapping(localSchema); // eg produecd_at -> AVH, id -> integer
                //System.out.println(attributeToSupertype);

                // get the data source -> mapping map
                Map datasourceMapping = view.getDatasourceMapping();

                Map dsName2Mapping = new HashMap();
                Map dsName2Schema = new HashMap();

                for (Iterator it = datasourceMapping.keySet().iterator(); it
                        .hasNext();)
                {
                    String dsName = (String) it.next();

                    // find the inverse mapping to that remote data source
                    String mappingName = (String) datasourceMapping.get(dsName);
                    DataSourceMapping mapping = InfoReader
                            .readMapping(mappingName);
                    dsName2Mapping.put(dsName, mapping);

                    // find the remote schema
                    String remoteSchemaName = (String) mapping.schemaMapping.to;
                    Schema remoteSchema = InfoReader
                            .readSchema(remoteSchemaName);
                    dsName2Schema.put(dsName, remoteSchema);

                }
                //System.out.println(dsName2Mapping);
                //System.out.println(dsName2Schema);

                JTable table = resultPane.getTable();

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
                    String columnType = (String) attributeToSupertype
                            .get(columnName);
                    System.out.println(columnType);

                    if (columnType.equals("integer")
                            || columnType.equals("float"))
                    {
                        //  type = ZConstantEx.NUMBER;
                        //translateNumberColumn(columnName, i, dsName2Mapping);
                        // that should be done in QueryTranslator.translateQuery()
                    }
                    else if (columnType.equals("AVH")
                            || columnType.equals("DAG"))
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
        private void translateAVHColumn(String columnName, String avhName,
                int columnIndex, Map dsName2Mapping)
        {
            System.out.println(dsName2Mapping);

            // do the translation row by row
            JTable table = resultPane.getTable();
            int rows = table.getRowCount();

            for (int i = 0; i < rows; i++)
            {
                String dsName = (String) table.getValueAt(i, dsOriginColIndex);
                DataSourceMapping mapping = (DataSourceMapping) dsName2Mapping
                        .get(dsName);
                if (mapping != null)
                {
                    // the usable inverse mapping
                    String oldValue = (String) table.getValueAt(i, columnIndex);
                    String newValue = mapping.findAVHFirstMappedTo(avhName,
                            oldValue).toTerm;
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
            for (Iterator it = dsName2Mapping.keySet().iterator(); it.hasNext();)
            {
                String dsName = (String) it.next();
                DataSourceMapping mapping = (DataSourceMapping) dsName2Mapping
                        .get(dsName);
                SchemaMapping sMapping = mapping.schemaMapping;

                // find an applicable numeric bridge rule for each data source
                BridgeRule rules[] = sMapping.findAppliableMapping(columnName);
                NumericConnector translator = null;
                for (int i = 0; i < rules.length; i++)
                {
                    if (rules[i].connector instanceof NumericConnector)
                    {
                        translator = (NumericConnector) rules[i].connector
                                .getMirror();
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
            JTable table = resultPane.getTable();
            int rows = table.getRowCount();

            for (int i = 0; i < rows; i++)
            {
                String ds = (String) table.getValueAt(i, dsOriginColIndex);
                NumericConnector translator = (NumericConnector) ds2inverseBridge
                        .get(ds);
                if (translator != null)
                {
                    String oldValue = (String) table.getValueAt(i, columnIndex);
                    String newValue = translator.eval(oldValue);
                    System.out.println(newValue);
                    table.setValueAt(newValue, i, columnIndex);
                }
            }
        }
    }
}
