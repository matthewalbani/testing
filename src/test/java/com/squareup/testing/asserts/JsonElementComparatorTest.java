package com.squareup.testing.asserts;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonElementComparatorTest {
  JsonElementComparator comparator = new JsonElementComparator();
  Gson gson = new Gson();

  @Test
  public void testNulls() {
    assertTrue(comparator.compare(JsonNull.INSTANCE, JsonNull.INSTANCE) == 0);
  }

  @Test
  public void testNumbers_lessThan() {
    assertTrue(comparator.compare(new JsonPrimitive(1), new JsonPrimitive(2)) < 0);
  }

  @Test
  public void testNumbers_greaterThan() {
    assertTrue(comparator.compare(new JsonPrimitive(2), new JsonPrimitive(1)) > 0);
  }

  @Test
  public void testNumbers_equalTo() {
    assertTrue(comparator.compare(new JsonPrimitive(2), new JsonPrimitive(2)) == 0);
  }

  @Test
  @SuppressWarnings("SelfComparison")
  public void testBooleans() {
    assertEquals(
        comparator.compare(new JsonPrimitive(true), new JsonPrimitive(false)),
        Boolean.TRUE.compareTo(Boolean.FALSE));
    assertEquals(
        comparator.compare(new JsonPrimitive(false), new JsonPrimitive(true)),
        Boolean.FALSE.compareTo(Boolean.TRUE));
    assertEquals(comparator.compare(new JsonPrimitive(true), new JsonPrimitive(true)),
        Boolean.TRUE.compareTo(Boolean.TRUE));
    assertEquals(
        comparator.compare(new JsonPrimitive(false), new JsonPrimitive(false)),
        Boolean.FALSE.compareTo(Boolean.FALSE));
  }

  @Test
  public void testArrays_empty() {
    JsonElement jsonOne = gson.fromJson("[]", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("[]", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) == 0);
  }

  @Test
  public void testArrays_equal() {
    JsonElement jsonOne = gson.fromJson("[1]", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("[1]", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) == 0);
  }

  @Test
  public void testArrays_firstElementDifferent() {
    JsonElement jsonOne = gson.fromJson("[1,2]", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("[2,2]", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) < 0);
  }

  @Test
  public void testArrays_secondElementDifferent() {
    JsonElement jsonOne = gson.fromJson("[1,4]", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("[1,3]", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) > 0);
  }

  @Test
  public void testArrays_arraysOfArrays() {
    JsonElement jsonOne = gson.fromJson("[[1,2],[1,2]]", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("[[1,2],[1,3]]", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) < 0);
  }

  @Test
  public void testArrays_size() {
    JsonElement jsonOne = gson.fromJson("[[1,2],[1,3]]", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("[[1,2],[1,3],[1,4]]", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) < 0);
  }

  @Test
  public void testObjects_empty() {
    JsonElement jsonOne = gson.fromJson("{}", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("{}", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) == 0);
  }

  @Test
  public void testObjects_keysDiffer() {
    JsonElement jsonOne = gson.fromJson("{'a':1,'c':1}", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("{'a':1,'b':2}", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) > 0);
  }

  @Test
  public void testObjects_keysMatch() {
    JsonElement jsonOne = gson.fromJson("{'a':1,'b':1}", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("{'a':1,'b':2}", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) < 0);
  }

  @Test
  public void testObjects_size() {
    JsonElement jsonOne = gson.fromJson("{'a':1,'b':1}", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("{'a':1,'b':1,'c':1}", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) < 0);
  }

  @Test
  public void testObjects_typeOrder() {
    JsonElement jsonOne = gson.fromJson("null", JsonElement.class);
    JsonElement jsonTwo = gson.fromJson("true", JsonElement.class);
    JsonElement jsonThree = gson.fromJson("1", JsonElement.class);
    JsonElement jsonFour = gson.fromJson("'a'", JsonElement.class);
    JsonElement jsonFive = gson.fromJson("[]", JsonElement.class);
    JsonElement jsonSix = gson.fromJson("{}", JsonElement.class);
    assertTrue(comparator.compare(jsonOne, jsonTwo) < 0);
    assertTrue(comparator.compare(jsonTwo, jsonThree) < 0);
    assertTrue(comparator.compare(jsonThree, jsonFour) < 0);
    assertTrue(comparator.compare(jsonFour, jsonFive) < 0);
    assertTrue(comparator.compare(jsonFive, jsonSix) < 0);
  }
}
