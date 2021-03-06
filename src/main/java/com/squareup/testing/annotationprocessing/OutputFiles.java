package com.squareup.testing.annotationprocessing;

import java.lang.annotation.Target;
import javax.tools.StandardLocation;

/**
 * A set of "golden" output files that are loaded from resources. These are used to verify the
 * outputs of an annotation processor.
 *
 * <p>This class exists separate from {@link InputFiles} solely for convenience since the only
 * difference is the default value for {@link #location()}.
 *
 * @see ValidateGeneratedFiles
 */
@Target({})
public @interface OutputFiles {
  /** The names of the files/resources, relative to {@link #folder()}. */
  String[] value();

  /**
   * The name of a folder that contains the files to load. For a given file name (the
   * {@code i}<sup>th</sup> file in the array returned by {@link #value()}, for example), the following is
   * the path used when trying to load the file as a resource:
   * <pre> folder() + "/" + value()[i]</pre>
   */
  String folder() default "";

  /**
   * The location in the in-memory file system where the file will exist. The {@linkplain #folder()
   * folder name} is not used when determining file paths in the in-memory file system. So the
   * resulting path in the file system for a given file name (the {@code i}<sup>th</sup> file in
   * the array returned by {@link #value()}, for example) is as follows:
   * <pre> location().getName() + "/" + value()[i]</pre>
   */
  StandardLocation location() default StandardLocation.CLASS_OUTPUT;

  /**
   * True if the files are binary and bitwise comparison should be used, otherwise false for text.
   */
  boolean areBinary() default false;
}
