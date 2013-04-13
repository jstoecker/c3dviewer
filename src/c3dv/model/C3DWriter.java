package c3dv.model;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import c3dv.model.C3DFile.DataFormat;

public class C3DWriter {

  C3DFile         file;
  ByteBuffer      buf;
  PointWriter     pointWriter;
  ChannelWriter   channelWriter;

  private short[] analogOffset;
  private float[] analogGenScale;
  private float[] analogScale;

  public boolean write(C3DFile file, String fileName) {
    this.file = file;

    // a source file may be ordered differently, but I force that the parameter section comes
    // immediately after the header, and the data comes immediately after the parameters
    file.paramStartBlock = 2;
    file.dataStartBlock = file.paramStartBlock + file.numParamBlocks;

    // allocate memory to store the file
    int numBlocks = 1 + file.numParamBlocks + (int) Math.ceil(file.calcSizeOfDataSection() / 512.0);
    buf = ByteBuffer.allocate(numBlocks * 512);
    buf.order(ByteOrder.LITTLE_ENDIAN);

    // select writers based on data type
    if (file.dataFormat == DataFormat.INTEGER) {
      pointWriter = new PointWriterSI();
      channelWriter = new ChannelWriterSI();
    } else {
      pointWriter = new PointWriterFP();
      channelWriter = new ChannelWriterFP();
    }

    // write to memory
    writeHeader();
    writeParameters();
    writeData();

    // write to disk
    try {
      File outFile = new File(fileName);
      FileOutputStream fos = new FileOutputStream(outFile);
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      DataOutputStream out = new DataOutputStream(bos);

      out.write(buf.array());

      out.close();
      return true;
    } catch (FileNotFoundException e) {
      System.err.println("(C3DWriter.write): could not find file: " + e.getMessage());
      return false;
    } catch (IOException e) {
      System.err.println("(C3DWriter.write): " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  private void writeHeader() {
    buf.put((byte) file.paramStartBlock); // word 1, byte 1
    buf.put((byte) 0x50); // word 1, byte 2
    buf.putShort((short) file.num3DPoints); // word 2
    buf.putShort((short) file.numAnalogChannelsPerVideoFrame); // word 3
    buf.putShort((short) file.first3DFrame); // word 4
    buf.putShort((short) file.last3DFrame); // word 5
    buf.putShort((short) file.maxInterpolationGap); // word 6
    buf.putFloat(file.scale3DFactor); // words 7-8
    buf.putShort((short) file.dataStartBlock); // word 9
    buf.putShort((short) file.numAnalogSamplesPerFrame); // word 10
    buf.putFloat(file.framesPerSecond); // word 11-12
    buf.position((148 - 1) * 2); // words 13 - 147 : reserved for future use
    buf.putShort((short) (file.rangeAndLabelDataPresent ? 12345 : 0)); // word 148
    buf.putShort((short) (file.rangeAndLabelStartBlock)); // word 149
    buf.putShort((short) (file.supports4CharEventLabels ? 12345 : 0)); // word 150
    buf.putShort((short) file.numDefinedTimeEvents); // word 151
    buf.position((152 - 1) * 2); // word 152 : reserved for future use
    if (file.numDefinedTimeEvents > 0) {
      for (int i = 0; i < file.eventTimes.length; i++)
        buf.putFloat(file.eventTimes[i]); // words 153-188
      buf.position((189 - 1) * 2);
      for (int i = 0; i < file.eventFlags.length; i++)
        buf.put((byte) (file.eventFlags[i] ? 0x00 : 0x01)); // words 189-197
      buf.position((199 - 1) * 2); // word 198 reserved for future use
      for (int i = 0; i < file.eventLabels.length; i++)
        buf.put(file.eventLabels[i].getBytes()); // words 199-234
    }
  }

  private void writeParameters() {
    // parameter section header
    buf.position((file.paramStartBlock - 1) * 512);
    buf.put((byte) 0x00);
    buf.put((byte) 0x00);
    buf.put((byte) (file.numParamBlocks));
    buf.put((byte) (84));

    List<ParameterGroup> groups = file.getGroups();
    for (int i = 0; i < groups.size(); i++) {
      ParameterGroup group = groups.get(i);
      List<Parameter> parameters = group.getParameters();
      writeGroup(group, i < groups.size() - 1 || !parameters.isEmpty());

      for (int j = 0; j < parameters.size(); j++) {
        writeParameter(parameters.get(j), i < groups.size() - 1 || j < parameters.size() - 1);
      }
    }
  }

  private void writeGroup(ParameterGroup group, boolean hasNext) {
    int M = (group.description == null) ? 0 : group.description.length();
    int offset = hasNext ? (3 + M) : 0;

    buf.put((byte) (group.locked ? -group.name.length() : group.name.length()));
    buf.put((byte) group.id);
    buf.put(group.name.getBytes());
    buf.putShort((short) offset);
    buf.put((byte) M);
    if (M > 0)
      buf.put(group.description.getBytes());
  }

  private void writeParameter(Parameter param, boolean hasNext) {
    int dataType = paramTypeLength(param);
    byte[] data = param.getDataBytes();
    int D = (param.dimensions == null) ? 0 : param.dimensions.length;
    int T = (data == null) ? 0 : data.length;
    int M = (param.description == null) ? 0 : param.description.length();
    int offset = hasNext ? (5 + D + T + M) : 0;

    buf.put((byte) (param.locked ? -param.name.length() : param.name.length()));
    buf.put((byte) param.id);
    buf.put(param.name.getBytes());
    buf.putShort((short) offset);
    buf.put((byte) dataType);
    buf.put((byte) D);
    for (int i = 0; i < D; i++)
      buf.put((byte) param.dimensions[i]);
    if (data != null)
      buf.put(data);
    buf.put((byte) M);
    if (M > 0)
      buf.put(param.description.getBytes());
  }

  private int paramTypeLength(Parameter param) {
    if (param instanceof CharParameter)
      return -1;
    if (param instanceof ByteParameter)
      return 1;
    if (param instanceof IntParameter)
      return 2;
    return 4;
  }

  private void writeData() {
    buf.position((file.dataStartBlock - 1) * 512);

    analogOffset = file.intParamData("ANALOG", "OFFSET", null);
    analogScale = file.floatParamData("ANALOG", "SCALE", null);
    analogGenScale = file.floatParamData("ANALOG", "GEN_SCALE", null);
    int numChannels = file.numAnalogSamplesPerFrame == 0 ? 0 : file.numAnalogChannelsPerVideoFrame
        / file.numAnalogSamplesPerFrame;
    for (int frameIndex = 0; frameIndex < file.frames.length; frameIndex++) {
      Frame frame = file.frames[frameIndex];

      // write point data for frame
      for (int pointIndex = 0; pointIndex < file.num3DPoints; pointIndex++)
        pointWriter.write(buf, frame, pointIndex, file.scale3DFactor);

      // write analog data for frame
      for (int sampleIndex = 0; sampleIndex < file.numAnalogSamplesPerFrame; sampleIndex++)
        for (int channelIndex = 0; channelIndex < numChannels; channelIndex++)
          channelWriter.write(buf, frame, sampleIndex, channelIndex);
    }
  }

  interface PointWriter {
    void write(ByteBuffer buf, Frame frame, int pointIndex, float scale);
  }

  class PointWriterSI implements PointWriter {
    public void write(ByteBuffer buf, Frame frame, int pointIndex, float scale) {
      if (frame.isValid(pointIndex)) {
        buf.putShort((short) (frame.x[pointIndex] / scale));
        buf.putShort((short) (frame.y[pointIndex] / scale));
        buf.putShort((short) (frame.z[pointIndex] / scale));
        int b1 = frame.camMask[pointIndex] & 0xff;
        int b2 = (byte) (frame.residual[pointIndex] / scale);
        buf.putShort((short) (b1 << 8 | b2));
      } else {
        buf.putShort((short) 0);
        buf.putShort((short) 0);
        buf.putShort((short) 0);
        buf.putShort((short) -1);
      }
    }
  }

  class PointWriterFP implements PointWriter {
    public void write(ByteBuffer buf, Frame frame, int pointIndex, float scale) {
      if (frame.isValid(pointIndex)) {
        buf.putFloat(frame.x[pointIndex]);
        buf.putFloat(frame.y[pointIndex]);
        buf.putFloat(frame.z[pointIndex]);
        int b1 = frame.camMask[pointIndex];
        int b2 = (byte) (frame.residual[pointIndex] / -scale);
        buf.putFloat(b1 << 8 | b2);
      } else {
        buf.putFloat(0);
        buf.putFloat(0);
        buf.putFloat(0);
        buf.putFloat(-1);
      }
    }
  }

  interface ChannelWriter {
    void write(ByteBuffer buf, Frame frame, int sampleIndex, int channelIndex);
  }

  class ChannelWriterSI implements ChannelWriter {
    public void write(ByteBuffer buf, Frame frame, int sampleIndex, int channelIndex) {
      float value = frame.analogValues[sampleIndex][channelIndex];
      if (analogOffset != null && analogScale != null && analogGenScale != null) {
        buf.putShort((short) (value / analogGenScale[0] / analogScale[channelIndex] + analogOffset[channelIndex]));
      } else {
        buf.putShort((short) value);
      }
    }
  }

  class ChannelWriterFP implements ChannelWriter {
    public void write(ByteBuffer buf, Frame frame, int sampleIndex, int channelIndex) {
      float value = frame.analogValues[sampleIndex][channelIndex];
      if (analogOffset != null && analogScale != null && analogGenScale != null) {
        buf.putFloat((value / analogGenScale[0] / analogScale[channelIndex] + analogOffset[channelIndex]));
      } else {
        buf.putFloat(value);
      }
    }
  }
}
