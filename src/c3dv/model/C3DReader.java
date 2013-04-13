package c3dv.model;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import c3dv.model.Buffer.ByteOrder;
import c3dv.model.C3DFile.AnalogFormat;
import c3dv.model.C3DFile.DataFormat;

/**
 * Reads a .c3d file into memory.
 * 
 * @author Justin Stoecker
 */
public class C3DReader {

  private C3DFile                          file;
  private Buffer                           buf;
  private HashMap<Integer, ParameterGroup> groupMap;
  private ArrayList<Parameter>             parameters;
  private PointReader                      pointParser;
  private ChannelReader                    analogParser;

  private short[]                          analogOffset;
  private float[]                          analogGenScale;
  private float[]                          analogScale;

  public C3DFile load(String fileName) {
    file = new C3DFile();

    // load data into memory
    if (!loadData(fileName))
      return null;

    // parse header block
    if (!parseHeader())
      return null;

    // parse parameter blocks
    if (!parseParameters())
      return null;

    // parse data blocks
    parseData();

    return file;
  }

  private boolean loadData(String fileName) {
    try {
      File file = new File(fileName);
      FileInputStream fis = new FileInputStream(file);
      BufferedInputStream bis = new BufferedInputStream(fis);
      DataInputStream in = new DataInputStream(bis);

      byte[] fileData = new byte[(int) file.length()];
      in.readFully(fileData);
      buf = new Buffer(fileData);

      in.close();
      return true;
    } catch (FileNotFoundException e) {
      System.err.println("(C3DReader.loadData): could not find file: " + e.getMessage());
      return false;
    } catch (IOException e) {
      System.err.println("(C3DReader.loadData): " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  private boolean parseHeader() {
    // start at the first word of data
    buf.setPositionToC3DWord(1);

    file.paramStartBlock = buf.getUByte(); // word 1, byte 1

    // ensure that this really is a C3D file
    boolean isC3Dfile = buf.getSByte() == 0x50; // word 1, byte 2
    if (!isC3Dfile) {
      System.err
          .println("(C3DReader.parseHeader): file does not appear to be a C3D file (second byte must be 0x50).");
      return false;
    }

    // the processor type is the 4th byte of the first parameter block; the first parameter block
    // location is stored in the first byte of the header block
    int procType = buf.getUByte((file.paramStartBlock - 1) * 512 + 3);
    switch (procType) {
    case 84:
      file.byteOrder = ByteOrder.LITTLE_ENDIAN;
      break;
    case 85:
      file.byteOrder = ByteOrder.MIDDLE_ENDIAN;
      break;
    case 86:
      file.byteOrder = ByteOrder.BIG_ENDIAN;
      break;
    default:
      System.err.println("(C3DReader.parseHeader): byte order is not recognized.");
      return false;
    }
    buf.setOrder(file.byteOrder);

    file.num3DPoints = buf.getUShort(); // word 2
    file.numAnalogChannelsPerVideoFrame = buf.getUShort(); // word 3
    file.first3DFrame = buf.getUShort(); // word 4
    file.last3DFrame = buf.getUShort(); // word 5
    file.maxInterpolationGap = buf.getUShort(); // word 6
    file.scale3DFactor = buf.getFloat(); // word 7,8
    file.dataFormat = file.scale3DFactor < 0 ? DataFormat.REAL : DataFormat.INTEGER;
    file.dataStartBlock = buf.getUShort(); // word 9
    file.numAnalogSamplesPerFrame = buf.getUShort(); // word 10
    file.framesPerSecond = buf.getFloat(); // word 11,12

    // move to word 148 (words 13-147 are reserved for future use)
    buf.setPositionToC3DWord(148);

    file.rangeAndLabelDataPresent = buf.getUShort() == 12345; // word 148
    file.rangeAndLabelStartBlock = buf.getUShort(); // word 149
    file.supports4CharEventLabels = buf.getUShort() == 12345; // word 150
    file.numDefinedTimeEvents = buf.getUShort(); // word 151

    if (file.numDefinedTimeEvents > 0) {
      file.eventTimes = new float[file.numDefinedTimeEvents];
      file.eventFlags = new boolean[file.numDefinedTimeEvents];
      file.eventLabels = new String[file.numDefinedTimeEvents];

      // move to word 153 to read event times
      buf.setPositionToC3DWord(153);
      for (int i = 0; i < file.numDefinedTimeEvents; i++)
        file.eventTimes[i] = buf.getFloat();

      // move to word 189 to read event flags
      buf.setPositionToC3DWord(189);
      for (int i = 0; i < file.numDefinedTimeEvents; i++)
        file.eventFlags[i] = buf.getSByte() == 0x00;

      // move to word 199 to read event labels
      buf.setPositionToC3DWord(199);
      int labelLength = file.supports4CharEventLabels ? 4 : 2;
      for (int i = 0; i < file.numDefinedTimeEvents; i++)
        file.eventLabels[i] = buf.getString(labelLength);
    } else {
      file.eventTimes = null;
      file.eventFlags = null;
      file.eventLabels = null;
    }

    return true;
  }

  private boolean parseParameters() {
    // move to parameter section header
    buf.setPositionToC3DBlock(file.paramStartBlock);
    buf.getUByte(); // byte 1 (not used)
    buf.getUByte(); // byte 2 (not used)
    file.numParamBlocks = buf.getUByte(); // byte 3
    buf.getUByte(); // byte 4 (processor type; already used in parseHeader)

    // parse all parameters / groups
    groupMap = new HashMap<Integer, ParameterGroup>();
    parameters = new ArrayList<Parameter>();
    while (parseParamOrGroup())
      ;

    // groups & parameters may appear in arbitrary order, so they must be connected after all
    // groups have been parsed
    for (Parameter parameter : parameters) {
      ParameterGroup group = groupMap.get(parameter.id);
      group.addParameter(parameter);
      parameter.group = group;
    }

    return true;
  }

  private boolean parseParamOrGroup() {
    int numCharsInName = buf.getSByte();
    boolean locked = false;
    if (numCharsInName < 0) {
      locked = true;
      numCharsInName *= -1;
    }

    byte groupID = buf.getSByte();
    String name = buf.getString(numCharsInName);
    int mark = buf.getPosition();
    int nextParamOrGroupOffset = buf.getUShort();

    if (groupID < 0) {
      // id is negative to indicate that a group follows
      int numCharsInDescription = buf.getUByte();
      String description = buf.getString(numCharsInDescription);

      ParameterGroup group = new ParameterGroup(name, groupID, description, locked);
      groupMap.put(group.id * -1, group);
      file.addGroup(group);
    } else {
      // id is positive to indicate that a parameter follows
      int dataType = buf.getSByte();
      int numDimensions = buf.getUByte();
      int size = Math.abs(dataType);
      int T = 1;
      int[] dimensions = null;
      if (numDimensions > 0) {
        dimensions = new int[numDimensions];
        for (int i = 0; i < numDimensions; i++) {
          dimensions[i] = buf.getUByte();
          T *= dimensions[i];
        }
      }

      Buffer dataBuf = null;
      if (T > 0) {
        dataBuf = new Buffer(buf.getSBytes(size * T));
        dataBuf.setOrder(file.byteOrder);
      }
      int numCharsInDescription = buf.getUByte();
      String description = buf.getString(numCharsInDescription);

      Parameter param = null;
      switch (dataType) {
      case -1:
        param = new CharParameter(name, groupID, dimensions, dataBuf, description, locked);
        break;
      case 1:
        param = new ByteParameter(name, groupID, dimensions, dataBuf, description, locked);
        break;
      case 2:
        param = new IntParameter(name, groupID, dimensions, dataBuf, description, locked);
        break;
      case 4:
        param = new FloatParameter(name, groupID, dimensions, dataBuf, description, locked);
        break;
      default:
        return false;
      }

      parameters.add(param);
    }

    // end of parameter section
    if (nextParamOrGroupOffset == 0)
      return false;

    buf.setPosition(mark + nextParamOrGroupOffset);

    return true;
  }

  private boolean parseData() {
    buf.setPositionToC3DBlock(file.dataStartBlock);

    file.frames = new Frame[file.last3DFrame - file.first3DFrame + 1];

    if (file.dataFormat == DataFormat.INTEGER) {
      pointParser = new PointReaderSI();

      String[] pAnalogFormat = file.charParamData("ANALOG", "FORMAT", null);
      if (pAnalogFormat != null && pAnalogFormat[0].equals("UNSIGNED")) {
        analogParser = new ChannelReaderUI();
        file.analogFormat = AnalogFormat.UNSIGNED;
      } else {
        analogParser = new ChannelReaderSI();
        file.analogFormat = AnalogFormat.SIGNED;
      }
    } else {
      pointParser = new PointReaderFP();
      analogParser = new ChannelReaderFP();
    }

    analogOffset = file.intParamData("ANALOG", "OFFSET", null);
    analogScale = file.floatParamData("ANALOG", "SCALE", null);
    analogGenScale = file.floatParamData("ANALOG", "GEN_SCALE", null);

    for (int frameIndex = 0; frameIndex < file.frames.length; frameIndex++) {
      int numPoints = file.num3DPoints;
      int numSamples = file.numAnalogSamplesPerFrame;
      int numChannelsPerSample = numSamples == 0 ? 0 : file.numAnalogChannelsPerVideoFrame
          / numSamples;
      Frame frame = new Frame(numPoints, numSamples, numChannelsPerSample);
      file.frames[frameIndex] = frame;

      for (int point = 0; point < file.num3DPoints; point++)
        pointParser.parsePoint(frame, point);

      for (int sample = 0; sample < numSamples; sample++) {
        for (int channel = 0; channel < numChannelsPerSample; channel++) {
          analogParser.parseAnalog(frame, sample, channel);
        }
      }
    }

    return true;
  }

  /** Reads the next 3D point */
  interface PointReader {
    void parsePoint(Frame frame, int point);
  }

  /** Signed integer point parser */
  class PointReaderSI implements PointReader {
    public void parsePoint(Frame frame, int point) {
      frame.x[point] = buf.getSShort() * file.scale3DFactor;
      frame.y[point] = buf.getSShort() * file.scale3DFactor;
      frame.z[point] = buf.getSShort() * file.scale3DFactor;
      short word4 = buf.getSShort();
      frame.camMask[point] = (byte) ((word4 >> 8) & 0xff);
      frame.residual[point] = (word4 == -1) ? -1 : (word4 & 0xff) * file.scale3DFactor;
    }
  }

  /** Floating-point point parser */
  class PointReaderFP implements PointReader {
    public void parsePoint(Frame frame, int point) {
      frame.x[point] = buf.getFloat();
      frame.y[point] = buf.getFloat();
      frame.z[point] = buf.getFloat();
      short word4 = (short) (buf.getFloat());
      frame.camMask[point] = (byte) ((word4 >> 8) & 0xff);
      frame.residual[point] = (word4 == -1) ? -1 : (word4 & 0xff) * -file.scale3DFactor;
    }
  }

  /** Reads the next analog channel */
  interface ChannelReader {
    void parseAnalog(Frame frame, int sample, int channel);
  }

  /** Signed integer channel parser */
  class ChannelReaderSI implements ChannelReader {
    public void parseAnalog(Frame frame, int sample, int channel) {
      short value = buf.getSShort();
      if (analogOffset != null && analogScale != null && analogGenScale != null) {
        frame.analogValues[sample][channel] = (value - analogOffset[channel]) * analogScale[channel] * analogGenScale[0];
      } else {
        frame.analogValues[sample][channel] = value;
      }
    }
  }

  /** Unsigned integer channel parser */
  class ChannelReaderUI implements ChannelReader {
    public void parseAnalog(Frame frame, int sample, int channel) {
      int value = buf.getUShort();
      if (analogOffset != null && analogScale != null && analogGenScale != null) {
        frame.analogValues[sample][channel] = (value - analogOffset[channel]) * analogScale[channel] * analogGenScale[0];
      } else {
        frame.analogValues[sample][channel] = value;
      }
    }
  }

  /** Floating-point channel parser */
  class ChannelReaderFP implements ChannelReader {
    public void parseAnalog(Frame frame, int sample, int channel) {
      float value = buf.getFloat();
      if (analogOffset != null && analogScale != null && analogGenScale != null) {
        frame.analogValues[sample][channel] = (value - analogOffset[channel]) * analogScale[channel] * analogGenScale[0];
      } else {
        frame.analogValues[sample][channel] = value;
      }
    }
  }
}