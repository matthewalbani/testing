package com.squareup.testing.asserts;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Compares {@link JsonElement}s.
 *
 * <p>Each element is considered one of the following types:
 *   <ol>
 *     <li>null</li>
 *     <li>boolean</li>
 *     <li>number</li>
 *     <li>string</li>
 *     <li>array</li>
 *     <li>object</li>
 *   </ol>
 *   Elements of different types are compared to each other using that order, i.e. objects are
 *   greater than arrays, arrays are greater than strings, etc. Elements of the same type are
 *   compared to each other using the following rules:
 *   <ul>
 *     <li>All nulls are equal.</li>
 *     <li>Boolean are compared using {@link Boolean#compareTo(Object)}.</li>
 *     <li>Numbers are compared using {@link Double#compareTo(Object)}.</li>
 *     <li>Strings are compared using {@link String#compareTo(String)}.</li>
 *     <li>
 *       When comparing arrays, each element in the first array is compared to the element at the
 *       same index in the second array via a recursive call to
 *       {@link #compare(com.google.gson.JsonElement, com.google.gson.JsonElement)}. Comparison
 *       starts at index zero and proceeds until two unequal elements are found or the end of the
 *       smallest array is reached. If the end of the smallest array is reached, the array sizes are
 *       compared.
 *     </li>
 *     <li>
 *       When comparing objects, a set of each object's names is sorted and each name in the first
 *       set is compared with the name at the same position in the second set. If two names are
 *       equal, the corresponding values are compared via a recursive call to
 *       {@link #compare(com.google.gson.JsonElement, com.google.gson.JsonElement)}. Comparison
 *       continues until either two unequal names are found, two equal names with unequal values are
 *       found, or the end of the smallest set is reached. If the end of the smallest set is
 *       reached, the set sizes are compared.
 *     </li>
 *   </ul>
 * </p>
 *
 * @author nickd@
 */
class JsonElementComparator implements Comparator<JsonElement> {

  enum ElementType {
    NULL(0),
    BOOLEAN(1),
    NUMBER(2),
    STRING(3),
    ARRAY(4),
    OBJECT(5);

    private final int sortOrder;

    private ElementType(int sortOrder) {
      this.sortOrder = sortOrder;
    }
  }

  @Override public int compare(final JsonElement one, final JsonElement two) {
    Preconditions.checkNotNull(one);
    Preconditions.checkNotNull(two);

    if (one.equals(two)) {
      return 0;
    }

    ElementType elementTypeOne = getElementType(one);
    ElementType elementTypeTwo = getElementType(two);

    if (elementTypeOne != elementTypeTwo) {
      return ((Integer) elementTypeOne.sortOrder).compareTo(elementTypeTwo.sortOrder);
    }

    switch (elementTypeOne) {
      case NULL:
        throw new AssertionError("Null types should always be equal");
      case BOOLEAN:
        return ((Boolean)
            one.getAsJsonPrimitive().getAsBoolean()).compareTo(
            two.getAsJsonPrimitive().getAsBoolean());
      case NUMBER:
        return ((Double)
            one.getAsJsonPrimitive().getAsDouble()).compareTo(
            two.getAsJsonPrimitive().getAsDouble());
      case STRING:
        return (
            one.getAsJsonPrimitive().getAsString()).compareTo(
            two.getAsJsonPrimitive().getAsString());
      case ARRAY:
        JsonArray arrayOne = one.getAsJsonArray();
        JsonArray arrayTwo = two.getAsJsonArray();

        for (int i = 0; i < Math.min(arrayOne.size(), arrayTwo.size()); i++) {
          int compare = compare(arrayOne.get(i), arrayTwo.get(i));
          if (compare != 0) {
            return compare;
          }
        }
        return ((Integer) arrayOne.size()).compareTo(arrayTwo.size());
      case OBJECT:
        Set<Map.Entry<String,JsonElement>> oneEntries = one.getAsJsonObject().entrySet();
        Set<Map.Entry<String, JsonElement>> twoEntries = two.getAsJsonObject().entrySet();

        // If either object is empty, compare their sizes.
        if (oneEntries.size() == 0 || twoEntries.size() == 0) {
          return ((Integer) oneEntries.size()).compareTo(twoEntries.size());
        }

        Map<String, JsonElement> oneMap = Maps.newTreeMap();
        for (Map.Entry<String, JsonElement> entry : oneEntries) {
          oneMap.put(entry.getKey(), entry.getValue());
        }

        Map<String, JsonElement> twoMap = Maps.newTreeMap();
        for (Map.Entry<String, JsonElement> entry : twoEntries) {
          twoMap.put(entry.getKey(), entry.getValue());
        }

        // Compare corresponding keys and values looking for the first difference.
        Iterator<Map.Entry<String, JsonElement>> iteratorOne = oneMap.entrySet().iterator();
        Iterator<Map.Entry<String, JsonElement>> iteratorTwo = twoMap.entrySet().iterator();
        while (iteratorOne.hasNext() && iteratorTwo.hasNext()) {
          Map.Entry<String, JsonElement> entryOne = iteratorOne.next();
          Map.Entry<String, JsonElement> entryTwo = iteratorTwo.next();
          int compare = entryOne.getKey().compareTo(entryTwo.getKey());
          if (compare != 0) {
            return compare;
          } else {
            compare = compare(entryOne.getValue(), entryTwo.getValue());
            if (compare != 0) {
              return compare;
            }
          }
        }
        // All corresponding names match. Use size.
        return ((Integer) oneEntries.size()).compareTo(twoEntries.size());
      default:
        throw new AssertionError("Unknown ElementType: " + elementTypeOne);
    }
  }

  static ElementType getElementType(JsonElement element) {
    if (element.isJsonArray()) {
      return ElementType.ARRAY;
    } else if (element.isJsonNull()) {
      return ElementType.NULL;
    } else if (element.isJsonObject()) {
      return ElementType.OBJECT;
    } else if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isBoolean()) {
        return ElementType.BOOLEAN;
      } else if (primitive.isString()) {
        return ElementType.STRING;
      } else if (primitive.isNumber()) {
        return ElementType.NUMBER;
      } else {
        throw new AssertionError("Unknown JsonPrimitive type: " + primitive);
      }
    } else {
      throw new AssertionError("Unknown JsonElement type.");
    }
  }
}
