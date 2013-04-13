package c3dv.ui;

import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import c3dv.model.C3DFile;

public class AnalogTableModel extends AbstractTableModel {

  private C3DFile file;
  private final String[] columnNames = { "Frame", "Sample", "Channels" };
  
  public AnalogTableModel(C3DFile file) {
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
    return file.frames.length * file.numAnalogChannelsPerVideoFrame;
  }

  @Override
  public Object getValueAt(int row, int col) {
    int frameIndex = row / file.numAnalogChannelsPerVideoFrame;
    int sampleIndex = row % file.numAnalogSamplesPerFrame;
    switch (col) {
    case 0: return frameIndex;
    case 1: return sampleIndex;
    case 2: return Arrays.toString(file.frames[frameIndex].analogValues[sampleIndex]);
    }
    return null;
  }
}
