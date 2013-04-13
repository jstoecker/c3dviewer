package c3dv.ui;

import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import c3dv.model.Parameter;
import c3dv.model.ParameterGroup;

/**
 * Displays a parameter group in a JTable.
 * 
 * @author justin
 */
public class ParameterGroupTableModel extends AbstractTableModel {

  private final ParameterGroup group;
  private final String[]          columns = { "Name", "Description", "Locked", "Type", "Dimensions", "Data" };

  public ParameterGroupTableModel(ParameterGroup group) {
    this.group = group;
  }

  @Override
  public int getColumnCount() {
    return columns.length;
  }

  @Override
  public int getRowCount() {
    return group.getParameters().size();
  }

  @Override
  public String getColumnName(int column) {
    return columns[column];
  }

  @Override
  public Object getValueAt(int row, int col) {
    Parameter param = group.getParameters().get(row);
    switch (col) {
    case 0:
      return param.name;
    case 1:
      return param.description;
    case 2:
      return param.locked;
    case 3:
      return param.typeName();
    case 4:
      return (param.dimensions == null) ? 1 : Arrays.toString(param.dimensions);
    case 5:
      return param.dataString();
    }

    return null;
  }
}
