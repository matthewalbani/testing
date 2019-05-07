package com.squareup.testing.annotationprocessing;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Utility for injecting arguments into test methods. This allows tests to define various incoming
 * parameters to receive information about the processing environment. The methods on this
 * interface are used to inject values for the parameters at runtime.
 *
 * <p>Guice is not used here because it is insufficient for two reasons:
 * <ol>
 *   <li>This interface is used to perform "lazy" injection. We want to know the set of bound keys
 *   before we can actually bind those keys to instances. This is because we need to know the types
 *   we can inject to validate test methods are defined properly, but we don't actually instantiate
 *   the objects (and really don't want to) until a test method is actually being executed. This can
 *   be done with Guice, but would be a little ugly and require a bit of code (lots of
 *   {@code @Provides} methods and a test-method scope), so this purpose-built interface is clearer
 *   and no less concise.</li>
 *   <li>One of the sneaky things this interface supports (for test method injection) is for the
 *   test method to inject <em>any</em> type that implements {@link
 *   javax.annotation.processing.Processor}. We don't want to instantiate the processor prior to
 *   validation, so we allow any type and then throw a {@link ClassCastException} at runtime if
 *   the specified processor isn't assignable to the injected type.</li>
 * </ol>
 *
 * @param <C> a type that represents context for injection and supplies the injected values
 *
 * @see TestEnvironment
 * @see TestMethodProcessor
 * @see AnnotationProcessorTestRunner
 */
interface TestMethodParameterInjector<C> {

   /**
    * Gets the parameter values to use for invoking the specified method.
    *
    * @param m the method whose parameter values are returned
    * @param context the current context which contains values that will be injected
    * @return parameter values, injected from the context
    * @throws IllegalArgumentException if the specified method contains an argument whose
    *       type cannot be injected
    * @throws ClassCastException if the specified method accepts an argument whose type
    *       could be injected but isn't actually cast-able from the runtime type of
    *       the object in context
    */
   Object[] getInjectedParameters(Method m, C context);

   /**
    * Validates that the specified method has injectable parameter types. If it
    * has invalid types, {@link IllegalArgumentException}s will be added to the
    * specified list of errors that contain messages describing the invalid
    * argument.
    *
    * @param m the method to verify
    * @param errors the list of errors to which to add validation errors
    */
   void validateParameterTypes(Method m, List<Throwable> errors);

}
