package c3dv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jgl.math.Maths;
import c3dv.model.C3DFile;
import c3dv.model.C3DReader;
import c3dv.view.Renderer;

import com.jogamp.opengl.util.FPSAnimator;

public class C3DViewer extends JFrame {
  private JFrame         parametersFrame;
  private JPanel         contentPane;
  private C3DFile        file;
  private Renderer       renderer    = new Renderer();
  private JFileChooser   fileChooser = new JFileChooser();
  private GLCanvas       canvas;
  private AnimationPanel animationPanel;
  private MarkerPanel    markerPanel;

  public C3DViewer(C3DFile file) {
    GLProfile glp = GLProfile.get(GLProfile.GL2);
    GLCapabilities glc = new GLCapabilities(glp);
    glc.setNumSamples(8);
    glc.setSampleBuffers(true);
    canvas = new GLCanvas(glc);

    setTitle("C3D File Viewer");
    contentPane = new JPanel();
    setContentPane(contentPane);
    contentPane.setLayout(new BorderLayout(0, 0));

    animationPanel = new AnimationPanel(renderer, canvas);
    contentPane.add(animationPanel, BorderLayout.SOUTH);

    markerPanel = new MarkerPanel();
    markerPanel.setBorder(new MatteBorder(0, 1, 0, 0, (Color) Color.LIGHT_GRAY));
    markerPanel.setFile(null);
    markerPanel.list.addListSelectionListener(new MarkerListListener());
    contentPane.add(markerPanel, BorderLayout.EAST);

    contentPane.add(canvas, BorderLayout.CENTER);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLocationRelativeTo(null);
    setJMenuBar(createMenuBar());

    setVisible(true);
    
    final CameraController camControl = new CameraController();
    canvas.addKeyListener(camControl);
    canvas.addMouseWheelListener(camControl);
    canvas.addGLEventListener(new GLEventListener() {
      public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
      }
      public void init(GLAutoDrawable arg0) {
      }
      public void dispose(GLAutoDrawable arg0) {
      }
      public void display(GLAutoDrawable arg0) {
        camControl.update();
      }
    });
    
    canvas.addGLEventListener(renderer);

    
    
    FPSAnimator animator = new FPSAnimator(canvas, 60);
    animator.start();
    
    camControl.setCamera(renderer.getCamera());
    camControl.setRadius(3);
    camControl.setAltitude(Maths.PI/4);
    camControl.setUpY(false);
  }

  void setFile(C3DFile file) {
    this.file = file;

    boolean paramFrameWasVisible = parametersFrame == null ? false : parametersFrame.isVisible();
    if (parametersFrame != null)
      parametersFrame.setVisible(false);
    parametersFrame = new JFrame("C3D File Data");
    parametersFrame.add(new C3DPanel(file));
    parametersFrame.pack();
    parametersFrame.setVisible(paramFrameWasVisible);

    animationPanel.setFile(file);
    renderer.setFile(file);
    markerPanel.setFile(file);
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);

    JMenuItem openMenuItem = new JMenuItem("Open File...");
    fileMenu.add(openMenuItem);
    openMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (fileChooser.showOpenDialog(C3DViewer.this) == JFileChooser.APPROVE_OPTION) {
          setFile(new C3DReader().load(fileChooser.getSelectedFile().getAbsolutePath()));
        }
      }
    });

//    JMenuItem saveMenuItem = new JMenuItem("Save As...");
//    fileMenu.add(saveMenuItem);
//    saveMenuItem.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        if (file == null)
//          return;
//        if (fileChooser.showSaveDialog(C3DViewer.this) == JFileChooser.APPROVE_OPTION) {
//          new C3DWriter().write(file, fileChooser.getSelectedFile().getAbsolutePath());
//        }
//      }
//    });

    JMenu viewMenu = new JMenu("View");
    menuBar.add(viewMenu);

    JMenuItem paramMenuItem = new JMenuItem("Parameters");
    viewMenu.add(paramMenuItem);
    paramMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (parametersFrame != null)
          parametersFrame.setVisible(true);
      }
    });

    return menuBar;
  }

  public static void main(String[] args) {
    final C3DFile file = (args.length == 1) ? new C3DReader().load(args[0]) : null;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new C3DViewer(file);
      }
    });
  }

  private class MarkerListListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) {
        renderer.setSelected(markerPanel.list.getSelectedIndex());
      }
    }
  }
}
