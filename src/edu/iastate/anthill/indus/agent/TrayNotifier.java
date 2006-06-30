package edu.iastate.anthill.indus.agent;

import java.awt.Component;
import java.awt.Point;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.tray.AdvancedTrayIcon;
import edu.iastate.utils.tray.NativeIcon;
import edu.iastate.utils.tray.TrayEventAdapter;
import edu.iastate.utils.tray.TrayIconManager;
import edu.iastate.utils.tray.Win32Toolkit;


// need rath_awt.dll
// 2005-03-17 Jie Bao
public abstract class TrayNotifier
    extends Component
{
    TrayIconManager tray = new TrayIconManager(Win32Toolkit.getInstance());
    String imagePath, iconText, baloonTitle, baloonText;

    public TrayNotifier(String imagePath, String iconText, String baloonTitle,
                        String baloonText)
    {
        try
        {
            this.imagePath = imagePath;
            this.iconText = iconText;
            this.baloonText = baloonText;
            this.baloonTitle = baloonTitle;
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void removeTrayIcon()
    {
        tray.removeTrayIcon(trayIcon);
    }

    private void jbInit() throws Exception
    {
        registerTrayIcon();
    }

    abstract public void onDoubleClick();

    abstract public void onRightClick();

    // Popup
    public void onPopupMenu(Point p)
    {
        final JPopupMenu menu = new JPopupMenu();

        // Create and add a menu item
        JMenuItem item = new JMenuItem("Item Label");
        menu.add(item);
        menu.add(item);
        menu.add(item);
        menu.add(item);
        menu.add(item);
        menu.add(item);
        menu.add(item);

        menu.show(this.getFocusCycleRootAncestor(), p.x, p.y);
    }

    AdvancedTrayIcon trayIcon;
    public void registerTrayIcon()
    {
        ImageIcon ii = (ImageIcon) GUIUtils.loadIcon(imagePath);
        NativeIcon icon1 = new NativeIcon(ii.getImage());
        trayIcon = new AdvancedTrayIcon(icon1, iconText);
        trayIcon.setBaloonTitle(baloonTitle);
        trayIcon.setBaloonText(baloonText);
        trayIcon.setBaloonIcon(trayIcon.ICON_INFORMATION);

        tray.addTrayIcon(trayIcon, new TrayEventAdapter()
        {
            public void mouseDblClicked(Point p)
            {
                System.out.println("mouseDblClicked: " + p);
                onDoubleClick();
            }

            public void mouseLeftClicked(Point p)
            {
            }

            public void mouseRightClicked(Point p)
            {
                System.out.println("mouseRightClicked: " + p);
                //onPopupMenu(p);
            }

            public void mouseMove(Point p)
            {

            }

        });
    }
}
