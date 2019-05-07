// Copyright 2014 by Square, Inc.
package com.squareup.testing.base.scenario;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class ScenarioBuilderTest {
  private static final String STRING_CONSTANT = "Jack Horsey";
  private ScenarioBuilder scenarioBuilder;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void before() {
    scenarioBuilder = new ScenarioBuilder();
  }

  @Test
  public void singleInstance() {
    Scenario scenario = scenarioBuilder.given(string()).build();
    assertThat(scenario.get(String.class)).isEqualTo(STRING_CONSTANT);
  }

  @Test
  public void singleNamedInstance() {
    Scenario scenario = scenarioBuilder.given(string().named("jack"))
        .build();
    assertThat(scenario.get("jack", String.class)).isEqualTo(STRING_CONSTANT);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Scenario does not contain unnamed java.lang.String.");
    scenario.get(String.class);
  }

  @Test
  public void dependency() {
    Scenario scenario = scenarioBuilder.given(sb ->
        new AtomicInteger(sb.satisfyWith(string()).length()), AtomicInteger.class)
        .build();
    assertThat(scenario.get(AtomicInteger.class).get()).isEqualTo(STRING_CONSTANT.length());
  }

  @Test
  public void parametrizedType() {
    Scenario scenario = scenarioBuilder
        .given(sb -> Lists.<String>newArrayList("a", "b"),
            new TypeToken<List<String>>() {
            })
        .build();
    assertThat(scenario.get(new TypeToken<List<String>>() {})).containsExactly("a", "b");
  }

  @Test
  public void satisfyWithDoesNotOverwrite() {
    Scenario scenario = scenarioBuilder
        .given(sb -> {
          AtomicInteger i = sb.satisfyWith(atomicInt());
          return Integer.toString(i.get());
        }, String.class)
        .given(atomicInt().map(i -> new AtomicInteger(i.get() + 1729)))
        .build();
    assertThat(scenario.get(String.class)).isEqualTo("1729");
  }

  @Test
  public void chainedTransformations() {
    Scenario scenario = scenarioBuilder
        .given(atomicInt()
            .map(i -> new AtomicInteger(i.get() + 5))
            .map(i -> new AtomicInteger(i.get() * 2)))
        .build();
    assertThat(scenario.get(AtomicInteger.class).get()).isEqualTo(10);
  }

  @Test
  public void informativeExceptionWithMissingDependency() {
    scenarioBuilder
        .given(sb -> {
          // perform a successful dependency resolution
          int val = sb.satisfyWith(atomicInt()).get();
          return sb.satisfy("puppies", String.class).length();
        }, Integer.class)
        .given(sb -> {
          // perform a successful dependency resolution
          String nonPuppy = sb.satisfyWith(string());
          // this will fail
          Map<String, String> myFavoriteThings = sb.satisfy("faves",
              new TypeToken<Map<String, String>>() {
              });
          return myFavoriteThings.get("puppies");
        }, String.class, "puppies");
    thrown.expect(UnsatisfiableDependencyException.class);
    thrown.expectMessage("Failed to satisfy instance builder dependency.\n"
        + "Could not find\n"
        + "\t(faves, java.util.Map<java.lang.String, java.lang.String>) while looking for\n"
        + "\t(puppies, java.lang.String) while looking for\n"
        + "\t(, java.lang.Integer)");
    scenarioBuilder.build();
  }

  @Test
  public void informativeExceptionWithCause() {
    scenarioBuilder
        .given(sb -> sb.satisfy("throws", String.class), String.class, "proxy")
        .given(sb -> {
          throw new UnsupportedOperationException();
        }, String.class, "throws");
    thrown.expect(UnsatisfiableDependencyException.class);
    thrown.expectCause(CoreMatchers.isA(UnsupportedOperationException.class));
    thrown.expectMessage("Failed to satisfy instance builder dependency.\n"
        + "Could not find\n"
        + "\t(throws, java.lang.String) while looking for\n"
        + "\t(proxy, java.lang.String)");
    scenarioBuilder.build();
  }

  @Test
  public void givenTransformation() {
    Scenario scenario = scenarioBuilder
        // Class
        .given(sb -> "banana", String.class)
        .givenTransformation((sb, s) -> s + "grams!", String.class)
            // Class + name
        .given(sb -> "cracker", String.class, "k1")
        .givenTransformation((sb, s) -> s + " jacks!", String.class, "k1")
            // TypeToken
        .given(sb -> 111, TypeToken.of(Integer.class))
        .givenTransformation((sb, i) -> i * 2, TypeToken.of(Integer.class))
            // TypeToken + name
        .given(sb -> "meow", TypeToken.of(String.class), "k2")
        .givenTransformation((sb, s) -> s + " mix!", TypeToken.of(String.class), "k2")
        .build();
    assertThat(scenario.get(String.class)).isEqualTo("bananagrams!");
    assertThat(scenario.get("k1", String.class)).isEqualTo("cracker jacks!");
    assertThat(scenario.get(TypeToken.of(Integer.class))).isEqualTo(222);
    assertThat(scenario.get("k2", TypeToken.of(String.class))).isEqualTo("meow mix!");
  }

  @Test
  public void givenTransformation_missingKey() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Cannot override non-existent InstanceBuilder. key=(, java.lang.Integer)");

    scenarioBuilder.givenTransformation((sb, i) -> i * 2, Integer.class);
  }

  private static InstanceBuilder<String> string() {
    return new InstanceBuilder<>(sb -> STRING_CONSTANT, String.class);
  }

  private static InstanceBuilder<AtomicInteger> atomicInt() {
    return new InstanceBuilder<>(sb -> new AtomicInteger(0), AtomicInteger.class);
  }
}
