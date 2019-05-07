package com.squareup.testing.rules;

import com.google.common.base.Charsets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class TemporaryFolder extends org.junit.rules.TemporaryFolder {
  private final List<File> toDeleteAfter = new ArrayList<File>();

  @Override protected void after() {
    for (File file : toDeleteAfter) {
      recursiveDelete(file);
    }
    super.after();
  }

  public File deleteAfter(File file) {
    toDeleteAfter.add(file);
    return file;
  }

  @Override public File newFile(String fileName) throws IOException {
    File file = new File(getRoot(), fileName);
    if (!file.getParentFile().equals(getRoot())) {
      file.getParentFile().mkdirs();
    }
    file.createNewFile();
    return file;
  }

  public File newFile(String fileName, String contents) throws IOException {
    File file = newFile(fileName);
    FileUtils.writeStringToFile(file, contents, Charsets.UTF_8);
    return file;
  }

  @Override public File newFolder(String folderName) {
    File file = new File(getRoot(), folderName);
    file.mkdirs();
    return file;
  }

  protected void recursiveDelete(File file) {
    File[] files = file.listFiles();
    if (files != null) {
      for (File each : files)
        recursiveDelete(each);
    }
    file.delete();
  }
}
