package c3dv.ui;

import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import c3dv.model.C3DFile;
import c3dv.model.Frame;

/**
 * Displays a C3D file's point information in a JTable.
 * 
 * @author justin
 */
public class PointTableModel extends AbstractTableModel {

  private C3DFile        file;
  private final String[] columnNames = { "Frame", "Point", "X", "Y", "Z", "Residual", "Camera Mask" };

  public PointTableModel(C3DFile file) {
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
    return file.frames.length * file.num3DPoints;
  }

  @Override
  public Object getValueAt(int row, int col) {
    int frameIndex = row / file.num3DPoints;
    int pointIndex = row % file.num3DPoints;
    switch (col) {
    case 0: return frameIndex;
    case 1: return pointIndex;
    case 2: return file.frames[frameIndex].x[pointIndex];
    case 3: return file.frames[frameIndex].y[pointIndex];
    case 4: return file.frames[frameIndex].z[pointIndex];
    case 5: return file.frames[frameIndex].residual[pointIndex];
    case 6: return binaryString(file.frames[frameIndex].camMask[pointIndex]);
    }
    return null;
  }

  private static String binaryString(byte value) {
    String bits = Integer.toBinaryString(value & 0xff);
    return new String(new char[8 - bits.length()]).replace("\0", "0") + bits;
  }
}
