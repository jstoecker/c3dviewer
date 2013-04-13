package c3dv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import c3dv.model.C3DFile;
import c3dv.view.Renderer;

public class AnimationPanel extends JPanel {
  private JSlider  slider;
  private JLabel   lblFrame;
  private JLabel   lblTime;
  private GLCanvas canvas;
  private C3DFile  file;
  private Renderer renderer;
  private JLabel   lblHertz;
  public boolean   playing = false;
  private JButton  btnPlay;

  public AnimationPanel(Renderer renderer, GLCanvas canvas) {
    this.renderer = renderer;
    this.canvas = canvas;
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] { 840, 0 };
    gridBagLayout.rowHeights = new int[] { 29, 31, 0 };
    gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
    gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
    setLayout(gridBagLayout);

    JPanel sliderPanel = new JPanel();
    sliderPanel.setBorder(new MatteBorder(1, 0, 0, 0, (Color) new Color(192, 192, 192)));
    GridBagConstraints gbc_sliderPanel = new GridBagConstraints();
    gbc_sliderPanel.fill = GridBagConstraints.BOTH;
    gbc_sliderPanel.insets = new Insets(0, 0, 5, 0);
    gbc_sliderPanel.gridx = 0;
    gbc_sliderPanel.gridy = 1;
    add(sliderPanel, gbc_sliderPanel);
    sliderPanel.setLayout(new BorderLayout(0, 0));

    slider = new JSlider();
    slider.setValue(0);
    slider.addChangeListener(new SliderListener());
    sliderPanel.add(slider);

    btnPlay = new JButton("");
    btnPlay.setIcon(new ImageIcon(AnimationPanel.class.getResource("/resources/play.png")));
    btnPlay.setPreferredSize(new Dimension(50, 35));
    btnPlay.addActionListener(new PlayListener());
    sliderPanel.add(btnPlay, BorderLayout.WEST);

    JPanel labelPanel = new JPanel();
    labelPanel.setBorder(new MatteBorder(1, 0, 0, 0, (Color) Color.LIGHT_GRAY));
    GridBagConstraints gbc_labelPanel = new GridBagConstraints();
    gbc_labelPanel.fill = GridBagConstraints.BOTH;
    gbc_labelPanel.gridx = 0;
    gbc_labelPanel.gridy = 0;
    add(labelPanel, gbc_labelPanel);
    GridBagLayout gbl_labelPanel = new GridBagLayout();
    gbl_labelPanel.columnWidths = new int[] { 100, 100, 100, 100, 100, 100, 0 };
    gbl_labelPanel.rowHeights = new int[] { 0, 0 };
    gbl_labelPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
    gbl_labelPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
    labelPanel.setLayout(gbl_labelPanel);

    JLabel lblFrameTitle = new JLabel("Frame:");
    GridBagConstraints gbc_lblFrameTitle = new GridBagConstraints();
    gbc_lblFrameTitle.insets = new Insets(0, 0, 0, 5);
    gbc_lblFrameTitle.gridx = 0;
    gbc_lblFrameTitle.gridy = 0;
    labelPanel.add(lblFrameTitle, gbc_lblFrameTitle);

    lblFrame = new JLabel("<frame>");
    GridBagConstraints gbc_lblFrame = new GridBagConstraints();
    gbc_lblFrame.anchor = GridBagConstraints.WEST;
    gbc_lblFrame.insets = new Insets(0, 0, 0, 5);
    gbc_lblFrame.gridx = 1;
    gbc_lblFrame.gridy = 0;
    labelPanel.add(lblFrame, gbc_lblFrame);

    JLabel lbTimeTitle = new JLabel("Time:");
    GridBagConstraints gbc_lbTimeTitle = new GridBagConstraints();
    gbc_lbTimeTitle.insets = new Insets(0, 0, 0, 5);
    gbc_lbTimeTitle.gridx = 2;
    gbc_lbTimeTitle.gridy = 0;
    labelPanel.add(lbTimeTitle, gbc_lbTimeTitle);

    lblTime = new JLabel("<time>");
    GridBagConstraints gbc_lblTime = new GridBagConstraints();
    gbc_lblTime.anchor = GridBagConstraints.WEST;
    gbc_lblTime.insets = new Insets(0, 0, 0, 5);
    gbc_lblTime.gridx = 3;
    gbc_lblTime.gridy = 0;
    labelPanel.add(lblTime, gbc_lblTime);

    JLabel lblHertzTitle = new JLabel("Rate:");
    GridBagConstraints gbc_lblHertzTitle = new GridBagConstraints();
    gbc_lblHertzTitle.insets = new Insets(0, 0, 0, 5);
    gbc_lblHertzTitle.gridx = 4;
    gbc_lblHertzTitle.gridy = 0;
    labelPanel.add(lblHertzTitle, gbc_lblHertzTitle);

    lblHertz = new JLabel("<hz>");
    GridBagConstraints gbc_lblHertz = new GridBagConstraints();
    gbc_lblHertz.anchor = GridBagConstraints.WEST;
    gbc_lblHertz.gridx = 5;
    gbc_lblHertz.gridy = 0;
    labelPanel.add(lblHertz, gbc_lblHertz);

  }

  public void setFile(C3DFile file) {
    this.file = file;
    slider.setMinimum(0);
    slider.setMaximum(file.last3DFrame - file.first3DFrame);
    slider.setValue(0);
    lblHertz.setText(Float.toString(file.framesPerSecond) + "Hz");
  }

  private void setFrame(int frame) {
    if (frame != slider.getValue())
      slider.setValue(frame);

    renderer.setFrame(frame);
    lblFrame.setText(Integer.toString(slider.getValue()));
    lblTime.setText(String.format("%.3f", slider.getValue() / file.framesPerSecond));
    canvas.repaint();

  }

  private class SliderListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (file != null)
        setFrame(slider.getValue());
    }
  }

  private class PlayListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (file != null) {
        if (playing) {
          playing = false;
        } else {
          new PlayThread().start();
        }
      }
    }
  }

  private class PlayThread extends Thread {
    int frame         = slider.getValue();
    int sleepDuration = (int) (1000f / file.framesPerSecond);

    public void run() {
      if (frame == file.last3DFrame - file.first3DFrame)
        frame = 0;
      btnPlay.setIcon(new ImageIcon(AnimationPanel.class.getResource("/resources/pause.png")));
      playing = true;
      slider.setEnabled(false);
      while (playing && frame < (file.last3DFrame - file.first3DFrame + 1)) {
        setFrame(frame++);
        try {
          Thread.sleep(sleepDuration);
        } catch (InterruptedException e) {
        }
      }
      btnPlay.setIcon(new ImageIcon(AnimationPanel.class.getResource("/resources/play.png")));
      slider.setEnabled(true);
      playing = false;
    }
  }
}
