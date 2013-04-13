package c3dv.model;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Base class for adding / modifying c3d point data.
 * 
 * @author justin
 */
public abstract class C3DEditor {

  protected final C3DFile file;
  private final String[]  newMarkerLabels;
  private final String    outputFileName;
  private String[]        markerLabels;

  public C3DEditor(String inputFileName, String outputFileName, String[] newMarkerLabels) {
    file = new C3DReader().load(inputFileName);
    this.newMarkerLabels = newMarkerLabels;
    this.outputFileName = outputFileName;
  }

  public final void process() {
    int newMarkers = (newMarkerLabels == null ? 0 : newMarkerLabels.length);
    int numMarkers = file.num3DPoints + newMarkers;
    resizeData(numMarkers);

    if (newMarkers > 0)
      addLabels(newMarkerLabels);

    for (int frameIndex = 0; frameIndex < file.frames.length; frameIndex++) {
      for (int markerIndex = 0; markerIndex < file.num3DPoints; markerIndex++) {
        String label = (markerLabels == null) ? null : markerLabels[markerIndex].trim();
        editMarker(frameIndex, markerIndex, label);
      }
    }

    new C3DWriter().write(file, outputFileName);
  }

  /**
   * Place code here that will calculate the marker at index for the given frame.
   * 
   * @param frameIndex - The index of the frame being edited.
   * @param markerIndex - The index of the marker in the frame.
   * @param markerName - The name of the marker (or NULL if it doesn't have one).
   */
  protected abstract void editMarker(int frameIndex, int markerIndex, String markerName);

  /**
   * New markers are being added, so the old arrays must be resized.
   */
  void resizeData(int numPoints) {
    if (file.num3DPoints != numPoints) {
      file.num3DPoints = numPoints;
      for (Frame frame : file.frames) {
        frame.x = Arrays.copyOf(frame.x, numPoints);
        frame.y = Arrays.copyOf(frame.y, numPoints);
        frame.z = Arrays.copyOf(frame.z, numPoints);

        // don't worry about setting these values for new markers:
        frame.residual = Arrays.copyOf(frame.residual, numPoints);
        frame.camMask = Arrays.copyOf(frame.camMask, numPoints);
      }
    }
  }

  /**
   * This will add labels for the new markers.
   */
  void addLabels(String[] newLabels) {
    Parameter param = file.getParameter("POINT", "LABELS");
    if (param != null && param instanceof CharParameter) {
      CharParameter pLabels = (CharParameter) param;

      // each label must have the same length, specified by the first dimension:
      int labelLength = pLabels.dimensions[1];

      // the number of labels is the second dimension, so we add 2:
      pLabels.dimensions[1] += newLabels.length;

      // resize array and insert new labels (resized to correct size)
      pLabels.data = Arrays.copyOf(pLabels.data, pLabels.dimensions[1]);
      String fmt = String.format("%%-%d.%ds", labelLength, labelLength);
      int j = pLabels.data.length - newLabels.length;
      for (int i = 0; i < newLabels.length; i++)
        pLabels.data[j++] = String.format(fmt, newLabels[i]);

      // finally, make sure the number of param blocks hasn't changed
      int blocksNeeded = (int) Math.ceil(file.calcSizeOfParameterSection() / 512.0);
      if (blocksNeeded > file.numParamBlocks)
        file.numParamBlocks = blocksNeeded;

      markerLabels = pLabels.data;

      for (int i = 0; i < markerLabels.length; i++)
        System.out.printf("markerIndex %2d = %s\n", i, markerLabels[i]);
    }
  }
}
