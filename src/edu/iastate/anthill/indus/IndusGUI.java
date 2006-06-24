package edu.iastate.anthill.indus;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import edu.iastate.anthill.indus.panel.DataPanel;
import edu.iastate.anthill.indus.panel.TypePanel;
import edu.iastate.anthill.indus.panel.IndusPane;
import edu.iastate.anthill.indus.panel.MappingPanel;
import edu.iastate.anthill.indus.panel.QueryPanel;
import edu.iastate.anthill.indus.panel.SchemaPanel;
import edu.iastate.anthill.indus.panel.ViewPanel;

import edu.iastate.utils.gui.JStatusBar;
import edu.iastate.utils.Debug;

/**
 * GUI setting
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public class IndusGUI
    extends IndusBasis
{
    public IndusGUI()
    {
        super();

        //Debug.trace("IndusGUI");

        try
        {
            jbInit();
            try
            {
                UIManager.setLookAndFeel(
                    "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
            catch (Exception ex)
            {
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    private void jbInit() throws Exception
    {
        menuFile.setText("File");
        menuHelp.setText("Help");
        menuHelpAbout.setText("About");

        this.setLayout(borderLayout1);
        jMenuItem1.setText("Nothing yet");
        mainMenu.add(menuFile);
        mainMenu.add(menuHelp);
        menuHelp.add(menuHelpAbout);
        mainFrame.setJMenuBar(mainMenu);

        //toolbar.add(jButton1);
        jButton1.setText("Some buttons will be put here");
        this.add(toolbar, java.awt.BorderLayout.NORTH);

        paneOntology = new TypePanel(this, null);
        paneSchema = new SchemaPanel(this, null);
        paneMapping = new MappingPanel(this);
        paneDataSource = new DataPanel(this);
        paneView = new ViewPanel(this);
        paneQuery = new QueryPanel(this);

        tabPanel.add(paneOntology, "Ontology Editor");
        tabPanel.setSelectedComponent(paneOntology);
        tabPanel.add(paneSchema, "Schema Editor");
        tabPanel.add(paneMapping, "Mapping Editor");
        tabPanel.add(paneDataSource, "Data Editor");
        tabPanel.add(paneView, "View Editor");
        tabPanel.add(paneQuery, "Query Editor");
        this.add(tabPanel, java.awt.BorderLayout.CENTER);

        this.add(statusBar, java.awt.BorderLayout.SOUTH);

        mainFrame.getContentPane().add(this, BorderLayout.CENTER);
        menuFile.add(jMenuItem1);
    }

    public void switchToPane(IndusPane pane, String itemToShow)
    {
        tabPanel.setSelectedComponent(pane);
        pane.showDefault(itemToShow);
    }

    public JFrame mainFrame = new JFrame();

    JMenuBar mainMenu = new JMenuBar();
    JMenu menuFile = new JMenu();
    JMenu menuHelp = new JMenu();
    JMenuItem menuHelpAbout = new JMenuItem();

    JToolBar toolbar = new JToolBar();
    BorderLayout borderLayout1 = new BorderLayout();
    public JStatusBar statusBar = new JStatusBar("Ready");
    JButton jButton1 = new JButton();
    JTabbedPane tabPanel = new JTabbedPane();

    public TypePanel paneOntology;
    public SchemaPanel paneSchema;
    public MappingPanel paneMapping;
    public DataPanel paneDataSource;
    public ViewPanel paneView;
    public QueryPanel paneQuery;
    JMenuItem jMenuItem1 = new JMenuItem();

}
