package com.squareup.testing.annotationprocessing;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Utilities for fully consuming byte and character streams and returning the contents as byte
 * arrays or strings.
 */
final class ReadFully {
  private ReadFully() {
  }

  // a bit surprising that Guava's CharStreams and ByteStreams don't have analogs to these...

  public static String from(Readable in) throws IOException {
    StringWriter out = new StringWriter();
    CharStreams.copy(in, out);
    return out.toString();
  }

  public static String from(InputStream in, Charset charset) throws IOException {
    return new String(from(in), charset);
  }

  public static byte[] from(Readable in, Charset charset) throws IOException {
    return from(in).getBytes(charset);
  }

  public static byte[] from(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteStreams.copy(in, out);
    return out.toByteArray();
  }
}
