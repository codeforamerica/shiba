package org.codeforamerica.shiba;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguagePreferences {
    private Language spokenLanguage;
    private Language writtenLanguage;
    private Boolean needInterpreter;
}
