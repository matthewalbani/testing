package com.squareup.testing;

import java.io.PrintStream;
import java.util.List;
import org.junit.runner.manipulation.Filter;

public interface ShardingStrategy {
  void setTestClasses(List<Class<?>> testClasses);

  List<Class<?>> getClassesForChunk(ChunkConfig chunkConfig, PrintStream out);

  Filter getFilter(ChunkConfig chunkConfig);
}
