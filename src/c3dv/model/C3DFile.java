package c3dv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import c3dv.model.Buffer.ByteOrder;

/**
 * Contains information stored in a .c3d file.
 * 
 * @see {@link http://www.c3d.org/HTML/}
 * 
 * @author Justin Stoecker
 */
public class C3DFile {

  public enum DataFormat {
    INTEGER,
    REAL
  }

  public enum AnalogFormat {
    SIGNED,
    UNSIGNED
  }

  /** Points to the first block of the parameter section. */
  public int                          paramStartBlock;

  /** Total number of blocks in the parameter section. */
  public int                          numParamBlocks;

  /** Number of 3D points in the C3D file (i.e. the number of stored trajectories). */
  public int                          num3DPoints;

  /**
   * Total number of analog measurements per 3D frame, i.e. number of channels multiplied by the
   * samples per frame.
   */
  public int                          numAnalogChannelsPerVideoFrame;

  /** Number of the first frame of 3D data (1 based, not 0). */
  public int                          first3DFrame;

  /** Number of the last frame of 3D data. */
  public int                          last3DFrame;

  /** Maximum interpolation gap in 3D frames. */
  public int                          maxInterpolationGap;

  /**
   * The 3D scale factor (floating-point) that converts signed integer 3D data to reference system
   * measurement units. If this is negative then the file is scaled in floating-point.
   */
  public float                        scale3DFactor;

  /** DATA_START the number of the first block of the 3D and analog data section. */
  public int                          dataStartBlock;

  /** The number of analog samples per 3d frame */
  public int                          numAnalogSamplesPerFrame;

  /** The 3D frame rate in Hz (floating-point). */
  public float                        framesPerSecond;

  /** A label & range section is present in the file */
  public boolean                      rangeAndLabelDataPresent;

  /** The first block of the Label and Range section (if present). */
  public int                          rangeAndLabelStartBlock;

  /** This C3D file supports 4 char event labels. An older format supported only 2 character labels. */
  public boolean                      supports4CharEventLabels;

  /** Number of defined time events (0 to 18) */
  public int                          numDefinedTimeEvents;

  /** Event times (floating-point) in seconds (up to 18 events). */
  public float[]                      eventTimes;

  /** Event display flags (on or off) */
  public boolean[]                    eventFlags;

  /** Event labels. Each label is 4 characters long unless support4CharEventLabels is false. */
  public String[]                     eventLabels;

  /** 3D & analog data storage format */
  public DataFormat                   dataFormat;

  /** All parameter groups */
  private List<ParameterGroup>        groups       = new ArrayList<ParameterGroup>();

  /** Retrieves a parameter group by its name */
  private Map<String, ParameterGroup> groupMap     = new HashMap<String, ParameterGroup>();

  /** 3D & analog data for all frames */
  public Frame[]                      frames;

  /** Byte order of the data read. */
  public ByteOrder                    byteOrder    = ByteOrder.LITTLE_ENDIAN;

  /** Signing of the analog data. */
  public AnalogFormat                 analogFormat = AnalogFormat.SIGNED;

  public C3DFile() {
  }

  public List<ParameterGroup> getGroups() {
    return groups;
  }

  public void addGroup(ParameterGroup group) {
    groups.add(group);
    groupMap.put(group.name, group);
  }

  public ParameterGroup getGroup(String name) {
    return groupMap.get(name);
  }

  public Parameter getParameter(String groupName, String paramName) {
    ParameterGroup group = groupMap.get(groupName);
    if (group == null)
      return null;
    return group.paramMap.get(paramName);
  }

  public int calcSizeOfParameterSection() {
    int size = 4; // header is 4 bytes
    for (ParameterGroup group : groups) {
      size += 5;
      size += group.name.length();
      size += (group.description == null) ? 0 : group.description.length();

      for (Parameter param : group.getParameters()) {
        size += 7;
        size += param.name.length();
        size += (param.description == null) ? 0 : param.description.length();
        byte[] data = param.getDataBytes();
        size += (data == null) ? 0 : data.length;
        size += (param.dimensions == null) ? 0 : param.dimensions.length;
      }
    }

    return size;
  }

  /** Calculates number of bytes the data section would occupy using the current frame data. */
  public int calcSizeOfDataSection() {
    int sizeElement = (dataFormat == DataFormat.INTEGER) ? 2 : 4;
    int size3D = num3DPoints * 4 * sizeElement;
    int sizeAnalog = numAnalogChannelsPerVideoFrame * sizeElement;
    int sizeFrame = size3D + sizeAnalog;
    return frames.length * sizeFrame;
  }
  
  public short[] intParamData(String groupName, String paramName, short[] defaultValue) {
    Parameter p = getParameter(groupName, paramName);
    if (p == null || !(p instanceof IntParameter)) return defaultValue;
    IntParameter pInt = (IntParameter)p;
    return pInt.data == null ? defaultValue : pInt.data;
  }
  
  public String[] charParamData(String groupName, String paramName, String[] defaultValue) {
    Parameter p = getParameter(groupName, paramName);
    if (p == null || !(p instanceof CharParameter)) return defaultValue;
    CharParameter pChar = (CharParameter)p;
    return pChar.data == null ? defaultValue : pChar.data;
  }
  
  public float[] floatParamData(String groupName, String paramName, float[] defaultValue) {
    Parameter p = getParameter(groupName, paramName);
    if (p == null || !(p instanceof FloatParameter)) return defaultValue;
    FloatParameter pFloat = (FloatParameter)p;
    return pFloat.data == null ? defaultValue : pFloat.data;
  }
  
  public int[] byteParamData(String groupName, String paramName, int[] defaultValue) {
    Parameter p = getParameter(groupName, paramName);
    if (p == null || !(p instanceof ByteParameter)) return defaultValue;
    ByteParameter pByte = (ByteParameter)p;
    return pByte.data == null ? defaultValue : pByte.data;
  }
}
