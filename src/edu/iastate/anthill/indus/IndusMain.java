package edu.iastate.anthill.indus;

import java.io.IOException;

import javax.swing.JFrame;

import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.utils.Console;
import edu.iastate.utils.gui.GUIUtils;

/**
 * The Main INDUS run class
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public class IndusMain extends IndusGUIAction
{
    private static final long serialVersionUID = 3970622403461846063L;

    static public IndusMain   theInstance;

    public IndusMain()
    {
        super();
        theInstance = this;
        //Debug.trace("IndusMain");
    }

    void start()
    {
        mainFrame.setTitle(IndusConstants.NAME + " " + IndusConstants.VER
                + ", build " + IndusConstants.TIME);

        GUIUtils.maximize(mainFrame);

        mainFrame.setVisible(true);

        loadAllPanels();

        //to work aroun a bug of JSplitPane
        //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4182558   
        // Jie Bao 2006-06-24
        mainFrame.validate();        

    }

    static void showConsole()
    {
        try
        {
            final Console output = new Console();
            output.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        catch (IOException e)
        {}
    }
    
    public static void main(String[] args)
    {
        try
        {
            //showConsole();
            IndusMain indusmain = new IndusMain();
            indusmain.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
