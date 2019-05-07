// Copyright 2014 by Square, Inc.
package com.squareup.testing.base.scenario;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A builder for instances of type {@link T}. Used in conjunction with {@link ScenarioBuilder}.
 * The core of the {@link InstanceBuilder} is the {@link #attachableInstanceSupplier} function,
 * which maps an in-progress {@link ScenarioBuilder} to a new instance of type {@link T}.
 * Optionally, the user can supply
 * <ul>
 *   <li>A name for the instance builder.</li>
 *   <li>A series of transformations to be applied, in order, to the produced instance.</li>
 *   <li>A mutation to be applied. Sometimes it is more concise to set a mutable field than to
 *   construct a whole new instance from a prototype.</li>
 * </ul>
 *
 * The {@link InstanceBuilder} can be attached to a {@link ScenarioBuilder}, which can then invoke
 * it. This has two purposes:
 * <ul>
 *   <li>The scenario builder can create and store the instance created by this instance builder.
 *   </li>
 *   <li>The instance builder itself can use objects previously constructed by the scenario builder
 *   when constructing its instance.</li>
 * </ul>
 *
 * @param <T> the type of instance produced by this instance builder.
 */
public final class InstanceBuilder<T> {
  private final Function<ScenarioBuilder, T> attachableInstanceSupplier;
  private final TypeToken<T> instanceType;
  protected Optional<String> optionalName;
  protected List<BiFunction<ScenarioBuilder, T, T>> transformations;

  // Verify that the instance builder is only attached to one ScenarioBuilder.
  private boolean attached = false;
  // Populated after attaching this to a ScenarioBuilder.
  private Supplier<T> instanceSupplier;
  private ScenarioBuilder baseScenarioBuilder;
  // Verify that the instance builder only produces the instance once.
  private boolean producedTheInstance = false;

  public InstanceBuilder(Function<ScenarioBuilder, T> attachableInstanceSupplier, Class<T> klass) {
    this(attachableInstanceSupplier, TypeToken.of(klass));
  }

  public InstanceBuilder(Function<ScenarioBuilder, T> attachableInstanceSupplier,
      TypeToken<T> instanceType) {
    this.attachableInstanceSupplier = attachableInstanceSupplier;
    this.instanceType = instanceType;
    optionalName = Optional.empty();
    transformations = new ArrayList<>();
  }

  void attach(ScenarioBuilder scenarioBuilder) {
    Preconditions.checkArgument(!attached, "InstanceBuilder can only attach to a ScenarioBuilder "
        + "at most once.");
    baseScenarioBuilder = scenarioBuilder;
    instanceSupplier = () -> attachableInstanceSupplier.apply(scenarioBuilder);
    attached = true;
  }

  /**
   * Actually create, transform, and mutate the instance. Will only be called once.
   */
  protected T produceInstance() {
    Preconditions.checkArgument(!producedTheInstance, "InstanceBuilder must create its "
        + "instance at most once.");
    T beforeTransformation = instanceSupplier.get();
    // apply the transformations in order
    T instance = transformations.stream()
        .reduce(beforeTransformation, (t, f) -> f.apply(baseScenarioBuilder, t), (t1, t2) -> t2);
    producedTheInstance = true;
    return instance;
  }

  /**
   * Apply a name to the instance produced by this instance builder.
   *
   * @param name the name to use.
   * @return this builder.
   */
  public InstanceBuilder<T> named(String name) {
    optionalName = Optional.of(name);
    return this;
  }

  /**
   * Apply a transformation to the instances produced by this instance builder, with the scenario
   * builder in scope. Useful for instance builders of immutable objects.
   *
   * @param transformation the function to apply to instances built by this builder.
   * @return this builder.
   */
  public InstanceBuilder<T> map(BiFunction<ScenarioBuilder, T, T> transformation) {
    this.transformations.add(transformation);
    return this;
  }

  /**
   * Apply a unary operator to the instance produced by this instance builder
   *
   * @param transformation the unary operator to apply
   * @return this builder.
   */
  public InstanceBuilder<T> map(UnaryOperator<T> transformation) {
    this.transformations.add((b, t) -> transformation.apply(t));
    return this;
  }

  public Key getKey() {
    return Key.of(optionalName, instanceType);
  }
}
