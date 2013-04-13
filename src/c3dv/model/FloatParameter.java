package c3dv.model;

import java.nio.ByteBuffer;
import java.util.Arrays;

import c3dv.model.Buffer.ByteOrder;

/**
 * C3D parameter with type float.
 * 
 * @author justin
 */
public class FloatParameter extends Parameter {

  float[] data;

  public FloatParameter(String name, int groupID, int[] dimensions, Buffer dataBuf,
      String description, boolean locked) {
    super(name, groupID, dimensions, description, locked);

    if (dataBuf == null) {
      data = null;
    } else {
      data = dataBuf.getFloats(dataBuf.array().length / 4);
    }
  }

  @Override
  public String typeName() {
    return "Float";
  }

  @Override
  public String dataString() {
    return Arrays.toString(data);
  }

  @Override
  public byte[] getDataBytes() {
    if (data == null)
      return null;
    ByteBuffer buf = ByteBuffer.allocate(data.length * 4);
    buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    for (float f : data)
      buf.putFloat(f);
    return buf.array();
  }
}
