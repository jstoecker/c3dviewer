package c3dv;

import c3dv.model.C3DEditor;
import c3dv.model.Frame;

public class TestC3DEditor extends C3DEditor {

  private static final String[] MARKER_NAMES = { "TEST1", "TEST2", "TEST3" };

  public TestC3DEditor(String inputFileName, String outputFileName,
      String[] newMarkers) {
    super(inputFileName, outputFileName, newMarkers);
  }

  @Override
  protected void editMarker(int frameIndex, int markerIndex, String markerName) {
    Frame frame = file.frames[frameIndex];

    // The total number of frames in the motion:
    // int numFrames = file.frames.length;

    // You can get/set the position of a marker i for a frame using:
    // frame.x[i] is the x-coordinate of the marker
    // frame.y[i] is the y-coordinate of the marker
    // frame.z[i] is the z-coordinate of the marker

    if (markerIndex == file.num3DPoints - 3) {
      // TEST1: this marker will just go around in a circle
      frame.x[markerIndex] = (float) Math.sin(frameIndex / 20.0) * 50;
      frame.y[markerIndex] = (float) Math.cos(frameIndex / 20.0) * 50;
    } else if (markerIndex == file.num3DPoints - 2) {
      // TEST2: average of the first two markers
      frame.x[markerIndex] = (frame.x[0] + frame.x[1]) / 2;
      frame.y[markerIndex] = (frame.y[0] + frame.y[1]) / 2;
      frame.z[markerIndex] = (frame.z[0] + frame.z[1]) / 2;
    } else if (markerIndex == file.num3DPoints - 1) {
      // TEST3: goes up and down along Z
      frame.z[markerIndex] = (float) Math.sin(frameIndex / 10.0) * 100;
    }
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Provide two arguments:");
      System.out.println("  <in>  - the original C3D file to read");
      System.out.println("  <out> - the modified C3D file to write");
      System.exit(1);
    }
    new TestC3DEditor(args[0], args[1], MARKER_NAMES).process();
  }
}
