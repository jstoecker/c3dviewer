package c3dv.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Describes a feature of the C3D data.
 * 
 * @author Justin Stoecker
 */
public abstract class Parameter {

  public boolean        locked = false;
  public String         name;
  public int            id;
  public ParameterGroup group;
  public String         description;

  /** Null if scalar; otherwise, dimensions[i] is the length of the i-th dimension */
  public int[]          dimensions;

  public Parameter(String name, int groupID, int[] dimensions, String description, boolean locked) {
    this.name = name;
    this.id = groupID;
    this.dimensions = dimensions;
    this.description = description;
    this.locked = locked;
  }

  public abstract String typeName();

  public abstract String dataString();

  public abstract byte[] getDataBytes();

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(typeName());
    sb.append("\nName : " + name);
    sb.append("\nLocked : " + locked);
    sb.append("\nDescription : " + description);
    sb.append("\nDimensions: " + (dimensions == null ? 1 : Arrays.toString(dimensions)));
    sb.append("\nData: " + dataString());
    return sb.toString();
  }
}
