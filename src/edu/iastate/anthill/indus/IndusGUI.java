package edu.iastate.anthill.indus;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.panel.DataPanel;
import edu.iastate.anthill.indus.panel.IndusPane;
import edu.iastate.anthill.indus.panel.MappingPanel;
import edu.iastate.anthill.indus.panel.QueryPanel;
import edu.iastate.anthill.indus.panel.SchemaPanel;
import edu.iastate.anthill.indus.panel.TypePanel;
import edu.iastate.anthill.indus.panel.ViewPanel;
import edu.iastate.utils.gui.JStatusBar;

/**
 * GUI setting
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public class IndusGUI extends IndusBasis
{
    public IndusGUI()
    {
        super();

        //Debug.trace("IndusGUI");

        try
        {
            jbInit();
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public JFrame       mainFrame     = new JFrame();

    JMenuBar            mainMenu      = new JMenuBar();
    JMenu               menuFile      = new JMenu();
    JMenu               menuHelp      = new JMenu();
    JMenuItem           menuHelpAbout = new JMenuItem();

    //JToolBar            toolbar       = new JToolBar();
    BorderLayout        borderLayout1 = new BorderLayout();
    public JStatusBar   statusBar     = new JStatusBar("Ready");
    JButton             jButton1      = new JButton();
    JTabbedPane         tabPanel      = new JTabbedPane();

    public TypePanel    paneOntology;
    public SchemaPanel  paneSchema;
    public MappingPanel paneMapping;
    public DataPanel    paneDataSource;
    public ViewPanel    paneView;
    public QueryPanel   paneQuery;
    JMenuItem           jMenuItem1    = new JMenuItem();

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
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //toolbar.add(jButton1);
        //jButton1.setText("About");
        //this.add(toolbar, java.awt.BorderLayout.NORTH);

        //loadAllPanels();

        this.add(statusBar, java.awt.BorderLayout.SOUTH);

        mainFrame.getContentPane().add(this, BorderLayout.CENTER);
        menuFile.add(jMenuItem1);
    }

    protected void loadAllPanels()
    {
        this.add(tabPanel, BorderLayout.CENTER);
        tabPanel.add(new JPanel(), "Ontology Editor");
        tabPanel.add(new JPanel(), "Schema Editor");
        tabPanel.add(new JPanel(), "Mapping Editor");
        tabPanel.add(new JPanel(), "Data Editor");
        tabPanel.add(new JPanel(), "View Editor");
        tabPanel.add(new JPanel(), "Query Editor");

        final IndusGUI p = this;
        Thread t = new Thread() {
            public void run()
            {
                paneOntology = new TypePanel(p, null);
                tabPanel.remove(0);
                tabPanel.add(paneOntology, "Ontology Editor", 0);
                tabPanel.setSelectedComponent(paneOntology);

                paneOntology.listAllTypes.invalidate();

            }
        };
        t.start();

        t = new Thread() {
            public void run()
            {
                paneSchema = new SchemaPanel(p, null);
                tabPanel.remove(1);
                tabPanel.add(paneSchema, "Schema Editor", 1);
            }
        };
        t.start();

        t = new Thread() {
            public void run()
            {
                paneMapping = new MappingPanel(p);
                tabPanel.remove(2);
                tabPanel.add(paneMapping, "Mapping Editor", 2);
                paneMapping.resetPanel();
            }
        };
        t.start();

        t = new Thread() {
            public void run()
            {
                paneDataSource = new DataPanel(p);
                tabPanel.remove(3);
                tabPanel.add(paneDataSource, "Data Editor", 3);
            }
        };
        t.start();

        t = new Thread() {
            public void run()
            {
                paneView = new ViewPanel(p);
                tabPanel.remove(4);
                tabPanel.add(paneView, "View Editor", 4);
            }
        };
        t.start();

        t = new Thread() {
            public void run()
            {
                paneQuery = new QueryPanel(p);
                tabPanel.remove(5);
                tabPanel.add(paneQuery, "Query Editor", 5);
            }
        };
        t.start();

    }

    public void switchToPane(IndusPane pane, String itemToShow)
    {
        tabPanel.setSelectedComponent(pane);
        pane.showDefault(itemToShow);
    }

}
