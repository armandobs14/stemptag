package org.apache.lucene.store;

import java.io.IOException;

public class GAEIndexInput extends IndexInput implements Cloneable {

  static final int BUFFER_SIZE = GAEFile.SEGMENT_LENGTH;

  private GAEFile file;
  
  private long length;

  private byte[] currentBuffer;
  
  private int currentBufferIndex;

  private int bufferPosition;
  
  private long bufferStart;
  
  private int bufferLength;

  GAEIndexInput(GAEFile f) throws IOException {
    file = f;
    length = file.getLength();
    if (length / BUFFER_SIZE >= Integer.MAX_VALUE) {
      throw new IOException("Too large RAMFile! " + length);
    }
    currentBufferIndex = -1;
    currentBuffer = null;
  }

  public void close() {  }

  public long length() {
    return length;
  }

  public byte readByte() throws IOException {
    if (bufferPosition >= bufferLength) {
      currentBufferIndex++;
      switchCurrentBuffer();
    }
    return currentBuffer[bufferPosition++];
  }

  public void readBytes(byte[] b, int offset, int len) throws IOException {
    while (len > 0) {
      if (bufferPosition >= bufferLength) {
        currentBufferIndex++;
        switchCurrentBuffer();
      }
      int remainInBuffer = bufferLength - bufferPosition;
      int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
      System.arraycopy(currentBuffer, bufferPosition, b, offset, bytesToCopy);
      offset += bytesToCopy;
      len -= bytesToCopy;
      bufferPosition += bytesToCopy;
    }
  }

  private final void switchCurrentBuffer() throws IOException {
    if (currentBufferIndex >= file.getSegmentCount()) {
      throw new IOException("Read past EOF");
    } else {
      currentBuffer = file.getSegment(currentBufferIndex);
      bufferPosition = 0;
      bufferStart = (long) BUFFER_SIZE * (long) currentBufferIndex;
      long buflen = length - bufferStart;
      bufferLength = buflen > BUFFER_SIZE ? BUFFER_SIZE : (int) buflen;
    }
  }

  public long getFilePointer() {
    return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
  }

  public void seek(long pos) throws IOException {
    if (currentBuffer == null || pos < bufferStart || pos >= bufferStart + BUFFER_SIZE) {
      currentBufferIndex = (int) (pos / BUFFER_SIZE);
      switchCurrentBuffer();
    }
    bufferPosition = (int) (pos % BUFFER_SIZE);
  }

}