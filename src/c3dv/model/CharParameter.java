package c3dv.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * C3D parameter with type character.
 * 
 * @author justin
 */
public class CharParameter extends Parameter {

  public String[] data;

  public CharParameter(String name, int groupID, int[] dimensions, Buffer dataBuf, String description,
      boolean locked) {
    super(name, groupID, dimensions, description, locked);

    if (dataBuf == null) {
      data = null;
    } else if (dimensions == null || dimensions.length == 1) {
      data = new String[] { new String(dataBuf.array()) };
    } else if (dimensions.length == 2) {
      data = dataBuf.getStrings(dimensions[0], dimensions[1]);
    }
  }

  @Override
  public String typeName() {
    return "Char";
  }

  @Override
  public String dataString() {
    return Arrays.toString(data);
  }

  @Override
  public byte[] getDataBytes() {
    if (data == null)
      return null;
    
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    try {
      for (String s : data)
        stream.write(s.getBytes());
    } catch (IOException e) {
    }
    return stream.toByteArray();
  }
}
