package com.squareup.testing.asserts;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;

/**
 * Sorts a {@link JsonElement}. If the specified JsonElement is an object or array, sorting is
 * accomplished by applying {@link JsonElementComparator} to every nested object or array,
 * depth-first, then to the specified object or array itself. If the specified JsonElement is a
 * primitive (null, boolean, number, string), it is returned unchanged.
 *
 * @author nickd@
 */
public final class JsonElementSorter {
  private JsonElementSorter() { }

  /**
   * Returns a sorted version of the specified JsonElement.
   */
  public static JsonElement sort(final JsonElement element) {
    JsonElementComparator.ElementType elementType = JsonElementComparator.getElementType(element);

    switch (elementType) {
      case NULL:
      case BOOLEAN:
      case NUMBER:
      case STRING:
        return element;
      case ARRAY:
        JsonArray sortedArray = new JsonArray();
        for (JsonElement currentElement : element.getAsJsonArray()) {
          sortedArray.add(sort(currentElement));
        }
        return sortedArray;
      case OBJECT:
        Map<String, JsonElement> map = Maps.newTreeMap();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
          map.put(entry.getKey(), sort(entry.getValue()));
        }
        JsonObject sortedObject = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
          sortedObject.add(entry.getKey(), entry.getValue());
        }
        return sortedObject;
      default:
        throw new AssertionError("Unknown ElementType: " + elementType);
    }
  }
}
