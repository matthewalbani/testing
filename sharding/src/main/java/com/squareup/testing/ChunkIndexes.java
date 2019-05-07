package com.squareup.testing;


public final class ChunkIndexes {
  private final int startIndex;
  private final int endIndex;

  ChunkIndexes(int startIndex, int endIndex) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public int size() { return endIndex - startIndex; }
}
