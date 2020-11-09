package org.codeforamerica.shiba.output;

public class FullNameFormatter {
    public static String format(String unformattedName) {
        int lastIndex = unformattedName.trim().lastIndexOf(' ');
        return lastIndex == -1 ? "" : unformattedName.trim().substring(0, lastIndex);
    }
}
