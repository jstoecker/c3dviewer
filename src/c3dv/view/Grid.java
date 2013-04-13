package c3dv.view;

import javax.media.opengl.GL2;

import jgl.math.vector.Vec3f;

public class Grid {

  boolean     init    = false;
  int         list    = -1;
  int         cells   = 20;
  float       spacing = 1;
  CoordSystem coordSystem;

  public void update(CoordSystem coordSystem) {
    this.coordSystem = coordSystem;
    init = false;
  }

  public void init(GL2 gl) {
    if (init)
      destroy(gl);
    
    float halfGridSize = (cells * spacing) / 2;
    Vec3f p1 = coordSystem.worldX.plus(coordSystem.worldZ).times(halfGridSize);
    Vec3f p2 = new Vec3f(p1);

    list = gl.glGenLists(1);
    gl.glNewList(list, GL2.GL_COMPILE);
    {
      gl.glColor3f(0.2f, 0.2f, 0.2f);
      gl.glBegin(GL2.GL_LINES);
      for (int cell = 0; cell <= cells; cell++) {
        glVertex(gl, p1);
        glVertex(gl, p1.minus(coordSystem.worldZ.times(halfGridSize * 2)));
        p1.add(coordSystem.worldX.times(-spacing));

        glVertex(gl, p2);
        glVertex(gl, p2.minus(coordSystem.worldX.times(halfGridSize * 2)));
        p2.add(coordSystem.worldZ.times(-spacing));
      }
      gl.glEnd();
    }
    gl.glEndList();

    init = true;
  }

  private static void glVertex(GL2 gl, Vec3f v) {
    gl.glVertex3f(v.x, v.y, v.z);
  }

  public void destroy(GL2 gl) {
    if (init) {
      gl.glDeleteLists(list, 1);
      init = false;
    }
  }

  public void draw(GL2 gl) {
    if (!init)
      init(gl);

    gl.glCallList(list);
  }
}
