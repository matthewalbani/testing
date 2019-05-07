package com.squareup.testing;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ChunkConfigTest {
  @Test public void testOneChunk() {
    ChunkConfig chunkConfig = new ChunkConfig(1, 1, false);
    ChunkIndexes chunkIndexes = chunkConfig.getChunkIndexes(4);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(0);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(4);
  }

  @Test public void testSizeLessThanChunks() {
    ChunkConfig chunkConfig = new ChunkConfig(5, 1, false);
    ChunkIndexes chunkIndexes = chunkConfig.getChunkIndexes(4);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(0);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(1);
  }

  @Test public void testNegativeChunk() {
    assertThatThrownBy(() -> {
      new ChunkConfig(-1, 5, false); }
    ).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("chunks must be greater than or equal to zero");
  }

  @Test public void testNegativeChunkZero() {
    assertThatThrownBy(() -> {
      new ChunkConfig(5, -1, false); }
      ).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("runChunk must be greater than or equal to zero");
  }

  @Test public void testZero() {
    ChunkConfig chunkConfig = new ChunkConfig(0, 0, false);
    ChunkIndexes chunkIndexes = chunkConfig.getChunkIndexes(4);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(0);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(0);
  }

  @Test public void testSizeLessThanChunksAndRunChunk() {
    ChunkConfig chunkConfig = new ChunkConfig(5, 1, false);
    ChunkIndexes chunkIndexes = chunkConfig.getChunkIndexes(4);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(0);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(1);

    chunkConfig = new ChunkConfig(5, 4, false);
    chunkIndexes = chunkConfig.getChunkIndexes(4);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(3);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(4);

    chunkConfig = new ChunkConfig(5, 5, false);
    chunkIndexes = chunkConfig.getChunkIndexes(4);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(0);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(0);
  }

  @Test public void testChunksDivideEvenly() {
    ChunkConfig chunkConfig = new ChunkConfig(5, 1, false);
    ChunkIndexes chunkIndexes = chunkConfig.getChunkIndexes(5);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(0);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(1);

    chunkConfig = new ChunkConfig(5, 5, false);
    chunkIndexes = chunkConfig.getChunkIndexes(5);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(4);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(5);
  }

  @Test public void testChunksDivideUnEvenly() {
    ChunkConfig chunkConfig = new ChunkConfig(5, 1, false);
    ChunkIndexes chunkIndexes = chunkConfig.getChunkIndexes(6);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(0);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(2);

    chunkConfig = new ChunkConfig(5, 5, false);
    chunkIndexes = chunkConfig.getChunkIndexes(6);
    assertThat(chunkIndexes.getStartIndex()).isEqualTo(5);
    assertThat(chunkIndexes.getEndIndex()).isEqualTo(6);
  }

  @Test public void testGetChunksDivideUnEvenlyWithLargerNumber() {
    ChunkConfig chunkConfig;
    ChunkIndexes chunkIndexes;
    for(int i = 1; i <= 109; i++) {
      chunkConfig = new ChunkConfig(109, i, false);
      chunkIndexes = chunkConfig.getChunkIndexes(2778);

      if (i <= 53) {
        assertThat(chunkIndexes.size()).isEqualTo(26);
      } else {
        assertThat(chunkIndexes.size()).isEqualTo(25);
      }
      assertThat(chunkIndexes.getEndIndex()).isLessThanOrEqualTo(2778);
    }
  }
}
