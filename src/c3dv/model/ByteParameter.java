package c3dv.model;

import java.util.Arrays;

/**
 * C3D parameter with type byte.
 * 
 * @author justin
 */
public class ByteParameter extends Parameter {

  int[] data;

  public ByteParameter(String name, int groupID, int[] dimensions, Buffer dataBuf, String description,
      boolean locked) {
    super(name, groupID, dimensions, description, locked);
    data = (dataBuf == null) ? null : dataBuf.getUBytes(dataBuf.array().length);
  }

  @Override
  public String typeName() {
    return "Byte";
  }

  @Override
  public String dataString() {
    return Arrays.toString(data);
  }

  @Override
  public byte[] getDataBytes() {
    if (data == null)
      return null;

    byte[] bytes = new byte[data.length];
    for (int i = 0; i < bytes.length; i++)
      bytes[i] = (byte) data[i];
    return bytes;
  }
}
