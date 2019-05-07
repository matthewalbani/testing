package com.squareup.testing.asserts;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonElementSorterTest {
  Gson gson = new Gson();

  @Test
  public void testArray() {
    compare(
        "[2,1,{\"b\":1,\"a\":2}]",
        "[2,1,{\"a\":2,\"b\":1}]");
  }

  @Test
  public void testObject() {
    compare(
        "{\"c\":{\"z\":1,\"x\":1},\"b\":1,\"a\":2}",
        "{\"a\":2,\"b\":1,\"c\":{\"x\":1,\"z\":1}}");
  }

  private void compare(String unsortedJson, String sortedJson) {
    JsonElement unsorted =
        gson.fromJson(unsortedJson, JsonElement.class);
    JsonElement sorted = JsonElementSorter.sort(unsorted);
    assertEquals(sortedJson, gson.toJson(sorted).replace(" ", ""));
  }
}
