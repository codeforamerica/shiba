package org.codeforamerica.shiba;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utils {

    public static String joinNonEmpty(String... strings) {
        return Arrays.stream(strings)
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.joining(", "));
    }


    public static String getFileType(String filename) {
        String[] fileNameParts = filename.split("\\.");
        String extension = fileNameParts.length > 1 ? fileNameParts[fileNameParts.length - 1] : "";
        return extension;
    }
}
