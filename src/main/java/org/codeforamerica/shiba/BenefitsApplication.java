package org.codeforamerica.shiba;

import lombok.Setter;

import java.util.Optional;

@Setter
public class BenefitsApplication {
    private LanguagePreferences languagePreferences;

    public Optional<LanguagePreferences> getLanguagePreferences() {
        return Optional.ofNullable(languagePreferences);
    }
}
