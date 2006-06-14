package edu.iastate.anthill.indus;

import java.io.IOException;

import javax.swing.JFrame;

import edu.iastate.utils.Console;
import edu.iastate.utils.gui.GUIUtils;

/**
 * The Main INDUS run class
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public class IndusMain
    extends IndusGUIAction
{
    public IndusMain()
    {
        super();
        //Debug.trace("IndusMain");
    }

    void start()
    {
        mainFrame.setTitle(IndusConstants.NAME + " " +
                           IndusConstants.VER + ", build " +
                           IndusConstants.TIME);

        GUIUtils.maximize(mainFrame);
        mainFrame.setVisible(true);

        mainFrame.show();
    }

    static void showConsole()
    {
        try
        {
            final Console output = new Console();
            output.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        catch (IOException e)
        {
        }
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
