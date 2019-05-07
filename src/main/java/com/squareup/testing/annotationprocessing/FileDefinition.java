package com.squareup.testing.annotationprocessing;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

/**
 * A definition of a test file. A test file exists in the test file system, and also has an analog
 * in the test class's resources. The contents of the resource may be used to either seed the test
 * file system contents (for setting up input files) or to verify the test file system contents (for
 * checking generated output files).
 */
class FileDefinition {

  /**
   * The folder in which the resource belongs.
   *
   * @see InputFiles#folder()
   * @see OutputFiles#folder()
   */
  private final String sourceFolder;

  /**
   * The name of the file. This name is relative to the {@linkplain #sourceFolder folder} when
   * finding the associated resource. In the test file system this name will be relative to the
   * {@linkplain #targetLocation target location}.
   *
   * @see InputFiles#value()
   * @see OutputFiles#value()
   */
  private final String fileName;

  /**
   * The location in the test file system where the file resides.
   *
   * @see InputFiles#location()
   * @see OutputFiles#location()
   */
  private final Location targetLocation;

  /**
   * True if the file is binary and should be compared using bitwise-comparison. False for text.
   *
   * @see OutputFiles#areBinary()
   */
  private final boolean isBinary;

  /**
   * Creates a new file definition.
   *
   * @param sourceFolder the folder in which the resource exists
   * @param fileName the name of the file
   * @param targetLocation the location in the test file system where the file resides
   * @param isBinary true if the file is binary
   */
  FileDefinition(String sourceFolder, String fileName, Location targetLocation, boolean isBinary) {
    this.sourceFolder = sourceFolder;
    this.fileName = fileName;
    this.targetLocation = targetLocation;
    this.isBinary = isBinary;
  }

  /**
   * The name of the file. The name can contain more than just a simple name and is allowed to
   * contain more than one path element.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * The full path to the resource that represents this file. This path can be used in conjunction
   * with {@link Class#getResourceAsStream(String)} to load resource contents.
   *
   * @return the full path to the resource
   */
  public String getResourcePath() {
    return TestJavaFileManager.removeMultipleSlashes(sourceFolder + "/" + fileName);
  }

  /**
   * The location of the file in the test file system.
   *
   * @return the location
   */
  public Location getTargetLocation() {
    return targetLocation;
  }

  /**
   * The full path to the file in the test file system.
   *
   * @return the full path to the test file
   */
  public String getTargetPath() {
    return TestJavaFileManager.removeMultipleSlashes(targetLocation.getName() + "/" + fileName);
  }

  /**
   * @return true if the file is binary, otherwise false for text.
   */
  public boolean isBinary() { return isBinary; }

  /**
   * A view of sets of files. This has the same structure as the two annotations used for defining
   * file sets in tests.
   *
   * @see FilesToProcess
   * @see ValidateGeneratedFiles
   */
  private interface FileSets {
    List<FileSet> files();
    boolean incremental();
  }

  /**
   * A view of a single set of files. This has the same structure as the two annotations used for
   * describing files in tests.
   *
   * @see InputFiles
   * @see OutputFiles
   */
  private interface FileSet {
    List<String> relativePaths();
    String folder();
    StandardLocation location();
    boolean areBinary();
  }

  /**
   * Gets a list of all {@link FileDefinition}s that represent input files to process for the
   * specified test. The returned list may include duplicate test files in the event that
   * annotations on both the test method and the test class refer to the same test file. In this
   * case, the specification from the method is first and the one from the class is later in the
   * list.
   *
   * @param testMethod the test method
   * @param testClass the test class
   * @return the list of all {@link FileDefinition}s
   * @see FilesToProcess
   */
  public static List<FileDefinition> getFilesToProcess(Method testMethod, Class<?> testClass) {
    return getFilesFromAnnotations(testMethod, testClass,
        annotationToFileSets(testMethod.getAnnotation(FilesToProcess.class)),
        annotationToFileSets(testClass.getAnnotation(FilesToProcess.class)));
  }

