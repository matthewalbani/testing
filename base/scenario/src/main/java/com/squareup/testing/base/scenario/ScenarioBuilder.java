// Copyright 2014 by Square, Inc.
package com.squareup.testing.base.scenario;

import com.google.common.reflect.TypeToken;
import com.squareup.core.time.Clock;
import com.squareup.core.time.FakeClock;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A builder for {@link Scenario}s.
 */
public class ScenarioBuilder {
  protected final Map<Key, Object> cache;
  protected final Map<Key, InstanceBuilder> instanceBuilders;
  // The stack of keys currently being satisfied
  private Stack<Key> keyTrace;
  public final Clock clock;

  public ScenarioBuilder() {
    this(new FakeClock());
  }

  public ScenarioBuilder(FakeClock clock) {
    this.clock = clock;
    cache = new HashMap<>();
    // Iteration order will be insertion order, which makes the build method easier to reason about.
    instanceBuilders = new LinkedHashMap<>();
  }

  /**
   * Methods for registering instance builders inline.
   */
  public final <T> ScenarioBuilder given(Function<ScenarioBuilder, T> attachable,
      Class<T> klass, String name) {
    return given(attachable, TypeToken.of(klass), name);
  }

  public final <T> ScenarioBuilder given(Function<ScenarioBuilder, T> attachable,
      Class<T> klass) {
    return given(attachable, TypeToken.of(klass));
  }

  public final <T> ScenarioBuilder given(Function<ScenarioBuilder, T> attachable,
      TypeToken<T> instanceType, String name) {
    return given(new InstanceBuilder<>(attachable, instanceType).named(name));
  }

  public final <T> ScenarioBuilder given(Function<ScenarioBuilder, T> attachable,
      TypeToken<T> instanceType) {
    return given(new InstanceBuilder<>(attachable, instanceType));
  }

  /**
   * Apply a transformation to an {@link InstanceBuilder} that is already registered.
   *
   * @param transformation transformation function applied to the {@link InstanceBuilder}. When this
   * is invoked, it is passed the output of the existing {@link InstanceBuilder} along with the
   * {@link ScenarioBuilder}.
   * @param klass class that identifies a unique {@link InstanceBuilder}.
   * @throws IllegalArgumentException Thrown if an {@link InstanceBuilder} isn't already
   * registered for the key.
   */
  public final <T> ScenarioBuilder givenTransformation(
      BiFunction<ScenarioBuilder, T, T> transformation, Class<T> klass) {
    return givenOverride(transformation, Key.of(Optional.empty(), TypeToken.of(klass)));
  }

  public final <T> ScenarioBuilder givenTransformation(
      BiFunction<ScenarioBuilder, T, T> transformation, Class<T> klass, String name) {
    return givenOverride(transformation, Key.of(Optional.of(name), TypeToken.of(klass)));
  }

  public final <T> ScenarioBuilder givenTransformation(
      BiFunction<ScenarioBuilder, T, T> transformation, TypeToken<T> typeToken) {
    return givenOverride(transformation, Key.of(Optional.empty(), typeToken));
  }

  public final <T> ScenarioBuilder givenTransformation(
      BiFunction<ScenarioBuilder, T, T> transformation, TypeToken<T> typeToken, String name) {
    return givenOverride(transformation, Key.of(Optional.of(name), typeToken));
  }

  private <T> ScenarioBuilder givenOverride(
      BiFunction<ScenarioBuilder, T, T> transformation, Key key) {
    @SuppressWarnings("unchecked")
    InstanceBuilder<T> instanceBuilder = instanceBuilders.get(key);

    checkArgument(instanceBuilder != null, "Cannot override non-existent %s. key=%s",
        InstanceBuilder.class.getSimpleName(), key);

    instanceBuilder.map(transformation);
    return this;
  }

  /**
   * Register concrete instances of a type. Useful for cases where you have, e.g. a fake clock, and
   * want to share it between the scenario builder and the rest of your test.
   * @param instance the instance to use.
   * @param klass the class of the instance.
   * @param name the name of the instance.
   * @param <T> the type of the instance.
   * @return this builder.
   */
  public final <T> ScenarioBuilder givenThis(T instance, Class<T> klass, String name) {
    return given(sb -> instance, klass, name);
  }

  public final <T> ScenarioBuilder givenThis(T instance, Class<T> klass) {
    return given(sb -> instance, klass);
  }

  public final <T> ScenarioBuilder givenThis(T instance, TypeToken<T> instanceType, String name) {
    return given(sb -> instance, instanceType, name);
  }

  public final <T> ScenarioBuilder givenThis(T instance, TypeToken<T> instanceType) {
    return given(sb -> instance, instanceType);
  }

  /**
   * Attaches and registers the given instance builder to this scenario builder.
   *
   * @param detached the instance builder to attach.
   * @param <T> the type of the instance builder.
   * @return this scenario builder.
   */
  public final <T> ScenarioBuilder given(InstanceBuilder<T> detached) {
    detached.attach(this);
    instanceBuilders.put(detached.getKey(), detached);
    return this;
  }

  /**
   * The satisfy methods represent an obligation on the part of the ScenarioBuilder to supply an
   * instance of the given name and type to an {@link InstanceBuilder}. Lazy evaluation of instances
   * means that if a scenario has been constructed properly, and all dependencies have been
   * supplied, that the scenario builder will have an instance to give.
   *
   * The scenario builder keeps track of tracing information for the purposes of informative error
   * messages. Whenever this method is called, it pushes the desired dependency key on a stack. When
   * the dependency is successfully satisfied, it pops the stack. Thus, at any point, we know "how
   * we got here"--that is, how it is that we ended up trying to satisfy this dependency. See
   * {@link UnsatisfiableDependencyException} for how this information is used.
   *
   *
   * @param klass the desired class of instance.
   * @param <T> the type parameter for the instance.
   * @return the unique unnamed instance of this class in this scenario.
   * @throws UnsatisfiableDependencyException if the dependency cannot be satisfied.
   */
  public final <T> T satisfy(Class<T> klass) {
    return satisfy(TypeToken.of(klass));
  }

