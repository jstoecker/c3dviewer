/*******************************************************************************
 *  Copyright (C) 2013 Justin Stoecker. The MIT License.
 *******************************************************************************/
package c3dv.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import jgl.cameras.Camera;
import jgl.math.Maths;
import jgl.math.vector.Mat4f;
import jgl.math.vector.Transform;
import jgl.math.vector.Vec3f;

/**
 * Rotates with spherical coordinates (azimuth, altitude).
 * 
 * @author justin
 */
public class CameraController implements KeyListener, MouseWheelListener {

  private class Parameter {
    float value       = 0;
    int   dir         = 0;
    float velocity    = 0;
    float damping     = 0.5f;
    float maxValue    = Float.POSITIVE_INFINITY;
    float maxVelocity = 2;

    Parameter() {
    }

    Parameter(float maxValue) {
      this.maxValue = maxValue;
    }

    boolean update() {

      velocity = Maths.clamp((velocity + dir * 0.05f) * damping, -maxVelocity, maxVelocity);
      value = Maths.clamp(value + velocity, -maxValue, maxValue);
      float velocityMagnitude = Math.abs(velocity);
      if (velocityMagnitude < 0.001)
        velocity = 0;

      return true;
    }
  }

  Parameter altitude = new Parameter(Maths.PI / 2 - 0.02f);
  Parameter azimuth  = new Parameter();
  Parameter radius   = new Parameter();

  Vec3f     up;
  Vec3f     target   = new Vec3f(0);
  Camera    camera;

  public CameraController() {
    setUpY(true);
  }
  
  /** If false, Z is up; if true, Y is up. */
  public void setUpY(boolean yUp) {
    up = (yUp ? Vec3f.axisY() : Vec3f.axisZ());
  }

  public void setTarget(Vec3f target) {
    this.target = target;
    updateView();
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
    updateView();
  }

  public void setRadius(float radius) {
    this.radius.value = radius;
  }
  
  public void setAzimuth(float azimuth) {
    this.azimuth.value = azimuth;
    updateView();
  }
  
  public void setAltitude(float altitude) {
    this.altitude.value = altitude;
    updateView();
  }

  public void update() {
    if (camera == null)
      return;

    boolean changed = false;
    changed = azimuth.update() || changed;
    changed = altitude.update() || changed;
    changed = radius.update() || changed;

    if (changed)
      updateView();
  }

  private void updateView() {
    Vec3f eye = Maths.sphericalToCartesian(radius.value, altitude.value, azimuth.value, up.y > 0);
    Mat4f view = Transform.lookAt(eye.x, eye.y, eye.z, target.x, target.y, target.z, up.x, up.y,
        up.z);
    camera.setView(view);
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_W) {
      altitude.dir = 1;
    } else if (e.getKeyCode() == KeyEvent.VK_S) {
      altitude.dir = -1;
    } else if (e.getKeyCode() == KeyEvent.VK_A) {
      azimuth.dir = -1;
    } else if (e.getKeyCode() == KeyEvent.VK_D) {
      azimuth.dir = 1;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_W && altitude.dir == 1) {
      altitude.dir = 0;
    } else if (e.getKeyCode() == KeyEvent.VK_S && altitude.dir == -1) {
      altitude.dir = 0;
    } else if (e.getKeyCode() == KeyEvent.VK_A && azimuth.dir == -1) {
      azimuth.dir = 0;
    } else if (e.getKeyCode() == KeyEvent.VK_D && azimuth.dir == 1) {
      azimuth.dir = 0;
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    radius.value += e.getWheelRotation() * 0.5f;
    updateView();
  }
}