  private static FileSets annotationToFileSets(final FilesToProcess filesToProcess) {
    if (filesToProcess == null) {
      return null;
    }
    // convert annotation to this common interface
    return new FileSets() {
      @Override public List<FileSet> files() {
        return ImmutableList.copyOf(Iterables.transform(Arrays.asList(
            filesToProcess.value()), new Function<InputFiles, FileSet>() {
          @Override public FileSet apply(final InputFiles inputFiles) {
            // convert annotation to interface
            return new FileSet() {
              @Override public List<String> relativePaths() {
                return ImmutableList.copyOf(inputFiles.value());
              }

              @Override public String folder() {
                return inputFiles.folder();
              }

              @Override public StandardLocation location() {
                return inputFiles.location();
              }

              @Override public boolean areBinary() {
                // We don't know or use this. Can't throw an UnsupportedOperationException,
                // because this gets called from other generic code.
                return false;
              }
            };
          }
        }));
      }

      @Override public boolean incremental() {
        return filesToProcess.incremental();
      }
    };
  }

  /**
   * Gets a list of all {@link FileDefinition}s that represent output files to validate for the
   * specified test. The returned list may include duplicate test files in the event that
   * annotations on both the test method and the test class refer to the same test file. In this
   * case, the specification from the method is first and the one from the class is later in the
   * list.
   *
   * @param testMethod the test method
   * @param testClass the test class
   * @return the list of all {@link FileDefinition}s
   * @see ValidateGeneratedFiles
   */
  public static List<FileDefinition> getFilesToValidate(Method testMethod, Class<?> testClass) {
    return getFilesFromAnnotations(testMethod, testClass,
        annotationToFileSets(testMethod.getAnnotation(ValidateGeneratedFiles.class)),
        annotationToFileSets(testClass.getAnnotation(ValidateGeneratedFiles.class)));
  }

  private static FileSets annotationToFileSets(final ValidateGeneratedFiles filesToValidate) {
    if (filesToValidate == null) {
      return null;
    }
    // convert annotation to this common interface
    return new FileSets() {
      @Override public List<FileSet> files() {
        return ImmutableList.copyOf(Iterables.transform(Arrays.asList(
            filesToValidate.value()), new Function<OutputFiles, FileSet>() {
          @Override public FileSet apply(final OutputFiles outputFiles) {
            // convert annotation to interface
            return new FileSet() {
              @Override public List<String> relativePaths() {
                return ImmutableList.copyOf(outputFiles.value());
              }

              @Override public String folder() {
                return outputFiles.folder();
              }

              @Override public StandardLocation location() {
                return outputFiles.location();
              }

              @Override public boolean areBinary() {
                return outputFiles.areBinary();
              }
            };
          }
        }));
      }

      @Override public boolean incremental() {
        return filesToValidate.incremental();
      }
    };
  }

  /**
   * Extracts a list of files from annotations.
   *
   * @param testMethod the test method
   * @param testClass the test class
   * @param forMethod sets of files defined in a method annotation
   * @param forClass sets of files defined in a class annotation
   * @return the list of files
   */
  private static List<FileDefinition> getFilesFromAnnotations(Method testMethod, Class<?> testClass,
      FileSets forMethod, FileSets forClass) {
    ImmutableList.Builder<FileDefinition> files = ImmutableList.builder();
    boolean includeClass = true;
    if (forMethod != null) {
      if (!forMethod.incremental()) {
        includeClass = false;
      }
      addFilesToProcess(forMethod, files);
    }
    if (includeClass && forClass != null) {
      addFilesToProcess(forClass, files);
    }
    return files.build();
  }

  /**
   * Appends all files in the specified {@link FileSets} to the specified list.
   *
   * @param fileSets the sets of files to add
   * @param files the list builder to which files are added
   */
  private static void addFilesToProcess(FileSets fileSets,
      ImmutableList.Builder<FileDefinition> files) {
    for (FileSet inputFiles : fileSets.files()) {
      for (String file : inputFiles.relativePaths()) {
        files.add(new FileDefinition(inputFiles.folder(), file, inputFiles.location(),
            inputFiles.areBinary()));
      }
    }
  }
}
