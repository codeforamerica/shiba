package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguagePreferences {
    private String spokenLanguage;
    private String writtenLanguage;
    private Boolean needInterpreter;
}
