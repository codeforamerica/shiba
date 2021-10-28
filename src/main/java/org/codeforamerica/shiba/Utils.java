package org.codeforamerica.shiba;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

  private Utils() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  public static String joinNonEmpty(String... strings) {
    return Arrays.stream(strings)
        .filter(Predicate.not(String::isEmpty))
        .collect(Collectors.joining(", "));
  }


  public static String getFileType(String filename) {
    String[] fileNameParts = filename.toLowerCase().split("\\.");
    return fileNameParts.length > 1 ? fileNameParts[fileNameParts.length - 1] : "";
  }

  public static void writeByteArrayToFile(byte[] fileBytes, String filename) {
    try (FileOutputStream fos = new FileOutputStream(filename)) {
      fos.write(fileBytes);
    } catch (IOException e) {
      log.error("Error while attempting to write byte array to file: " + filename, e);
    }
  }
}
