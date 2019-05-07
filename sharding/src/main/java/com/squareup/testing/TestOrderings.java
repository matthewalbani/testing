package com.squareup.testing;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.Comparator;

public enum TestOrderings implements Comparator<Object> {
  NAME {
    @Override public int compare(Object left, Object right) {
      return left.toString().compareTo(right.toString());
    }
  },
  HASH_OF_NAME {
    @Override public int compare(Object left, Object right) {
      HashFunction hashFunction = Hashing.md5();
      String leftHash = hashFunction
          .hashString(left.toString(), Charsets.UTF_8)
          .toString();
      String rightHash = hashFunction
          .hashString(right.toString(), Charsets.UTF_8)
          .toString();
      return leftHash.compareTo(rightHash);
    }
  }
}
