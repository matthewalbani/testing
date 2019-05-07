package com.squareup.testing;

public class ChunkConfig {
  public final int chunks;
  public final int runChunk;
  public final boolean runFunctionalTests;

  public ChunkConfig(int chunks, int runChunk, boolean runFunctionalTests) {
    if (chunks < 0) {
      throw new IllegalArgumentException("chunks must be greater than or equal to zero");
    }
    if (runChunk < 0) {
      throw new IllegalArgumentException("runChunk must be greater than or equal to zero");
    }

    this.chunks = chunks;
    this.runChunk = runChunk;
    this.runFunctionalTests = runFunctionalTests;
  }

  public static ChunkConfig get() {
    String chunksString = System.getProperty("square.test.chunkCount");
    String runChunkString = System.getProperty("square.test.runChunk");
    boolean runFunctionalTests = Boolean.getBoolean("functionalTests");

    int chunks = chunksString == null ? 1 : Integer.parseInt(chunksString);
    int runChunk = runChunkString == null ? 1 : Integer.parseInt(runChunkString);

    return new ChunkConfig(chunks, runChunk, runFunctionalTests);
  }

  public ChunkIndexes getChunkIndexes(int size) {
    if (runChunk == 0 || chunks == 0 || runChunk > size) {
      return new ChunkIndexes(0, 0);
    }
    int chunkSize = size / chunks;

    // Spread the remainder evenly over the first chunks
    int remainder = size - (chunkSize * chunks);

    int startIndex = chunkSize * (runChunk - 1);
    if (runChunk > 1) {
      if (runChunk <=  remainder) {
        startIndex += (runChunk - 1);
      } else {
        startIndex += remainder;
      }
    }

    int endIndex = startIndex + chunkSize;
    if (runChunk <= remainder) {
      endIndex += 1;
    }

    return new ChunkIndexes(startIndex, endIndex);
  }
}
