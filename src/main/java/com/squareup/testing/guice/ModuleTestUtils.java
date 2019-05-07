package com.squareup.testing.guice;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.Message;
import com.google.inject.spi.PrivateElements;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Useful methods for exploring the bindings defined in a Guice module, for writing unit tests for
 * modules.
 *
 * <p>Binding checks can also be done using an Injector. But when unit testing a module it may be
 * inconvenient to also satisfy all the dependencies of the module under test. Without the entire
 * set of dependencies, an injector cannot be created.
 *
 * <p>So these utility methods let you make assertions about the module's bindings without requiring
 * its full set of up-stream dependencies to be configured.
 */
public final class ModuleTestUtils {

  private ModuleTestUtils() {
  }

  /**
   * Gets all error messages from configuring the specified modules. This does not perform full
   * validation, as would be done when creating an injector. Instead, it's just looking for errors
   * that modules add explicitly using {@link AbstractModule#addError}.
   *
   * @param modules the modules to inspect
   * @return the list of errors added by the specified modules
   */
  public static List<Message> getErrorMessages(Module... modules) {
    return getErrorMessages(Elements.getElements(modules));
  }

  private static List<Message> getErrorMessages(List<Element> elements) {
    final List<Message> errorMessages = Lists.newArrayList();
    for (Element element : elements) {
      element.acceptVisitor(new DefaultElementVisitor<Void>() {
        @Override public Void visit(Message message) {
          errorMessages.add(message);
          return null;
        }

        @Override public Void visit(PrivateElements privateElements) {
          errorMessages.addAll(getErrorMessages(privateElements.getElements()));
          return null;
        }
      });
    }
    return errorMessages;
  }

  /**
   * Extracts all bindings from the specified modules. You can query the bindings via their bind
   * {@link Key}.
   *
   * @param modules the modules to inspect
   * @return all bindings created by the specified modules
   */
  public static Map<Key<?>, Binding<?>> getBindingsByKey(Module... modules) {
    return getBindingsByKey(Elements.getElements(modules));
  }

  private static Map<Key<?>, Binding<?>> getBindingsByKey(List<Element> elements) {
    final Map<Key<?>, Binding<?>> boundKeys = Maps.newHashMap();
    for (Element element : elements) {
      element.acceptVisitor(new DefaultElementVisitor<Void>() {
        @Override public <T> Void visit(Binding<T> binding) {
          boundKeys.put(binding.getKey(), binding);
          return null;
        }

        @Override public Void visit(PrivateElements privateElements) {
          Map<Key<?>, Binding<?>> boundPrivateKeys =
              getBindingsByKey(privateElements.getElements());
          // also include all keys exposed by enclosed private environments
          for (Key<?> exposedKey : privateElements.getExposedKeys()) {
            boundKeys.put(exposedKey, checkNotNull(boundPrivateKeys.get(exposedKey)));
          }
          return null;
        }
      });
    }
    return boundKeys;
  }

  /**
   * Finds bind keys of a given type. This simply filters the list of bind keys, finding ones that
   * are for the given raw type.
   *
   * @param keys the keys to search
   * @param type the type whose keys are to be returned
   * @return the set of keys with the given raw type
   */
  public static Set<Key<?>> filterKeysByRawType(Iterable<Key<?>> keys, final Class<?> type) {
    return ImmutableSet.copyOf(Iterables.filter(keys, new Predicate<Key<?>>() {
      @Override public boolean apply(Key<?> input) {
        return input.getTypeLiteral().getRawType().equals(type);
      }
    }));
  }

  /**
   * Finds bind keys of a given type. This simply filters the list of bind keys, finding ones that
   * are for the given generic type.
   *
   * @param keys the keys to search
   * @param type the type whose keys are to be returned
   * @return the set of keys with the given raw type
   */
  public static Set<Key<?>> filterKeysByType(Iterable<Key<?>> keys, final TypeLiteral<?> type) {
    return ImmutableSet.copyOf(Iterables.filter(keys, new Predicate<Key<?>>() {
      @Override public boolean apply(Key<?> input) {
        return input.getTypeLiteral().equals(type);
      }
    }));
  }

  /**
   * Asserts that the given binding is bound to the expected key.
   *
   * @param binding the binding in question
   * @param expected the expected key to which it is bound
   * @throws AssertionError if the binding is bound to something different
   */
  public static void expectBinding(Binding<?> binding, final Key<?> expected) {
    binding.acceptTargetVisitor(new DefaultBindingTargetVisitor<Object, Void>() {
      @Override public Void visit(LinkedKeyBinding<?> binding) {
        assertEquals(expected, binding.getLinkedKey());
        return null;
      }

      @Override public Void visitOther(Binding<?> binding) {
        fail("expected a LinkedKeyBinding for " + binding.getKey());
        return null; // make javac happy
      }
    });
  }
}
