package c3dv.ui;

import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import c3dv.model.C3DFile;

/**
 * Displays a C3D file's header information in a JTable.
 * 
 * @author justin
 */
public class HeaderTableModel extends AbstractTableModel {

  private final C3DFile  file;
  private final String[] columns = { "Name", "Value" };
  private final String[] rowNames = {
      "Parameter Start",
      "Data Start",
      "Num. Points",
      "Num. Analog Channels / Frame",
      "Num. Analog Samples / Frame",
      "Video Rate",
      "Analog Rate",
      "First Frame",
      "Last Frame",
      "Scale Factor",
      "Max. Interpolation Gap",
      "Range & Label Data",
      "Range & Label Start",
      "Supports 4-char Event Labels",
      "File Byte Format",
      "Analog Format",
      "Data Format"};
  
  public HeaderTableModel(C3DFile file) {
    this.file = file;
  }

  @Override
  public int getColumnCount() {
    return columns.length;
  }

  @Override
  public int getRowCount() {
    return rowNames.length;
  }
  
  @Override
  public String getColumnName(int column) {
    return columns[column];
  }

  @Override
  public Object getValueAt(int row, int col) {
    if (col == 0)
      return rowNames[row];
    
    switch (row) {
    case 0: return file.paramStartBlock;
    case 1: return file.dataStartBlock;
    case 2: return file.num3DPoints;
    case 3: return file.numAnalogChannelsPerVideoFrame;
    case 4: return file.numAnalogSamplesPerFrame;
    case 5: return file.framesPerSecond;
    case 6: return file.numAnalogSamplesPerFrame * file.framesPerSecond;
    case 7: return file.first3DFrame;
    case 8: return file.last3DFrame;
    case 9: return file.scale3DFactor;
    case 10: return file.maxInterpolationGap;
    case 11: return file.rangeAndLabelDataPresent;
    case 12: return file.rangeAndLabelStartBlock;
    case 13: return file.supports4CharEventLabels;
    case 14: return file.byteOrder;
    case 15: return file.analogFormat;
    case 16: return file.dataFormat;
    }
    
    return null;
  }
}
