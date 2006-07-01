package edu.iastate.anthill.indus.gui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import edu.iastate.anthill.indus.IndusConfig;
import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;
import edu.iastate.utils.gui.LabelledItemPanel;
import edu.iastate.utils.gui.StandardDialog;
import edu.iastate.utils.io.FileUtils;
import edu.iastate.utils.log.Logging;
import edu.iastate.utils.net.Browser;
import edu.iastate.utils.string.SimpleXMLParser;

/**
 * The class to store gui-independent paraments
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public class IndusBasis extends JPanel
{
    public static User user ;
    
    public IndusConfig    config        = new IndusConfig("indus.xml");

    Logging               logger        = new Logging(System
                                                .getProperty("user.dir")
                                                + "\\", "indus-log");
    public static IndusDB indusSystemDB = new IndusDB();               // to store system ontology and information
    public static IndusDB indusCacheDB  = new IndusDB();               // to store view cache

    //2005-03-27
    public static IndusDB indusLocalDB  = new IndusDB();               //to store local data sources

    public IndusBasis()
    {
        //super();
        try
        {
            UIManager.setLookAndFeel(UIManager
                    .getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //Debug.trace("IndusBasis");

            
            boolean suc = indusSystemDB.connect(IndusConstants.dbURL);
            suc = suc && indusCacheDB.connect(IndusConstants.dbCacheURL);
            suc = suc && indusLocalDB.connect(IndusConstants.dbLocalURL);
            
            // check connection to the server
            if (!suc)
            {
                JOptionPane
                        .showMessageDialog(this,
                                "Cannot connect to the indus server, program terminate");
                System.exit(0);
            }
            
            


            //Debug.trace("IndusBasis() finished");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
   

    /**
     * Show a xml string and its orgin (like a url)
     * @param xml String
     * @param sourceURL String
     */
    protected static void showXML(String xml, String origin)
    {
        // beautify the XML
        JTextArea text = new JTextArea(20, 30);
        text.setText((String) SimpleXMLParser.printXMLSkeleton(xml));
        text.setLineWrap(true);

        //        JEditorPane xmlWin = new JEditorPane("text/html", "");
        //        xmlWin.setText(xml);

        JTextField tf = new JTextField();
        tf.setText(origin);

        StandardDialog dlg = new StandardDialog();
        LabelledItemPanel myContentPane = new LabelledItemPanel();
        myContentPane.setBorder(BorderFactory.createEtchedBorder());
        myContentPane.addItem("", new JLabel(
                "Select the text and press Ctrl+C to copy"));
        myContentPane.addItem("URL", tf);
        myContentPane.addItem("XML", new JScrollPane(text,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        //        myContentPane.addItem("Tree", xmlWin);

        dlg.setContentPane(myContentPane);
        dlg.pack();
        dlg.setResizable(false);
        dlg.show();
    }

    /**
     * showXML : save the result in a temp file and show with system default viewer
     *
     * @since 2005-03-23
     */
    public static void showXML(String xml)
    {
        File f = FileUtils.writeToTempFile("indus", ".xml", xml, true);
        Browser.openInWindowsDefaultBrowser(f.getPath());
    }

    public static String getTimeStamp()
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyyMMddHHmmssS");
        return dateFormat.format(new Date());
    }

}
