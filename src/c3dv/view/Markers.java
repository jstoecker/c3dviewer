package c3dv.view;

import javax.media.opengl.GL2;

import c3dv.model.C3DFile;
import c3dv.model.Frame;

public class Markers {
  int   list        = -1;
  float markerSize  = 0.03f;
  float markerScale = 1;

  public void update(C3DFile file) {
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

  public void draw(GL2 gl, int selected, Frame frame) {
    if (list == -1)
      init(gl);

    for (int markerIndex = 0; markerIndex < frame.x.length; markerIndex++) {
      
      if (markerIndex == selected) {
        gl.glColor3f(0.3f, 0.9f, 0.4f);
      } else {
        gl.glColor3f(0.9f, 0.3f, 0.4f);
      }
      
      float x = frame.x[markerIndex] * markerScale;
      float y = frame.y[markerIndex] * markerScale;
      float z = frame.z[markerIndex] * markerScale;

      gl.glPushMatrix();
      gl.glTranslatef(x, y, z);
      gl.glScaled(markerSize, markerSize, markerSize);
      gl.glCallList(list);
      gl.glPopMatrix();
    }
  }

  private void init(GL2 gl) {
    list = gl.glGenLists(1);
    gl.glNewList(list, GL2.GL_COMPILE);
    {
      gl.glBegin(GL2.GL_LINES);
      gl.glVertex3f(-0.5f, -0.5f, -0.5f);
      gl.glVertex3f(+0.5f, -0.5f, -0.5f);
      gl.glVertex3f(-0.5f, -0.5f, -0.5f);
      gl.glVertex3f(-0.5f, +0.5f, -0.5f);
      gl.glVertex3f(-0.5f, -0.5f, -0.5f);
      gl.glVertex3f(-0.5f, -0.5f, +0.5f);
      gl.glVertex3f(+0.5f, +0.5f, +0.5f);
      gl.glVertex3f(+0.5f, +0.5f, -0.5f);
      gl.glVertex3f(+0.5f, +0.5f, +0.5f);
      gl.glVertex3f(+0.5f, -0.5f, +0.5f);
      gl.glVertex3f(+0.5f, +0.5f, +0.5f);
      gl.glVertex3f(-0.5f, +0.5f, +0.5f);
      gl.glVertex3f(-0.5f, +0.5f, -0.5f);
      gl.glVertex3f(-0.5f, +0.5f, +0.5f);
      gl.glVertex3f(-0.5f, +0.5f, -0.5f);
      gl.glVertex3f(+0.5f, +0.5f, -0.5f);
      gl.glVertex3f(+0.5f, -0.5f, +0.5f);
      gl.glVertex3f(-0.5f, -0.5f, +0.5f);
      gl.glVertex3f(+0.5f, -0.5f, +0.5f);
      gl.glVertex3f(+0.5f, -0.5f, -0.5f);
      gl.glVertex3f(-0.5f, -0.5f, +0.5f);
      gl.glVertex3f(-0.5f, +0.5f, +0.5f);
      gl.glVertex3f(+0.5f, -0.5f, -0.5f);
      gl.glVertex3f(+0.5f, +0.5f, -0.5f);
      gl.glEnd();
    }
    gl.glEndList();
  }

  public void destroy(GL2 gl) {
    if (list != -1) {
      gl.glDeleteLists(list, 1);
      list = -1;
    }
  }
}
