package com.squareup.testing.asserts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Assert;

/**
 * Asserts for json elements that display convenient error messages if they fail.
 * @author nickd@
 */
public final class JsonAssert {
  private JsonAssert() { }

  public static void assertEquals(String expected, String actual) {
    JsonParser parser = new JsonParser();
    assertEquals(parser.parse(expected), parser.parse(actual));
  }

  public static void assertEquals(JsonElement expected, JsonElement actual) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    expected = JsonElementSorter.sort(expected);
    actual = JsonElementSorter.sort(actual);
    Assert.assertEquals(gson.toJson(expected), gson.toJson(actual));
  }
}
