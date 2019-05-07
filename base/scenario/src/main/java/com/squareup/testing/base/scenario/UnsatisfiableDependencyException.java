// Copyright 2014 by Square, Inc.
package com.squareup.testing.base.scenario;

import com.google.common.collect.ImmutableList;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Exception indicating that some dependency of an instance builder cannot be satisfied. That is, a
 * call to one of the {@link ScenarioBuilder#satisfy} methods has failed because the requested
 * named instance could not be found.
 */
public class UnsatisfiableDependencyException extends RuntimeException {
  public UnsatisfiableDependencyException(Stack<Key> keyTrace) {
    super(keyTraceToMessage(keyTrace));
  }

  public UnsatisfiableDependencyException(Stack<Key> keyTrace, Throwable cause) {
    super(keyTraceToMessage(keyTrace), cause);
  }

  private static String keyTraceToMessage(Stack<Key> keyTrace) {
    StringJoiner message = new StringJoiner("\n")
        .add("Failed to satisfy instance builder dependency.")
        .add("Could not find");
    ImmutableList<Key> allButFirst = ImmutableList.copyOf(keyTrace.stream()
            .skip(1)
            .collect(Collectors.toList())).reverse();
    allButFirst.forEach(key -> message.add("\t" + key + " while looking for"));
    message.add("\t" + keyTrace.get(0));
    return message.toString();
  }
}
