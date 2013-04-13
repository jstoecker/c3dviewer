package c3dv.view;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.gl2.GLUgl2;

import jgl.math.vector.Vec3f;
import jgl.opengl.view.FPCamera;
import jgl.opengl.view.OrbitCamera;
import jgl.opengl.view.Viewport;
import c3dv.model.C3DFile;

public class Renderer implements GLEventListener {

  OrbitCamera camera      = new OrbitCamera(3, 0, 0, new Vec3f(0), false);
  C3DFile     file;
  CoordSystem coordSystem = new CoordSystem();
  Markers     markers     = new Markers();
  Grid        grid        = new Grid();
  GLUgl2      glu         = new GLUgl2();
  int         curFrame;
  float       aspectRatio = 1;
  int         selected    = -1;

  public Renderer() {
    grid.update(coordSystem);
  }

  public OrbitCamera getCamera() {
    return camera;
  }

  public void setSelected(int selected) {
    this.selected = selected;
  }

  public void setFile(C3DFile file) {
    this.file = file;
    curFrame = 0;
    coordSystem.update(file);
    markers.update(file);
    grid.update(coordSystem);
  }

  public void setFrame(int curFrame) {
    this.curFrame = curFrame;
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();
    gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();
    grid.destroy(gl);
    markers.destroy(gl);
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
    camera.setViewport(new Viewport(x, y, w, h));
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    camera.apply(gl);

    grid.draw(gl);
    coordSystem.draw(gl);

    if (file == null) return;
    markers.draw(gl, selected, file.frames[curFrame]);
  }
}
