package edu.iastate.anthill.indus;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.iastate.anthill.indus.agent.IndusHttpClient;

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
public class IndusBasis
    extends JPanel
{
    IndusConfig config = new IndusConfig("indus.xml");

    Logging logger = new Logging(System.getProperty("user.dir") + "\\",
                                 "indus-log");
    public IndusDB indusSystemDB = new IndusDB(); // to store system ontology and information
    public IndusDB indusCacheDB = new IndusDB(); // to store view cache

    //2005-03-27
    public IndusDB indusLocalDB = new IndusDB(); //to store local data sources

    public IndusBasis()
    {
        //super();

        //Debug.trace("IndusBasis");

        if (!IndusHttpClient.testServer())
        {
            JOptionPane.showMessageDialog(this,
                                          "Cannot connect to the indus server, program terminate");
            System.exit(0);
        }
        indusSystemDB.connect(IndusConstants.dbURL);
        indusCacheDB.connect(IndusConstants.dbCacheURL);
        indusLocalDB.connect(IndusConstants.dbLocalURL);

        //Debug.trace("IndusBasis() finished");
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
        text.setText( (String) SimpleXMLParser.printXMLSkeleton(xml));
        text.setLineWrap(true);

//        JEditorPane xmlWin = new JEditorPane("text/html", "");
//        xmlWin.setText(xml);

        JTextField tf = new JTextField();
        tf.setText(origin);

        StandardDialog dlg = new StandardDialog();
        LabelledItemPanel myContentPane = new LabelledItemPanel();
        myContentPane.setBorder(BorderFactory.createEtchedBorder());
        myContentPane.addItem("",
                              new JLabel(
                                  "Select the text and press Ctrl+C to copy"));
        myContentPane.addItem("URL", tf);
        myContentPane.addItem("XML",
                              new JScrollPane(text,
                                              JScrollPane.
                                              VERTICAL_SCROLLBAR_ALWAYS,
                                              JScrollPane.
                                              HORIZONTAL_SCROLLBAR_NEVER));
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
        File f = FileUtils.writeToTempFile("indus", ".xml",
                                           xml, true);
        Browser.openInWindowsDefaultBrowser(f.getPath());
    }
}
