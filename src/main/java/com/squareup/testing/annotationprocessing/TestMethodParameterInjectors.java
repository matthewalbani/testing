package com.squareup.testing.annotationprocessing;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileManager;

/** Utility implementations of {@link TestMethodParameterInjector}. */
final class TestMethodParameterInjectors {
  private TestMethodParameterInjectors() {
  }

  /**
   * Creates an exception that indicates a problem with a parameter type of an injected method.
   *
   * @param m the method
   * @param argIndex the index of the argument with a bad type
   * @param supportedTypes the set of supported types
   * @return the exception with a detailed message of the offending argument
   */
  static IllegalArgumentException badArgumentType(Method m, int argIndex,
      Set<TypeToken<?>> supportedTypes) {
    return badArgumentTypeFromTypeStrings(m, argIndex, ImmutableSet.copyOf(
        Iterables.transform(supportedTypes, new Function<TypeToken<?>, String>() {
          @Override public String apply(TypeToken<?> input) {
            return input.toString();
          }
        })));
  }

  static IllegalArgumentException badArgumentTypeFromTypeStrings(Method m, int argIndex,
      Set<String> supportedTypes) {
    StringBuilder sb = new StringBuilder();
    sb.append("Argument #").append(argIndex + 1).append(" of method ").append(m.getName())
        .append("\nin class ").append(m.getDeclaringClass().getName())
        .append("\nhas unsupported type:")
        .append("\n  ").append(TypeToken.of(m.getGenericParameterTypes()[argIndex]))
        .append("\nSupported types include:");
    for (String supportedType : supportedTypes) {
      sb.append("\n  ").append(supportedType);
    }
    return new IllegalArgumentException(sb.toString());
  }

  // For test and after methods:

  /**
   * An interface for providing an injected value for a given {@link TestEnvironment}.
   *
   * @param <T> the type of value injected
   */
  private interface Provider<T> {
    T getValueFrom(TestEnvironment env);
  }

  private static final Map<TypeToken<?>, Provider<?>> providers = Maps.newLinkedHashMap();

  static {
    // update javadoc for AnnotationProcessorTestRunner whenever you add anything new here!!!
    addProvider(new TypeToken<TestEnvironment>() {},
        new Provider<TestEnvironment>() {
          @Override
          public TestEnvironment getValueFrom(TestEnvironment env) {
            return env;
          }
        }
    );
    addProvider(new TypeToken<TestJavaFileManager>() {},
        new Provider<TestJavaFileManager>() {
          @Override
          public TestJavaFileManager getValueFrom(TestEnvironment env) {
            return env.fileManager();
          }
        }
    );
    addProvider(new TypeToken<JavaFileManager>() {},
        new Provider<JavaFileManager>() {
          @Override
          public JavaFileManager getValueFrom(TestEnvironment env) {
            return env.fileManager();
          }
        }
    );
    addProvider(new TypeToken<CategorizingDiagnosticCollector>() {},
        new Provider<CategorizingDiagnosticCollector>() {
          @Override
          public CategorizingDiagnosticCollector getValueFrom(TestEnvironment env) {
            return env.diagnosticCollector();
          }
        }
    );
    addProvider(new TypeToken<ProcessingEnvironment>() {},
        new Provider<ProcessingEnvironment>() {
          @Override
          public ProcessingEnvironment getValueFrom(TestEnvironment env) {
            return env.processingEnvironment();
          }
        }
    );
    addProvider(new TypeToken<Elements>() {},
        new Provider<Elements>() {
          @Override
          public Elements getValueFrom(TestEnvironment env) {
            return env.processingEnvironment().getElementUtils();
          }
        }
    );
    addProvider(new TypeToken<Types>() {},
        new Provider<Types>() {
          @Override
          public Types getValueFrom(TestEnvironment env) {
            return env.processingEnvironment().getTypeUtils();
          }
        }
    );
    addProvider(new TypeToken<Filer>() {},
        new Provider<Filer>() {
          @Override
          public Filer getValueFrom(TestEnvironment env) {
            return env.processingEnvironment().getFiler();
          }
        }
    );
    addProvider(new TypeToken<Messager>() {},
        new Provider<Messager>() {
          @Override
          public Messager getValueFrom(TestEnvironment env) {
            return env.processingEnvironment().getMessager();
          }
        }
    );
    addProvider(new TypeToken<SourceVersion>() {},
        new Provider<SourceVersion>() {
          @Override
          public SourceVersion getValueFrom(TestEnvironment env) {
            return env.processingEnvironment().getSourceVersion();
          }
        }
    );
    addProvider(new TypeToken<Map<String, String>>() {},
        new Provider<Map<String, String>>() {
          @Override
          public Map<String, String> getValueFrom(TestEnvironment env) {
            return env.processingEnvironment().getOptions();
          }
        }
    );
    addProvider(new TypeToken<RoundEnvironment>() {},
        new Provider<RoundEnvironment>() {
          @Override
          public RoundEnvironment getValueFrom(TestEnvironment env) {
            return env.roundEnvironment();
          }
        }
    );
    addProvider(new TypeToken<Set<TypeElement>>() {},
        new Provider<Set<TypeElement>>() {
          @Override
          public Set<TypeElement> getValueFrom(TestEnvironment env) {
            return ImmutableSet.copyOf(env.annotationTypes());
          }
        }
    );
    // update javadoc for AnnotationProcessorTestRunner whenever you add anything new here!!!
  }

