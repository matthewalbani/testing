package com.squareup.testing.annotationprocessing;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Information about the current test, processing, and round environments. This represents the
 * context of a test run by an {@link AnnotationProcessorTestRunner}. It includes references to the
 * current {@link ProcessingEnvironment}, {@link RoundEnvironment}, {@link JavaFileManager}, etc. An
 * instance of this class can be injected into a test method so that the test logic can interact
 * with the environment.
 */
public class TestEnvironment {
  private final TestJavaFileManager fileManager;
  private final CategorizingDiagnosticCollector diagnosticCollector;
  private final ProcessingEnvironment processingEnv;
  private final RoundEnvironment roundEnv;
  private final int roundNumber;
  private final Set<? extends TypeElement> annotationTypes;
  private final Processor processorUnderTest;
  private final Object testObject;

  TestEnvironment(TestJavaFileManager fileManager,
      CategorizingDiagnosticCollector diagnosticCollector,
      ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, int roundNumber,
      Set<? extends TypeElement> annotationTypes, Processor processorUnderTest, Object testObject) {
    this.fileManager = fileManager;
    this.diagnosticCollector = diagnosticCollector;
    this.processingEnv = processingEnv;
    this.roundEnv = roundEnv;
    this.roundNumber = roundNumber;
    this.annotationTypes = annotationTypes;
    this.processorUnderTest = processorUnderTest;
    this.testObject = testObject;
  }

  /**
   * Gets the current {@link JavaFileManager}.
   *
   * @return the file manager
   */
  public TestJavaFileManager fileManager() {
    return fileManager;
  }

  /**
   * Gets the current collector of diagnostics emitted by the compiler and by annotation
   * processors.
   *
   * @return the diagnostic collector
   */
  public CategorizingDiagnosticCollector diagnosticCollector() {
    return diagnosticCollector;
  }

  /**
   * Gets the current processing environment.
   *
   * @return the processing environment
   */
  public ProcessingEnvironment processingEnvironment() {
    return processingEnv;
  }

  /**
   * Gets the round environment for the current round of processing.
   *
   * @return the round environment
   */
  public RoundEnvironment roundEnvironment() {
    return roundEnv;
  }

  /**
   * Gets the number of the current round (first round is number one).
   *
   * @return the current round number
   */
  public int roundNumber() {
    return roundNumber;
  }

  /**
   * Gets the set of annotation types for the current round, as {@link TypeElement}s. If the test
   * defined a processor (using {@link ProcessorUnderTest @ProcessorUnderTest} or {@link
   * InitializeProcessorField @InitializeProcessorField} annotations), then this will be filtered
   * to just the annotations supported by that processor.
   *
   * <p>If no processor is defined, this will be all annotations in the current set of classes and
   * input files to process. If a processor is later created programmatically inside of a test
   * method, this set can then be filtered to only the supported annotation types using {@link
   * #filterAnnotationTypesFor(Processor)}.
   *
   * @return the set of annotation types for the current round of processing
   * @see #processorUnderTest()
   * @see #filterAnnotationTypesFor(Processor)
   */
  public Set<? extends TypeElement> annotationTypes() {
    return annotationTypes;
  }

  /**
   * Filters the current set of annotation types to only those supported by the specified
   * processor.
   *
   * @param processor the processor
   * @return the filtered set of annotation types for the current round of processing
   * @see #annotationTypes()
   */
  public Set<? extends TypeElement> filterAnnotationTypesFor(Processor processor) {
    Set<String> supported = processor.getSupportedAnnotationTypes();
    ImmutableSet.Builder<TypeElement> filteredTypes = ImmutableSet.builder();
    for (TypeElement element : annotationTypes()) {
      if (supported.contains(element.getQualifiedName().toString())) {
        filteredTypes.add(element);
      }
    }
    return filteredTypes.build();
  }

  /**
   * Gets the current processor under test or {@code null} if there isn't one.
   *
   * @return the processor under test
   */
  public Processor processorUnderTest() {
    return processorUnderTest;
  }

  /**
   * Invokes the current processor under test by calling its {@link Processor#process(Set,
   * RoundEnvironment)} method. The current round environment and annotation types are passed to
   * this method.
   *
   * @return the result returned by the processor
   * @throws NullPointerException if there is no current processor under test
   * @see #processorUnderTest()
   */
  public boolean invokeProcessor() {
    return processorUnderTest.process(annotationTypes, roundEnv);
  }

  /**
   * Validates the contents of the specified file by comparing them to the contents of the
   * specified resource.
   *
   * @param file the output file to validate
   * @param resourcePath the resource that contains the "golden" contents
   * @param binary if true then the raw byte contents of the specified file and resource are
   * compared; otherwise the files are interpreted as character data and the result strings are
   * compared
   * @throws IOException if an exception occurs while reading the specified file or the specified
   * resource
   * @see ValidateGeneratedFiles
   */
  public void validateGeneratedFile(FileObject file, String resourcePath, boolean binary)
      throws IOException {
    InputStream in = testObject.getClass().getResourceAsStream(resourcePath);
    if (in == null) {
      throw new IllegalArgumentException("Resource not found: " + resourcePath);
    }
    try {
      if (binary) {
        byte goldenBytes[] = ReadFully.from(in);
        byte genBytes[];

        if (file instanceof TestJavaFileObject) {
          genBytes = ((TestJavaFileObject) file).getByteContents();
        } else {
          InputStream genIn = file.openInputStream();
          try {
            genBytes = ReadFully.from(genIn);
          } finally {
            genIn.close();
          }
        }
        assertArrayEquals(
            "Output " + file.getName() + " does not match contents of resource " + resourcePath,
            goldenBytes,
            genBytes);
      } else {
        String expected = ReadFully.from(in, Charsets.UTF_8);
        CharSequence actual = file.getCharContent(true);
        if (!expected.equals(actual.toString())) {
          throw new RuntimeException(
              "Output " + file.getName() + " does not match contents of resource " + resourcePath
                  + "\nExpected:\n" + expected + "\nActual:\n" + actual);
        }
      }
    } finally {
      in.close();
    }
  }
}
