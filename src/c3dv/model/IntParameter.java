package c3dv.model;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * C3D parameter with type 16-bit integer.
 * 
 * @author justin
 */
public class IntParameter extends Parameter {

  short[] data;

  public IntParameter(String name, int groupID, int[] dimensions, Buffer dataBuf,
      String description, boolean locked) {
    super(name, groupID, dimensions, description, locked);

    data = (dataBuf == null) ? null : dataBuf.getSShorts(dataBuf.array().length / 2);
  }

  @Override
  public String typeName() {
    return "Int";
  }

  @Override
  public String dataString() {
    return Arrays.toString(data);
  }

  @Override
  public byte[] getDataBytes() {
    if (data == null)
      return null;
    ByteBuffer buf = ByteBuffer.allocate(data.length * 2);
    buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    for (short s : data)
      buf.putShort(s);
    return buf.array();
  }
}
