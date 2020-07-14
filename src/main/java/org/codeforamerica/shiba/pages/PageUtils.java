package org.codeforamerica.shiba.pages;

import java.util.Iterator;
import java.util.List;

public class PageUtils {
    private static final String WEB_INPUT_ARRAY_TOKEN = "[]";

    public static String getFormInputName(String name) {
        return name + WEB_INPUT_ARRAY_TOKEN;
    }

    public static String getTitleString(List<String> strings) {
        if (strings.size() == 1) {
            return strings.iterator().next();
        } else {
            Iterator<String> iterator = strings.iterator();
            StringBuilder stringBuilder = new StringBuilder(iterator.next());
            while (iterator.hasNext()) {
                String string = iterator.next();
                if (iterator.hasNext()) {
                    stringBuilder.append(", ");
                } else {
                    stringBuilder.append(" and ");
                }
                stringBuilder.append(string);
            }
            return stringBuilder.toString();
        }
    }

    public static String formatPhone(String phoneDigits) {
        if (phoneDigits.length() != 10) {
            throw new IllegalArgumentException("Phone must contain exactly 10 digits in order to format!");
        }
        return String.format("(%s) %s-%s", phoneDigits.substring(0,3), phoneDigits.substring(3,6), phoneDigits.substring(6,10));
    }
}
