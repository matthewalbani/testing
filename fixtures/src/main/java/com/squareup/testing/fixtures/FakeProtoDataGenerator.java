// Copyright 2017 Square, Inc.
package com.squareup.testing.fixtures;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.DynamicMessage;
import java.util.Random;
import java.util.Set;

class FakeProtoDataGenerator {
  private final Random random;

  FakeProtoDataGenerator(Random random) {
    this.random = random;
  }

  DynamicMessage generateFakeMessage(Descriptor descriptor) {
    return generateFakeMessage(descriptor, ImmutableSet.of());
  }

  private DynamicMessage generateFakeMessage(Descriptor descriptor, Set<Descriptor> visited) {
    return generateFakeMessageBuilder(descriptor, visited).build();
  }

  // TODO: call this from ConcreteFakeAnnotations for builder fields.
  private DynamicMessage.Builder generateFakeMessageBuilder(Descriptor descriptor,
      Set<Descriptor> visited) {
    DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);

    if (!visited.contains(descriptor)) { // do not recurse infinitely
      Set<Descriptor> newVisited = ImmutableSet.<Descriptor>builder()
          .addAll(visited)
          .add(descriptor)
          .build();

      for (FieldDescriptor fieldDescriptor : descriptor.getFields()) {
        if (!fieldDescriptor.isRepeated()) {
          Object obj = getObjForType(fieldDescriptor, newVisited);
          builder.setField(fieldDescriptor, obj);
        } else {
          int i = 1 + random.nextInt(3);
          for (int j = 0; j < i; j++) {
            Object obj = getObjForType(fieldDescriptor, newVisited);
            builder.addRepeatedField(fieldDescriptor, obj);
          }
        }
      }
    }
    return builder;
  }

  private Object getObjForType(FieldDescriptor fieldDescriptor, Set<Descriptor> visited) {
    Type type = fieldDescriptor.getType();
    switch (type) {
      case DOUBLE:
        return random.nextDouble();
      case FLOAT:
        return random.nextFloat();
      case INT64:
        return random.nextLong();
      case UINT64:
        return random.nextLong();
      case INT32:
        return random.nextInt();
      case FIXED64:
        return random.nextLong();
      case FIXED32:
        return random.nextInt();
      case BOOL:
        return random.nextBoolean();
      case STRING:
        return fieldDescriptor.getName() + random.nextInt();
      case BYTES:
        int byteSize = random.nextInt(16);
        byte[] bytes = new byte[byteSize];
        random.nextBytes(bytes);
        return ByteString.copyFrom(bytes);
      case UINT32:
        return random.nextInt();
      case SFIXED32:
        return random.nextInt();
      case SFIXED64:
        return random.nextLong();
      case SINT32:
        // Special case for Time.DateTime.timezone_offset_min:
        // According to https://en.wikipedia.org/wiki/List_of_UTC_time_offsets, these range from
        // -12:00 to +14:00.
        if (fieldDescriptor.getName().equals("timezone_offset_min")) {
          int rangeMinutes = (14 - (-12))*60;
          return random.nextInt(rangeMinutes) - 12*60;
        }
        // A better solution would be this class respecting squareup.validation.range and
        // encoding this bound in the Time.DateTime proto
        // TODO(traviscj) do ^^
        return random.nextInt();
      case SINT64:
        return random.nextLong();
      case GROUP:
        break;
      case MESSAGE:
        Descriptor messageType = fieldDescriptor.getMessageType();
        return generateFakeMessage(messageType, visited);
      case ENUM:
        EnumDescriptor enumType = fieldDescriptor.getEnumType();
        int size = enumType.getValues().size();
        int i = random.nextInt(size);
        return enumType.getValues().get(i);
    }
    throw new RuntimeException("unknown type!");
  }
}
