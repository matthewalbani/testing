package com.squareup.testing.runtestonshardtests;

import com.squareup.testing.RunTestOnShard;
import org.junit.Test;

@RunTestOnShard(2)
public class Batch1Test {
  @Test public void testBatch1_test1() {
  }

  @Test public void testBatch1_test2() {
  }
}
