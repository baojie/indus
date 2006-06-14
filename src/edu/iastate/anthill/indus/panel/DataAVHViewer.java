package edu.iastate.anthill.indus.panel;

import java.util.Map;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;

import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.IndusDataSource;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.datasource.type.AVHDialog;

import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.sql.DBViewer;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-27</p>
 */
public class DataAVHViewer
    extends DBViewer
{
    IndusDataSource myDataSource;
    Map att2avh;

    public DataAVHViewer(JFrame aFrame, IndusDataSource ds)
    {
        super(aFrame, true, "Data Source Viewer : " + ds.getName() + " @ " +
              ds.getUrl(), ds.getUrl(), ds.getUser(),
              ds.getPassword(), false, ds.getDriver());
        this.myDataSource = ds;
        jbInit();
    }

    /**
     * @since 2005-03-28
     */
    private void jbInit()
    {
        datasourceList.setVisible(false);

        Schema schema = InfoReader.readSchema(myDataSource.getSchemaName());
        if (schema != null)
        {
            att2avh = InfoReader.findAttributeToAVHMapping(schema);
            //System.out.println(att2avh);
        }

        dataTable.setEditable(true);

        dataTable.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                onTableMouseClick(evt);
            }

            public void mouseReleased(MouseEvent evt)
            {
            }
        });
        GUIUtils.centerWithinParent(this);
    }

    /**
     * Allow choose value from a tree if the column is an AVH attribute
     * @param evt MouseEvent
     * @author Jie Bao
     * @since 2005-03-28
     */
    private void onTableMouseClick(MouseEvent evt)
    {
        try {
			// left click to select row
			int row = dataTable.rowAtPoint(evt.getPoint());
			int col = dataTable.columnAtPoint(evt.getPoint());
			//Object value = dataTable.getValueAt(row, col);
			String value = ((String)dataTable.getValueAt(row, col)).trim();
			//System.out.println("click on - '" + value + "'");

			// get the col name
			String colName = dataTable.getColumnName(col).toLowerCase();
			// if there is an AVH for it
			//System.out.println("col - " + colName);
			AVH avh = (AVH) att2avh.get(colName);

			if (avh != null)
			{
			    //System.out.println("avh - " + avh);

			    AVHDialog dlg = new AVHDialog(avh, value, (JFrame)getOwner());
			    dlg.setSize(600, 400);
			    GUIUtils.centerWithinParent(dlg);
			    dlg.setVisible(true);
			    
			    //System.out.println("AVHDialog");

			    if (dlg.isOK)
			    {
			        dataTable.setValueAt(dlg.selectedValue, row, col);
			    }
			}
		} catch (Exception e) {			
			e.printStackTrace();
		}
    }
}
