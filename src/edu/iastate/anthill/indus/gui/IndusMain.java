package edu.iastate.anthill.indus.gui;

import java.io.IOException;

import javax.swing.JFrame;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.utils.Console;
import edu.iastate.utils.gui.GUIUtils;

/**
 * The Main INDUS run class
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public class IndusMain extends IndusGUIAction
{
    static public IndusMain theInstance;

    public IndusMain()
    {
        super();
        theInstance = this;
        //Debug.trace("IndusMain");
    }

    /**
     * 
     * @return
     * 
     * @author Jie Bao
     * @since 2006-07-01, imported from COBEditor
     */
    public User askForUserName()
    {
        LoginPanel p = new LoginPanel(indusSystemDB.db, user);
        p.showDlg();

        if (p.ok)
        {
            // if is the same id
            if (user != null && p.getUser().equals(user.name)) { return user; }

            User newUser = new User(indusSystemDB.db, p.getUser());
            //System.out.println(newUser);
            return newUser;
        }
        else
        {
            return null;
        }
    }

    void start()
    {
        User newUser = askForUserName();
        if (newUser == null)
        {
            this.onExit();
        }
        updateUser(newUser);
        updateTitle();

        GUIUtils.maximize(mainFrame);

        mainFrame.setVisible(true);

        loadAllPanels();

        //to work aroun a bug of JSplitPane
        //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4182558   
        // Jie Bao 2006-06-24
        mainFrame.validate();

    }

    // 2005-08-25
    public void updateUser(User newUser)
    {
        if (this.user == newUser) { return; }

        //report the login  of the new user
        if (newUser != null)
        {
        }
        this.user = newUser;
    }

    public void updateTitle()
    {
        mainFrame.setTitle(IndusConstants.NAME + " " + IndusConstants.VER
            + ", build " + IndusConstants.TIME + ", User: " + user);
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
