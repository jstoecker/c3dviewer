package c3dv.view;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import jgl.math.vector.Vec3f;
import jgl.opengl.view.OrbitCamera;

public class CameraControl implements MouseListener, MouseMotionListener, MouseWheelListener,
    KeyListener {

  OrbitCamera           camera;
  public List<Listener> listeners        = new ArrayList<Listener>();
  private float         pixelsToRadians  = 0.005f;
  private int           rotButtonMask    = MouseEvent.BUTTON1_DOWN_MASK;
  private Point         mouseAnchor;
  private float         anchorYaw;
  private float         anchorPitch;
  private boolean       moveFwd          = false;
  private boolean       moveBack         = false;
  private boolean       moveLeft         = false;
  private boolean       moveRight        = false;
  private ControlThread controlThread;
  private float         translationScale = 0.1f;

  public CameraControl(Renderer renderer) {
    this.camera = renderer.camera;
  }

  public void start() {
    controlThread = new ControlThread();
    controlThread.start();
  }

  public void stop() {
    if (controlThread != null) {
      controlThread.running = false;
      controlThread = null;
    }
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getWheelRotation() < 0) {
      camera.setRadius(camera.getRadius() - 0.2f);
    } else {
      camera.setRadius(camera.getRadius() + 0.2f);
    }
    fireViewChangeEvent();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (mouseAnchor != null && (e.getModifiersEx() & rotButtonMask) == rotButtonMask) {
      Point curPt = e.getPoint();
      float dx = (mouseAnchor.x - curPt.x) * pixelsToRadians;
      float dy = (mouseAnchor.y - curPt.y) * pixelsToRadians;
      camera.setAltitude(anchorPitch - dy);
      camera.setAzimuth(anchorYaw + dx);
      fireViewChangeEvent();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    mouseAnchor = e.getPoint();
    anchorPitch = camera.getAltitude();
    anchorYaw = camera.getAzimuth();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    mouseAnchor = null;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
    case KeyEvent.VK_W:
      moveFwd = true;
      break;
    case KeyEvent.VK_A:
      moveLeft = true;
      break;
    case KeyEvent.VK_S:
      moveBack = true;
      break;
    case KeyEvent.VK_D:
      moveRight = true;
      break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
    case KeyEvent.VK_W:
      moveFwd = false;
      break;
    case KeyEvent.VK_A:
      moveLeft = false;
      break;
    case KeyEvent.VK_S:
      moveBack = false;
      break;
    case KeyEvent.VK_D:
      moveRight = false;
      break;
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  private void fireViewChangeEvent() {
    for (Listener l : listeners)
      l.viewChanged(this);
  }

  public interface Listener {
    void viewChanged(CameraControl control);
  }

  private void updateCamera() {
    Vec3f translation = new Vec3f(0);

    if (moveFwd) translation.add(camera.getForward().times(translationScale));
    if (moveBack) translation.add(camera.getBackward().times(translationScale));
    if (moveLeft) translation.add(camera.getLeft().times(translationScale));
    if (moveRight) translation.add(camera.getRight().times(translationScale));

    if (translation.lengthSquared() > 0) {
      camera.translateEye(translation);
      fireViewChangeEvent();
    }
  }

  private class ControlThread extends Thread {
    volatile boolean running = true;

    @Override
    public void run() {
      while (running) {
        try {
          updateCamera();
          Thread.sleep(16);
        } catch (InterruptedException ex) {
        }
      }
    }
  }
}
