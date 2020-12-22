package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.pages.data.InputData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FullNameFormatter {
    public static String format(String unformattedName) {
        int lastIndex = unformattedName.trim().lastIndexOf(' ');
        return lastIndex == -1 ? "" : unformattedName.trim().substring(0, lastIndex);
    }

    @NotNull
    public static List<String> getListOfSelectedFullNames(Application application, String pageName, String inputName) {
        return Optional.ofNullable(application.getApplicationData().getPageData(pageName))
                .map(pageData -> pageData.get(inputName))
                .map(InputData::getValue)
                .orElse(List.of(""))
                .stream().map(FullNameFormatter::format).collect(Collectors.toList());
    }
}