  private static <T> void addProvider(TypeToken<T> typeToken, Provider<T> provider) {
    providers.put(typeToken, provider);
  }

  /**
   * Injects values for "test" and "after" methods. Injected objects come from a
   * {@link TestEnvironment}.
   */
  static TestMethodParameterInjector<TestEnvironment> FOR_TEST_METHODS =
      new TestMethodParameterInjector<TestEnvironment>() {
        private final Set<String> supportedTypes =
            ImmutableSet.<String>builder()
                .addAll(
                    Iterables.transform(providers.keySet(), new Function<TypeToken<?>, String>() {
                      @Override public String apply(TypeToken<?> input) {
                        return input.toString();
                      }
                    }))
                .add("? extends " + Processor.class.getName())
                .build();

        @Override
        public Object[] getInjectedParameters(Method m, TestEnvironment testEnv) {
          Class<?> argClasses[] = m.getParameterTypes();
          Type argTypes[] = m.getGenericParameterTypes();
          Object ret[] = new Object[argClasses.length];
          for (int i = 0, len = ret.length; i < len; i++) {
            Type t = argTypes[i];
            Provider<?> provider = providers.get(TypeToken.of(t));
            if (provider != null) {
              ret[i] = provider.getValueFrom(testEnv);
            } else if (!Processor.class.isAssignableFrom(argClasses[i])) {
              throw badArgumentTypeFromTypeStrings(m, i, supportedTypes);
            } else {
              Processor p = testEnv.processorUnderTest();
              if (p != null && !argClasses[i].isInstance(p)) {
                throw new ClassCastException("Argument #" + (i + 1) + " of method " + m.getName()
                    + "\nin class " + m.getDeclaringClass().getName()
                    + "\nexpecting type " + argClasses[i].getName()
                    + "\nbut got " + p.getClass().getName());
              }
              ret[i] = p;
            }
          }
          return ret;
        }

        @Override
        public void validateParameterTypes(Method m, List<Throwable> errors) {
          Class<?> argClasses[] = m.getParameterTypes();
          Type argTypes[] = m.getGenericParameterTypes();
          for (int i = 0, len = argClasses.length; i < len; i++) {
            Type t = argTypes[i];
            if (!providers.containsKey(TypeToken.of(t))) {
              if (!Processor.class.isAssignableFrom(argClasses[i])) {
                errors.add(badArgumentTypeFromTypeStrings(m, i, supportedTypes));
              }
            }
          }
        }
      };

  // For before methods:

  private static Set<Class<?>> allowedBeforeClasses =
      ImmutableSet.<Class<?>>of(TestJavaFileManager.class, JavaFileManager.class);

  private static Set<TypeToken<?>> allowedBeforeTypes =
      ImmutableSet.copyOf(
          Iterables.transform(allowedBeforeClasses, new Function<Class<?>, TypeToken<?>>() {
            @Override public TypeToken<?> apply(Class<?> aClass) {
              return TypeToken.of(aClass);
            }
          }));

  /**
   * Injects values for "before" methods. Supported injected objects include only a
   * {@link TestJavaFileManager}.
   */
  static TestMethodParameterInjector<TestJavaFileManager> FOR_BEFORE_METHODS =
      new TestMethodParameterInjector<TestJavaFileManager>() {
        @Override
        public Object[] getInjectedParameters(Method m, TestJavaFileManager fileManager) {
          Class<?> argClasses[] = m.getParameterTypes();
          Object ret[] = new Object[argClasses.length];
          for (int i = 0, len = ret.length; i < len; i++) {
            if (allowedBeforeClasses.contains(argClasses[i])) {
              ret[i] = fileManager;
            } else {
              throw badArgumentType(m, i, allowedBeforeTypes);
            }
          }
          return ret;
        }

        @Override
        public void validateParameterTypes(Method m, List<Throwable> errors) {
          Class<?> argClasses[] = m.getParameterTypes();
          for (int i = 0, len = argClasses.length; i < len; i++) {
            if (!allowedBeforeClasses.contains(argClasses[i])) {
              errors.add(badArgumentType(m, i, allowedBeforeTypes));
            }
          }
        }
      };
}
