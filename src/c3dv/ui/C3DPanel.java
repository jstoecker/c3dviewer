package c3dv.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import c3dv.model.C3DFile;
import c3dv.model.ParameterGroup;

public class C3DPanel extends JPanel {

  private static final String HEADER_STR = "Header Info";
  private static final String POINT_STR  = "Point Data";
  private static final String ANALOG_STR = "Analog Data";
  private static final String EVENTS_STR = "Events";

  private final C3DFile       file;
  private JTable              table;
  private JComboBox           groupComboBox;

  public C3DPanel(C3DFile file) {
    this.file = file;

    setLayout(new BorderLayout(0, 0));

    table = new JTable();
    JScrollPane scrollPane = new JScrollPane(table);
    add(scrollPane);

    JPanel btnPanel = new JPanel();
    add(btnPanel, BorderLayout.NORTH);
    GridBagLayout gbl_btnPanel = new GridBagLayout();
    gbl_btnPanel.columnWidths = new int[] { 52, 113, 126, 117, 130, 0 };
    gbl_btnPanel.rowHeights = new int[] { 29, 0 };
    gbl_btnPanel.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
    gbl_btnPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
    btnPanel.setLayout(gbl_btnPanel);

    groupComboBox = new JComboBox();
    groupComboBox.addItemListener(new GroupBoxItemListener());
    groupComboBox.addItem(HEADER_STR);
    groupComboBox.addItem(POINT_STR);
    groupComboBox.addItem(ANALOG_STR);
    groupComboBox.addItem(EVENTS_STR);
    for (ParameterGroup group : file.getGroups())
      groupComboBox.addItem(group.getName());
    GridBagConstraints gbc_groupComboBox = new GridBagConstraints();
    gbc_groupComboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_groupComboBox.insets = new Insets(0, 0, 0, 5);
    gbc_groupComboBox.gridx = 0;
    gbc_groupComboBox.gridy = 0;
    btnPanel.add(groupComboBox, gbc_groupComboBox);

//    JButton btnNewGroup = new JButton("New Group");
//    GridBagConstraints gbc_btnNewGroup = new GridBagConstraints();
//    gbc_btnNewGroup.anchor = GridBagConstraints.NORTHWEST;
//    gbc_btnNewGroup.insets = new Insets(0, 0, 0, 5);
//    gbc_btnNewGroup.gridx = 1;
//    gbc_btnNewGroup.gridy = 0;
//    btnPanel.add(btnNewGroup, gbc_btnNewGroup);
//
//    JButton btnDeleteGroup = new JButton("Delete Group");
//    GridBagConstraints gbc_btnDeleteGroup = new GridBagConstraints();
//    gbc_btnDeleteGroup.anchor = GridBagConstraints.NORTHWEST;
//    gbc_btnDeleteGroup.insets = new Insets(0, 0, 0, 5);
//    gbc_btnDeleteGroup.gridx = 2;
//    gbc_btnDeleteGroup.gridy = 0;
//    btnPanel.add(btnDeleteGroup, gbc_btnDeleteGroup);
//
//    JButton btnNewParam = new JButton("New Param.");
//    GridBagConstraints gbc_btnNewParam = new GridBagConstraints();
//    gbc_btnNewParam.anchor = GridBagConstraints.NORTHWEST;
//    gbc_btnNewParam.insets = new Insets(0, 0, 0, 5);
//    gbc_btnNewParam.gridx = 3;
//    gbc_btnNewParam.gridy = 0;
//    btnPanel.add(btnNewParam, gbc_btnNewParam);
//
//    JButton btnDeleteParam = new JButton("Delete Param.");
//    GridBagConstraints gbc_btnDeleteParam = new GridBagConstraints();
//    gbc_btnDeleteParam.anchor = GridBagConstraints.NORTHWEST;
//    gbc_btnDeleteParam.gridx = 4;
//    gbc_btnDeleteParam.gridy = 0;
//    btnPanel.add(btnDeleteParam, gbc_btnDeleteParam);
  }

  private class GroupBoxItemListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String name = e.getItem().toString();
        if (name.equals(HEADER_STR)) {
          table.setModel(new HeaderTableModel(file));
        } else if (name.equals(POINT_STR)) {
          table.setModel(new PointTableModel(file));
        } else if (name.equals(ANALOG_STR)) {
          table.setModel(new AnalogTableModel(file));
        } else if (name.equals(EVENTS_STR)) {
          table.setModel(new EventsTableModel(file));
        } else {
          ParameterGroup group = file.getGroup(name);
          table.setModel(new ParameterGroupTableModel(group));
        }
      }
    }
  }
}
