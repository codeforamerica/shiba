package org.codeforamerica.shiba;

public class InputUtils {
    private static final String WEB_INPUT_ARRAY_TOKEN = "[]";

    public static String getFormInputName(String name) {
        return name + WEB_INPUT_ARRAY_TOKEN;
    }
}
