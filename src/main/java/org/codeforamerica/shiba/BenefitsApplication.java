package org.codeforamerica.shiba;

import lombok.Setter;

import java.util.Optional;

@Setter
public class BenefitsApplication {
    private LanguagePreferences languagePreferences;
    private ProgramSelection programSelection;
    private PersonalInfo personalInfo;

    public Optional<LanguagePreferences> getLanguagePreferences() {
        return Optional.ofNullable(languagePreferences);
    }

    public Optional<ProgramSelection> getProgramSelection() {
        return Optional.ofNullable(programSelection);
    }

    public Optional<PersonalInfo> getPersonalInfo() {
        return Optional.ofNullable(personalInfo);
    }
}
