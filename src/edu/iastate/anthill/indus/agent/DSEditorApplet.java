package edu.iastate.anthill.indus.agent;

import java.awt.Container;
import javax.swing.JApplet;

import edu.iastate.anthill.indus.gui.IndusMain;

/**
 * The applet interface for INDUS Data Source Editor
 * 
 * @deprecated
 * @author Jie Bao
 * @since 1.0 2004-09-23
 */
public class DSEditorApplet
    extends JApplet
{

    IndusMain editor = null;

    /**
     * Initialize the applet
     */
    public void init()
    {
        try
        {
            jbInit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // This method is called once when the browser destroys this applet.
    // 2004-10-12
    public void destroy()
    {
        if (editor != null)
        {
            editor.onExit();
        }
    }

    /**
     * Component initialization
     *
     * @throws Exception exception
     */
    private void jbInit() throws Exception
    {
        if (IndusHttpClient.testServer())
        {
            Container content = getContentPane();
            editor = new IndusMain();
            content.add(editor);
        }
    }

    /**
     * Get Applet information
     *
     * @return String
     */
    public String getAppletInfo()
    {
        return "INDUS Data Source Editor";
    }

}
