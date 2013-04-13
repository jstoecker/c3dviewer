package c3dv.model;

/**
 * Buffer that wraps a byte array and contains utility methods to parse a C3D file.
 * 
 * @author Justin Stoecker
 */
public class Buffer {

  public enum ByteOrder {
    LITTLE_ENDIAN,
    MIDDLE_ENDIAN,
    BIG_ENDIAN
  }

  private int          position = 0;
  private ByteOrder    order    = ByteOrder.LITTLE_ENDIAN;
  private final byte[] data;

  public Buffer(byte[] data) {
    this.data = data;
  }

  /** Sets the byte order */
  public void setOrder(ByteOrder order) {
    this.order = order;
  }
  
  /** @return The underlying array of bytes. */
  public byte[] array() {
    return data;
  }

  // POSITION
  // ----------------------------------------------------------------------------------------------

  /** @return The current buffer position. */
  public int getPosition() {
    return position;
  }

  /** Sets the current buffer position to the start of a byte (0-based indexing). */
  public void setPosition(int byteIndex) {
    this.position = byteIndex;
  }

  /** Sets the current buffer position (1-based indexing). */
  public void setPositionToC3DByte(int byteIndex) {
    setPosition(byteIndex - 1);
  }

  /** Sets the current buffer position to the start of a word (1-based indexing). A word is 2 bytes. */
  public void setPositionToC3DWord(int wordIndex) {
    setPosition((wordIndex - 1) * 2);
  }

  /**
   * Sets the current buffer position to the start of a block (1-based indexing). A block is 512
   * bytes.
   */
  public void setPositionToC3DBlock(int blockIndex) {
    setPosition((blockIndex - 1) * 512);
  }

  // SIGNED BYTE (8-bit)
  // ----------------------------------------------------------------------------------------------

  /** @return The (signed) byte at position i (0-based indexing). */
  public byte getSByte(int i) {
    return data[i];
  }

  /** @return The (signed) n bytes starting at position i (0-based indexing). */
  public byte[] getSBytes(int i, int n) {
    byte[] buf = new byte[n];
    System.arraycopy(data, i, buf, 0, n);
    return buf;
  }

  /** Reads 1 byte, increments position by 1, and returns the (signed) byte. */
  public byte getSByte() {
    byte b = getSByte(position);
    position++;
    return b;
  }

  /** Reads n bytes, increments position by n, and returns the (signed) bytes. */
  public byte[] getSBytes(int numBytes) {
    byte[] buf = getSBytes(position, numBytes);
    position += numBytes;
    return buf;
  }

  // UNSIGNED BYTE (8-bit) : Java bytes are signed, so an unsigned representation is returned
  // ----------------------------------------------------------------------------------------------

  /** @return The unsigned representation of the byte at position i (0-based indexing). */
  public int getUByte(int i) {
    return (data[i] & 0xff);
  }

  /** @return The unsigned representation of n bytes starting at position i (0-based indexing). */
  public int[] getUBytes(int i, int n) {
    int[] buf = new int[n];
    for (int j = 0; j < n; j++)
      buf[i] = getUByte(i + j);
    return buf;
  }

  /** Reads 1 byte, increments position by 1, and returns an unsigned representation of the byte. */
  public int getUByte() {
    int b = getUByte(position);
    position++;
    return b;
  }

  /** Reads n bytes, increments position by n, and returns the unsigned representations. */
  public int[] getUBytes(int n) {
    int[] buf = getUBytes(position, n);
    position += n;
    return buf;
  }

  // SIGNED SHORT (16-bit)
  // ----------------------------------------------------------------------------------------------

  /** @return The (signed) short at position i (0-based indexing). */
  public short getSShort(int i) {
    return (short)getUShort(i);
  }

  /** Reads 2 bytes, increments position by 2, and returns the (signed) short. */
  public short getSShort() {
    short s = getSShort(position);
    position += 2;
    return s;
  }
  
  /** Reads the next n shorts from the buffer, incrementing the position by 2 * n. */
  public short[] getSShorts(int n) {
    short[] shorts = new short[n];
    for (int i = 0; i < n; i++)
      shorts[i] = getSShort();
    return shorts;
  }

  // UNSIGNED SHORT (16-bit) : Java shorts are signed, so an unsigned representation is returned
  // ----------------------------------------------------------------------------------------------

  /** @return The unsigned representation of the short at position i (0-based indexing). */
  public int getUShort(int i) {
    int b0 = getUByte(i);
    int b1 = getUByte(i + 1);

    switch (order) {
    default:
    case MIDDLE_ENDIAN:
    case LITTLE_ENDIAN:
      return (b1 << 8 | b0);
    case BIG_ENDIAN:
      return (b0 << 8 | b1);
    }
  }

  /** Reads 2 bytes, increments position by 2, and returns an unsigned representation of the short. */
  public int getUShort() {
    int s = getUShort(position);
    position += 2;
    return s;
  }
  
  // SIGNED INT (32-bit)
  // ----------------------------------------------------------------------------------------------

  /** @return The (signed) integer at position i (0-based indexing). */
  public int getSInteger(int i) {
    int b0 = getUByte(i);
    int b1 = getUByte(i + 1);
    int b2 = getUByte(i + 2);
    int b3 = getUByte(i + 3);
    
    // TO BIG ENDIAN
    switch (order) {
    default:
    case MIDDLE_ENDIAN:
      if (b0 != 0 || b1 != 0 || b2 != 0 || b3 != 0)
        b1--;
      return (b1 << 24 | b0 << 16 | b3 << 8 | b2);
    case LITTLE_ENDIAN:
      return (b3 << 24 | b2 << 16 | b1 << 8 | b0);
    case BIG_ENDIAN:
      return (b0 << 24 | b1 << 16 | b2 << 8 | b3);
    }
  }
  
  /** Reads 4 bytes, increments position by 4, and returns the (signed) integer. */
  public int getSInteger() {
    int i = getSInteger(position);
    position += 4;
    return i;
  }
  
  // FLOAT (32-bit)
  // ----------------------------------------------------------------------------------------------

  /** @return The float at position i (0-based indexing). */
  public float getFloat(int i) {
    return Float.intBitsToFloat(getSInteger(i));
  }
  
  /** Reads 4 bytes, increments position by 4, and returns the float. */
  public float getFloat() {
    float f = getFloat(position);
    position += 4;
    return f;
  }
  
  /** Reads the next n floats from the buffer, incrementing the position by 4 * n. */
  public float[] getFloats(int n) {
    float[] floats = new float[n];
    for (int i = 0; i < n; i++)
      floats[i] = getFloat();
    return floats;
  }
  
  // STRING (n*8-bit)
  // ----------------------------------------------------------------------------------------------

  /** @return The string at position i (0-based indexing) with length. */
  public String getString(int i, int length) {
    return (length == 0) ? null : new String(data, i, length);
  }
  
  /** Reads length bytes, increments position by length, and returns the string. */
  public String getString(int length) {
    String s = getString(position, length);
    position += length;
    return s;
  }
  
  /** Reads the next n strings from the buffer, incrementing the position by length * n. */
  public String[] getStrings(int length, int n) {
    String[] strings = new String[n];
    for (int i = 0; i < n; i++)
      strings[i] = getString(length);
    return strings;
  }
}
