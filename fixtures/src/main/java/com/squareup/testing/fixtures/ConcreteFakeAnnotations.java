// Copyright 2017 Square, Inc.
package com.squareup.testing.fixtures;

import com.google.common.base.Preconditions;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.squareup.common.protobuf.MessageReflection;
import java.lang.reflect.Field;
import java.util.Random;

/**
 * constructs concrete fakes for strings & protobuf Messages.
 *
 * To use, annotate some field with {@literal @ConcreteFake} and run
 * {@literal ConcreteFakeAnnotations.initConcreteFakes(this);} in your {@literal #setUp} method.
 */
class ConcreteFakeAnnotations {
  static void initConcreteFakes(Object testClass) {
    Preconditions.checkNotNull(testClass);
    Class<?> aClass = testClass.getClass();
    Field[] fields = aClass.getDeclaredFields();
    for (Field field : fields) {
      ConcreteFake[] annotationsByType = field.getAnnotationsByType(ConcreteFake.class);
      if (annotationsByType.length == 1) {
        if (field.getType().equals(String.class)) {
          field.setAccessible(true);
          try {
            field.set(testClass, TestIdGenerator.prefixed(field.getName()));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        } else if (Message.class.isAssignableFrom(field.getType())) {
          MessageReflection<? extends Message> messageReflection =
              MessageReflection.forMessage((Class<? extends Message>) field.getType());

          FakeProtoDataGenerator fakeProtoDataGenerator = new FakeProtoDataGenerator(new Random());

          DynamicMessage dynamicMessage =
              fakeProtoDataGenerator.generateFakeMessage(messageReflection.getDescriptor());

          Message message = messageReflection.getDefaultInstance().toBuilder()
              .mergeFrom(dynamicMessage)
              .build();

          field.setAccessible(true);
          try {
            field.set(testClass, message);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        } else {
          throw new RuntimeException("initConcreteFakes doesn't support type " + field.getType());
        }
      }
    }
  }
}
