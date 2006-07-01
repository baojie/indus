package edu.iastate.anthill.indus.gui.query;

import java.util.HashMap;
import java.util.Map;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.datasource.type.AVHDialog;
import edu.iastate.anthill.indus.query.ZqlUtils;
import edu.iastate.anthill.indus.query.ZConstantEx;

import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;

import Zql.ZExpression;

/**
 * @author Jie Bao
 * @since 2005-03-23
 */
public class WhereAtomPane
    extends JPanel implements MessageHandler
{
    public static final String[] comboInit =
        {
        "=", "!=", ">", ">=", "<", "<="};

    final WhereAtomPane thisPane = this;

    public WhereAtomPane()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected JComboBox operator = new JComboBox(comboInit);
    protected JComboBox field = new JComboBox();
    protected JTextField value = new JTextField();
    protected Map att2avh = new HashMap(); // String -> AVH
    protected Map att2type = new HashMap(); // String -> String
    private final String DEFAULT_STRING = "[Select from AVH]";
    protected JButton btnValue = new JButton(DEFAULT_STRING);

    GridLayout gridLayout1 = new GridLayout();

    boolean AVHmode;

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(this.btnValue, this, "onTreeValue");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void onTreeValue(ActionEvent e)
    {
        // current select attribute
        String att = field.getSelectedItem().toString();
        if (att == null)
        {
            return;
        }
        AVH avh = (AVH) att2avh.get(att);

        // current select value
        String selectedValue = btnValue.getText();
        if (DEFAULT_STRING.equals(selectedValue))
        {
            selectedValue = null;
        }

        if (avh != null)
        {
            JFrame frame = (JFrame) SwingUtilities.getRoot(this);

            AVHDialog dlg = new AVHDialog(avh, selectedValue, frame);
            dlg.setSize(600, 400);
            dlg.setVisible(true);

            if (dlg.isOK)
            {
                btnValue.setText(dlg.selectedValue.toString());
                value.setText(dlg.selectedValue.toString());
            }
        }
    }

    private void jbInit() throws Exception
    {
        messageMap();

        operator.setSelectedItem("<=");
        this.setLayout(gridLayout1);

        field = new JComboBox();
        field.addItemListener(new FieldListener());

    }

    // 2005-03-26
    void addComponent()
    {
        this.removeAll();
        this.add(field);
        this.add(operator);
        if (AVHmode)
        {
            this.add(btnValue);
        }
        else
        {
            this.add(value);
        }
        this.revalidate();
        this.repaint();
    }

    // 2005-03-26
    class FieldListener
        implements ItemListener
    {
        // This method is called only if a new item has been selected.
        public void itemStateChanged(ItemEvent evt)
        {
            // Get the affected item
            Object item = evt.getItem();

            value.setText(null);
            btnValue.setText(DEFAULT_STRING);

            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                // Item was just selected
                AVH avh = (AVH) att2avh.get(item.toString());
                //System.out.println(att2avh);
                //Debug.trace(item + " is a tree: " + (avh != null));
                AVHmode = (avh != null);
                addComponent();
            }
        }
    }

    public ZExpression getExp()
    {
        Object op1 = (Object) field.getSelectedItem();
        String op = (String) operator.getSelectedItem();
        //String op2tree = btnValue.getText();
        //&& op2tree!=null &&   !DEFAULT_STRING.equals(op2tree)
        if (op1 != null && op != null)
        {
            String op2 = null; ;
            if (!AVHmode)
            {
                op2 = value.getText();
            }
            else
            {
                op2 = btnValue.getText();
                if (op2 == null)
                {
                    op2 = "";
                }
                if (DEFAULT_STRING.equals(op2) || op2.length() == 0)
                {
                    op2 = null;
                }
            }
            if (op2 != null)
            {
                String supertype = (String) att2type.get(op1.toString());
                // "integer;float;string;boolean;AVH";
                int type = ZConstantEx.STRING;
                if (supertype.equals("integer") || supertype.equals("float"))
                {
                    type = ZConstantEx.NUMBER;
                }
                else if (supertype.equals("AVH") || supertype.equals("DAG"))
                {
                    type = ZConstantEx.AVH;
                }

                return ZqlUtils.buildAttributeValuePair(op1.toString(), op,
                    op2, type);
            }
        }
        return null;
    }

    public void setExp(ZExpression newExp)
    {
        String op = newExp.getOperator();
        String op1 = ( (ZConstantEx) newExp.getOperand(0)).getValue();
        String op2 = ( (ZConstantEx) newExp.getOperand(1)).getValue();
        field.setSelectedItem(op1);
        operator.setSelectedItem(op);
        value.setText(op2);
        btnValue.setText(op2);
    }

    public void setAtt2avh(Map att2avh)
    {
        this.att2avh = att2avh;
        //Debug.trace(att2avh);
    }

    public void setAtt2type(Map att2type)
    {
        this.att2type = att2type;
        //Debug.trace(att2type);
    }
}
