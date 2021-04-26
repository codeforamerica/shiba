package org.codeforamerica.shiba;

import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Utils {
    public static String joinNonEmpty(String... strings) {
        return Arrays.stream(strings)
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.joining(", "));
    }


    public static String getFileType(String filename) {
        String[] fileNameParts = filename.split("\\.");
        return fileNameParts.length > 1 ? fileNameParts[fileNameParts.length - 1] : "";
    }

    public static void writeByteArrayToFile(byte[] fileBytes, String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(fileBytes);
        } catch (IOException e) {
            log.error("Failed to close FileOutputStream");
        }
    }
}
