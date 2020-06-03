package org.codeforamerica.shiba;

import lombok.Setter;

import java.util.Optional;

@Setter
public class BenefitsApplication {
    private LanguagePreferences languagePreferences;
    private ProgramSelection programSelection;

    public LanguagePreferences getLanguagePreferences() {
        return Optional.ofNullable(languagePreferences).orElse(new LanguagePreferences());
    }

    public ProgramSelection getProgramSelection() {
        return Optional.ofNullable(programSelection).orElse(new ProgramSelection());
    }
}