  public final <T> T satisfy(String name, Class<T> klass) {
    return satisfy(name, TypeToken.of(klass));
  }

  public final <T> T satisfy(TypeToken<T> type) {
    return satisfy(null, type);
  }

  public final <T> T satisfy(String name, TypeToken<T> type) {
    return satisfy(Key.of(Optional.ofNullable(name), type));
  }

  private <T> T satisfy(Key key) {
    keyTrace.push(key);
    if (!instanceBuilders.containsKey(key)) {
      throw new UnsatisfiableDependencyException(keyTrace);
    }
    try {
      T result = buildInstance(key);
      keyTrace.pop();
      return result;
    } catch (Exception e) {
      if (e instanceof UnsatisfiableDependencyException) {
        throw new UnsatisfiableDependencyException(keyTrace, e.getCause());
      } else {
        throw new UnsatisfiableDependencyException(keyTrace, e);
      }
    }
  }

  /**
   * The cast is safe for the following reason: by construction, the {@link #instanceBuilders} map
   * takes type tokens to instances of that same type.
   */
  @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
  private <T> T buildInstance(Key key) {
    InstanceBuilder<T> instanceBuilder = (InstanceBuilder<T>)instanceBuilders.get(key);
    return ((Optional<T>) Optional.ofNullable(cache.get(key)))
        .orElseGet(() -> {
          T instance = invokeInstanceBuilder(instanceBuilder);
          cache.put(key, instance);
          return instance;
        });
  }

  /**
   * Specify the resolution for a dependency inline. Allows you to replace a pattern like
   *
   * <pre>
   *   Scenario scenario = new MyScenarioBuilder()
   *       .given(payment().named("payment_1"))
   *       .given(user()).build()
   *       .build();
   *   ...
   *   private static Function<MyScenarioBuilder, InstanceBuilder<Payment>> payment() {
   *     return sb -> {
   *       User user = sb.satisfy(User.class);
   *       return Payment.for(user);
   *     };
   *   }
   * </pre>
   * with the inlined alternative:
   * <pre>
   *   Scenario scenario = new MyScenarioBuilder()
   *       .given(payment().named("payment_1"))
   *       .build();
   *   ...
   *   private static Function<MyScenarioBuilder, InstanceBuilder<Payment>> payment() {
   *     return sb -> Payment.for(sb.satisfyWith(user()));
   *   }
   * </pre>
   *
   * Under the hood, this method attaches the given instance builder to this scenario
   * builder. Equivalent to attaching the same instance
   * builder with {@link #given} and using one of the {@link #satisfy} methods to require the
   * dependency in a child instance builder.
   *
   * This method has low priority, and simply expresses a default. If an instance builder for the
   * desired key has already been registered elsewhere, it will not be overwritten by this method.
   * This is in contrast to the normal behavior, in which registering an instance builder with
   * {@link #given} will overwrite any previously registered builder for that key.
   *
   * @param detached an instance builder to attach to this scenario builder, which
   * when invoked will produce the desired instance.
   * @param <T> the type of the instance dependency to satisfy.
   * @return a constructed instance of type T.
   */
  public final <T> T satisfyWith(InstanceBuilder<T> detached) {
    Key key = detached.getKey();
    // Supplied instance builders should not overwrite previously registered builders.
    if (!instanceBuilders.containsKey(key)) {
      detached.attach(this);
      instanceBuilders.put(detached.getKey(), detached);
    }
    return satisfy(key);
  }

  public final <T> T satisfyWith(Function<ScenarioBuilder, T> attachable,
      TypeToken<T> type) {
    return satisfyWith(new InstanceBuilder<>(attachable, type));
  }

  public final <T> T satisfyWith(Function<ScenarioBuilder, T> attachable, Class<T> klass) {
    return satisfyWith(new InstanceBuilder<>(attachable, klass));
  }

  /**
   * This method mainly exists to be overridden. Because we don't allow {@link InstanceBuilder}
   * itself to have subclasses, in order to do something "extra", like persist certain entities in
   * a database, we need to be able to hook into the invocation chain at some point.
   *
   * @param instanceBuilder the instance builder to invoke
   * @param <T> the type of the instance produced
   * @return the produced instance
   *
   * @see com.squareup.testing.base.scenario.hibernate.HibernateScenarioBuilder
   */
  protected <T> T invokeInstanceBuilder(InstanceBuilder<T> instanceBuilder) {
    return instanceBuilder.produceInstance();
  }

  /**
   * Complete the current scenario builder and invoke all the registered instance builders.
   *
   * @return a finalized {@link Scenario}.
   */
  public final Scenario build() {
    keyTrace = new Stack<>();
    resolveDependencies();
    return buildFromCache();
  }

  /**
   * Subclasses are free to override this behavior in order to install extra hooks when
   * building the scenario.
   */
  protected Scenario buildFromCache() {
    return new BaseScenario(cache);
  }

  private void resolveDependencies() {
    while (cache.size() < instanceBuilders.size()) {
      Key key = instanceBuilders.entrySet().stream()
          .filter(entry -> !cache.containsKey(entry.getKey()))
          .findFirst().get().getKey();
      satisfy(key);
    }
  }
}
