package c3dv.view;

import javax.media.opengl.GL2;

import jgl.math.vector.Vec3f;
import c3dv.model.C3DFile;

public class CoordSystem {

  public Vec3f worldX      = Vec3f.unitX();
  public Vec3f worldY      = Vec3f.unitZ();
  public Vec3f worldZ      = worldX.cross(worldY);
  public float markerScale = 1;
  public float axisLength  = 1;

  public void update(C3DFile file) {
    setAxes(file);
    setScale(file);
  }

  private void setAxes(C3DFile file) {
    worldX = axis(file.charParamData("POINT", "X_SCREEN", new String[] { "+X" })[0].trim());
    worldY = axis(file.charParamData("POINT", "Y_SCREEN", new String[] { "+Z" })[0].trim());
    worldZ = worldX.cross(worldY);
  }

  private static Vec3f axis(String name) {
    if (name.equals("+X"))
      return Vec3f.unitX();
    if (name.equals("-X"))
      return Vec3f.unitX().mul(-1);
    if (name.equals("+Y"))
      return Vec3f.unitY();
    if (name.equals("-Y"))
      return Vec3f.unitY().mul(-1);
    if (name.equals("+Z"))
      return Vec3f.unitZ();
    return Vec3f.unitZ().mul(-1);
  }

  private void setScale(C3DFile file) {
    String pScale = file.charParamData("POINT", "UNITS", new String[] { "mm" })[0].trim();
    if (pScale.equalsIgnoreCase("mm")) {
      markerScale = 0.001f;
    } else if (pScale.equalsIgnoreCase("cm")) {
      markerScale = 0.01f;
    } else if (pScale.equalsIgnoreCase("dm")) {
      markerScale = 0.1f;
    } else {
      markerScale = 1;
    }
  }

  public void draw(GL2 gl) {
    gl.glBegin(GL2.GL_LINES);
    gl.glColor3f(0.7f, 0.2f, 0.2f);
    gl.glVertex3f(0, 0, 0);
    gl.glVertex3f(axisLength, 0, 0);
    gl.glColor3f(0.2f, 0.7f, 0.2f);
    gl.glVertex3f(0, 0, 0);
    gl.glVertex3f(0, axisLength, 0);
    gl.glColor3f(0.2f, 0.2f, 0.7f);
    gl.glVertex3f(0, 0, 0);
    gl.glVertex3f(0, 0, axisLength);
    gl.glEnd();
  }
}
