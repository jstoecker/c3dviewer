package c3dv.ui;

import javax.swing.table.AbstractTableModel;

import c3dv.model.C3DFile;

public class EventsTableModel extends AbstractTableModel {

  private C3DFile        file;
  private final String[] columnNames = { "Event", "Label", "Time", "Flag" };

  public EventsTableModel(C3DFile file) {
    this.file = file;
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public int getRowCount() {
    return (file.eventTimes == null) ? 0 : file.eventTimes.length;
  }

  @Override
  public Object getValueAt(int row, int col) {
    switch (col) {
    case 0:
      return row;
    case 1:
      return file.eventLabels[row];
    case 2:
      return file.eventTimes[row];
    case 3:
      return file.eventFlags[row];
    }
    return null;
  }
}
