package c3dv.model;

/**
 * Stores 3D position and analog data for one frame of a C3D file.
 * 
 * @author justin
 */
public class Frame {

  public float[][] analogValues;
  public float[]   x;
  public float[]   y;
  public float[]   z;
  public float[]   residual;
  public byte[]    camMask;

  public Frame(int numPoints, int numSamples, int numChannelsPerSample) {
    x = new float[numPoints];
    y = new float[numPoints];
    z = new float[numPoints];
    residual = new float[numPoints];
    camMask = new byte[numPoints];

    if (numSamples > 0 && numChannelsPerSample > 0)
      analogValues = new float[numSamples][numChannelsPerSample];
  }

  /** @return True if camera in [1,7] was used to calculate this point */
  public boolean cameraUsed(int pointIndex, int cameraIndex) {
    return ((camMask[pointIndex] >> (cameraIndex - 1)) & 0x01) == 0x01;
  }
  
  public boolean isValid(int pointIndex) {
    return residual[pointIndex] >= 0;
  }
}
