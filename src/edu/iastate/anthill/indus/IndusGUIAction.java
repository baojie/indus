package edu.iastate.anthill.indus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.iastate.anthill.indus.panel.IndusPane;


import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.utils.Debug;

/**
 * GUI message handlers
 * @author Jie Bao
 * @since 1.0
 */
public class IndusGUIAction
    extends IndusGUI implements MessageHandler
{
    public IndusGUIAction()
    {
        super();
        //Debug.trace("IndusGUIAction");

        messageMap();
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(menuHelpAbout, this, "onHelpAbout");
        }
        catch (Exception ex)
        {
        }
    }

    public void onHelpAbout(ActionEvent e)
    {
        AboutBoxDialog dlg = new AboutBoxDialog(IndusConstants.infoAbout,
                                                "About " + IndusConstants.NAME);
        dlg.showAboutBox();
    }

    /**
     * prompt to save changes when exit
     * @author Jie Bao
     * @since 2004-10-12
     */
    public void onExit()
    {
        //config.save(thisAgent);

        int count = tabPanel.getTabCount();
        for (int i = 0; i < count; i++)
        {
            // Get component associated with tab
            Component comp =  tabPanel.getComponentAt(i);
            if (comp instanceof IndusPane)
                ((IndusPane)comp).promptSave();
        }
        indusSystemDB.disconnect();
        indusCacheDB.disconnect();
        indusLocalDB.disconnect();

        setVisible(false);
        System.exit(0);
    }

    private void jbInit() throws Exception
    {
        final IndusGUIAction thisAgent = this;
        config.load(thisAgent);
        mainFrame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                onExit();
            }
        });
        tabPanel.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent evt)
            {
                onChangePane(evt);
            }
        });
    }

    IndusPane lastSelectedPane = null;
    // This method is called whenever the selected tab changes
    public void onChangePane(ChangeEvent evt)
    {
        JTabbedPane pane = (JTabbedPane) evt.getSource();

        // handle the last selected pane
        if (lastSelectedPane != null)
        {
            // do some thing
            //Debug.trace("You selected " + lastSelectedPane + " -> " + pane.getSelectedIndex());
            lastSelectedPane.promptSave();
        }
        // Get current tab
        Component c= pane.getSelectedComponent();
        if (c instanceof IndusPane)
            lastSelectedPane = (IndusPane) c;
    }

}
