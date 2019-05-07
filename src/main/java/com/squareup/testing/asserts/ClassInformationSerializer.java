// Copyright 2013 Square, Inc.
package com.squareup.testing.asserts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.common.json.DateTimeTypeConverter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

/**
 * This knows how to serialize with class information so you know the instances are the same type as
 * you expect. Only used internally.
 */
class ClassInformationSerializer implements TypeAdapterFactory {
  private static final String META_KEY = "__CLASS";
  private static final String PRIMITIVE_VALUE_KEY = "__VALUE";
  private static final Gson rawGson = new GsonBuilder()
      .serializeNulls()
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();

  public JsonElement serialize(Object o, Type type,
      JsonSerializationContext jsonSerializationContext) {
    JsonElement elem = jsonSerializationContext.serialize(o, o.getClass());
    elem.getAsJsonObject().addProperty(PRIMITIVE_VALUE_KEY, o.getClass().getCanonicalName());
    return elem;
  }

  @Override public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> tTypeToken) {
    final TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(this, tTypeToken);
    final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

    return new TypeAdapter<T>() {
      @Override public void write(JsonWriter jsonWriter, T t) throws IOException {
        JsonElement tree = delegateAdapter.toJsonTree(t);
        if (t == null) {
          elementAdapter.write(jsonWriter, tree);
        } else {
          Class<?> clazz = t.getClass();
          // Treat all lists and maps as equivalent.
          // The protobuf runtime has an internal list adapter wich breaks equivalence tests
          // with normal lists.
          if (List.class.isAssignableFrom(clazz)) {
            clazz = List.class;
          }
          if (Map.class.isAssignableFrom(clazz)) {
            clazz = Map.class;
          }

          if (tree.isJsonObject()) {
            tree.getAsJsonObject()
                .add(META_KEY, new JsonPrimitive(clazz.getCanonicalName()));
            elementAdapter.write(jsonWriter, tree);
          } else {
            JsonObject e = new JsonObject();
            e.add(META_KEY, new JsonPrimitive(clazz.toString()));
            JsonPrimitive primitive;
            // This is ugly but it makes the json look better (ex: prevents wrapping non-strings as
            // strings). Reflection would be worse.
            if (t instanceof Boolean) {
              primitive = new JsonPrimitive((Boolean) t);
            } else if (t instanceof Number) {
              primitive = new JsonPrimitive((Number) t);
            } else if (t instanceof Character) {
              primitive = new JsonPrimitive((Character) t);
            } else if (t instanceof String) {
              primitive = new JsonPrimitive((String) t);
            } else {
              // could be an array
              primitive = new JsonPrimitive(rawGson.toJson(t));
            }
            e.add(PRIMITIVE_VALUE_KEY, primitive);
            elementAdapter.write(jsonWriter, e);
          }
        }
      }

      @Override public T read(JsonReader jsonReader) throws IOException {
        throw new RuntimeException("Not implemented.");
      }
    };
  }
}
