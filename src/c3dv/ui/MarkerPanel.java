package c3dv.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import c3dv.model.C3DFile;

public class MarkerPanel extends JPanel {

  C3DFile file;
  JList   list = new JList();

  public MarkerPanel() {
    setLayout(new BorderLayout());
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scrollPanel = new JScrollPane(list);
    scrollPanel.setPreferredSize(new Dimension(100, 0));
    add(scrollPanel, BorderLayout.CENTER);
  }

  public void setFile(C3DFile file) {
    this.file = file;
    if (file != null) {
      list.setModel(new MarkerListModel(file));
    }
    repaint();
  }

  private class MarkerListModel extends AbstractListModel {
    C3DFile  file;
    String[] markerLabels;

    public MarkerListModel(C3DFile file) {
      this.file = file;
      markerLabels = file.charParamData("POINT", "LABELS", null);
    }

    @Override
    public Object getElementAt(int index) {
      return (markerLabels == null) ? index : markerLabels[index];
    }

    @Override
    public int getSize() {
      return file.num3DPoints;
    }
  }
}
