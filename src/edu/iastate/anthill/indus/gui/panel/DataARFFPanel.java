package edu.iastate.anthill.indus.gui.panel;

import java.net.MalformedURLException;
import java.net.URL;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.iastate.utils.gui.LabelledItemPanel;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;

/**
  @author Jie Bao
 * @since 1.0 2005-03-18
 */
public class DataARFFPanel
    extends JPanel implements MessageHandler
{
    public DataARFFPanel()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    JTextField DBName = new JTextField(),
        DBMachineURL = new JTextField();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JButton btnBrowser = new JButton();

    private void jbInit() throws Exception
    {
        messageMap();

        LabelledItemPanel myContentPane = new LabelledItemPanel();
        myContentPane.setBorder(BorderFactory.createEtchedBorder());
        myContentPane.addItem("Database Name", DBName);
        myContentPane.addItem("ARFF File URL", jPanel1);

        this.setLayout(borderLayout1);
        jPanel1.setLayout(borderLayout2);
        btnBrowser.setText("Browser");
        this.add(myContentPane, java.awt.BorderLayout.CENTER);
        jPanel1.add(DBMachineURL, java.awt.BorderLayout.CENTER);
        jPanel1.add(btnBrowser, java.awt.BorderLayout.EAST);
    }

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(btnBrowser, this, "onBrowser");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void onBrowser(ActionEvent e)
    {

        JFileChooser openDialog = new JFileChooser();
        openDialog.setDialogType(JFileChooser.OPEN_DIALOG);
        openDialog.setDialogTitle("Select the arff file");
        int returnVal = openDialog.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                URL u = openDialog.getSelectedFile().toURL();
                DBMachineURL.setText(u.toString());
            }
            catch (MalformedURLException ex)
            {
            }
        }
    }
}
