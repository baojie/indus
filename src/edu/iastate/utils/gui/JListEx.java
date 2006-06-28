/**
 * 
 */
package edu.iastate.utils.gui;

import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JList;

/**
 * Extended JList
 * 
 * @author baojie
 * @since 2006-06-28
 * 
 */
public class JListEx extends JList
{
    Vector listData;
    
    public JListEx(Vector<Object> listData)
    {
        super(listData);
        this.listData = listData;
    }
    
    public void setListData(final Vector<?> listData) {
        super.setListData(listData);
        this.listData = listData;
    }
    
    public void addElement(int pos, Object obj)
    {
        //System.out.println("JListEx.addElement()");
        listData.add(pos,obj);
        super.setListData(listData);
    }
    
    public void removeElement(Object obj)
    {
        listData.remove(obj);
        super.setListData(listData);
    }
}
