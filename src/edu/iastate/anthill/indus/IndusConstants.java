package edu.iastate.anthill.indus ;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;

import javax.swing.ImageIcon;

import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.string.Zip;

public class IndusConstants
{

    private static String PROJECT_FILE =
        "edu/iastate/anthill/indus/IndusMain.class" ;
    /**
     * Format the specified time.
     *
     * @param   time  The time to format.
     * @return        A string representation of the specified time.
     */
    public static String format(long time)
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-d HH:mm:ss") ;
        return(fmt.format(new Date(time))) ;
    }

    static String getModifiedTime()
    {
        URL fileURL = null;
        try
        {
            fileURL = ClassLoader.getSystemClassLoader().getResource(
                PROJECT_FILE) ;            
            //Debug.trace(fileURL);
            File file = new File(new URI(fileURL.toString())) ;
            System.out.println(file);
            long modifiedTime = file.lastModified() ;
            return format(modifiedTime) ;
        }
        catch(Exception ex)
        {
            // try zip
            try
            {
                String file = fileURL.toString();
                file = file.replace("!/"+PROJECT_FILE, "");
                file = file.replace("jar:file:/", "");
                file = file.replace("%20", " ");
                System.out.println(file);
                
                ZipEntry e= Zip.getEntry(file, PROJECT_FILE);
                long modifiedTime = e.getTime() ;
                return format(modifiedTime) ;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //ex.printStackTrace();
            return "" ;
        }
    }

    public static String TIME = getModifiedTime();//"2006-06-26" ;//

    public static String VER = "3.0.2" ;
    public static String NAME = "Indus Environment" ;

    public static final String DSSERVER = "boole.cs.iastate.edu";//"localhost";
    //public static final String DSSERVER = "localhost" ;
    public static final String INDUS_PORT = "9090" ;
    public static final int DSPORT = 2009 ; // 2008

    public static final String HOST = "http://" + DSSERVER + ":" + INDUS_PORT +
        "/wikiont/" ;

    public static final String imageBasisURL = HOST + "images/" ;
    public static final String typeBasisURL = HOST + "type/" ;
    public static final String schemaBasisURL = HOST + "schema/" ;
    public static final String mappingBasisURL = HOST + "mapping/" ;

    // icon used in the toolbar

    // icon used in the menu

    //
    public static String SUBCLASS = "rdfs:subclassOf" ;

    public static ImageIcon loadImageIcon(String name)
    {
        return(ImageIcon)GUIUtils.loadIcon("images/" + name) ;
    }

    public static ImageIcon iconDbTree = loadImageIcon("dbtree.gif") ;
    public static ImageIcon iconDbSet = loadImageIcon("dbset.gif") ;

    public static ImageIcon iconUndo = loadImageIcon("undo.gif") ;
    public static ImageIcon iconRedo = loadImageIcon("redo.gif") ;
    public static ImageIcon iconRename = loadImageIcon("rename.gif") ;
    public static ImageIcon iconComment = loadImageIcon("comment.gif") ;
    public static ImageIcon iconAddSup = loadImageIcon("addsup.gif") ;
    public static ImageIcon iconAddSub = loadImageIcon("addsub.gif") ;
    public static ImageIcon iconDelete = loadImageIcon("delete.gif") ;
    public static ImageIcon iconDeleteSub = loadImageIcon("deletesub.gif") ;
    public static ImageIcon iconDeleteSup = loadImageIcon("deletesup.gif") ;

    // icon used inside the tree
    public static ImageIcon iconRoot = loadImageIcon("root.gif") ;
    public static ImageIcon iconPackage = loadImageIcon("package.gif") ;

    public static ImageIcon iconClass = loadImageIcon("class.gif") ;
    public static ImageIcon iconProperty = loadImageIcon("property.gif") ;
    public static ImageIcon iconInstance = loadImageIcon("instance.gif") ;
    public static ImageIcon iconAllClasses = loadImageIcon("allclass.gif") ;
    public static ImageIcon iconAllProperties = loadImageIcon("allproperty.gif") ;
    public static ImageIcon iconAllInstances = loadImageIcon("allinstance.gif") ;

    // add Jie Bao , 2004-10-15 connectors
    public static ImageIcon iconEqu = loadImageIcon("equ.gif") ;
    public static ImageIcon iconUnequ = loadImageIcon("unequ.gif") ;
    public static ImageIcon iconInto = loadImageIcon(("into.gif")) ;
    public static ImageIcon iconOnto = loadImageIcon(("onto.gif")) ;
    public static ImageIcon iconComp = loadImageIcon(("comp.gif")) ;
    public static ImageIcon iconIncomp = loadImageIcon(("incomp.gif")) ;
    public static ImageIcon iconUser = loadImageIcon(("user.gif")) ;

    // add Jie Bao 2004-10-15 icon for datatypes
    public static ImageIcon iconDatatype = loadImageIcon(("datatype.gif")) ;
    public static ImageIcon iconNumber = loadImageIcon(("number.gif")) ;
    public static ImageIcon iconString = loadImageIcon(("string.gif")) ;
    public static ImageIcon iconTreetype = loadImageIcon(("treetype.gif")) ;

    // add Jie Bao , 2004-09-30
    public static ImageIcon iconSchema = loadImageIcon(("schema.gif")) ;
    public static ImageIcon iconAVHValue = loadImageIcon("tree.gif") ;
    public static ImageIcon iconDB = loadImageIcon(("db.gif")) ;
    // add Jie Bao , 2005-05-23
    public static ImageIcon iconTable = loadImageIcon("table.gif") ;

    // icon used in the tree pane
    public static ImageIcon iconTree = loadImageIcon(("tree.gif")) ;

    public static String infoAbout = "<html>" +
        "<font color=\"#FF0099\"><b>" +
        "INDUS Data Integration Environment</b></font><br>Version " + VER +
        "<br>" + "<br><b>Jie Bao</b><br>June 2006<br>" +
        "Iowa State University<br><a href=\"mailto:baojie@iastate.edu\">" +
        "baojie@iastate.edu</a><br><a href=\"http://www.cs.iastate.edu/~baojie\">" +
        "http://www.cs.iastate.edu/~baojie</a><br>" +
        "</html>" ;

// 2005-03-11 data base setting
    public static final String dbURL =
        "jdbc:postgresql://" + DSSERVER + "/indus" ;
    public static final String dbCacheURL =
        "jdbc:postgresql://" + DSSERVER + "/indus-data" ;
    public static final String dbLocalURL =
        "jdbc:postgresql://" + DSSERVER + "/indus-local" ;
    public static final String dbUsr = "indus", dbPwd = "indus" ;
    public static final String dbDriver = "org.postgresql.Driver" ;
}
