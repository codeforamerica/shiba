package org.codeforamerica.shiba;

import org.springframework.context.MessageSource;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgramSelectionPresenter {
    private final List<String> programSelection;
    private final Map<String, String> programNameMap;

    public ProgramSelectionPresenter(MessageSource messageSource, Locale locale, List<String> programSelection) {
        if (programSelection.isEmpty()) {
            throw new IllegalArgumentException("Program selection must be non-empty!");
        }
        this.programSelection = programSelection;
        this.programNameMap = Map.of(
                "EMERGENCY", messageSource.getMessage("how-it-works.emergency", new Object[]{}, locale),
                "CHILD_CARE", messageSource.getMessage("how-it-works.child-care", new Object[]{}, locale),
                "FOOD", messageSource.getMessage("how-it-works.food", new Object[]{}, locale),
                "CASH", messageSource.getMessage("how-it-works.cash", new Object[]{}, locale)
        );
    }

    public String getTitleString() {
        if (programSelection.size() == 1) {
            String benefitProgram = programSelection.iterator().next();
            return this.programNameMap.get(benefitProgram);
        } else {
            Iterator<String> iterator = programSelection.iterator();
            StringBuilder stringBuilder = new StringBuilder(programNameMap.get(iterator.next()));
            while (iterator.hasNext()) {
                String benefitProgram = iterator.next();
                if (iterator.hasNext()) {
                    stringBuilder.append(", ");
                } else {
                    stringBuilder.append(" and ");
                }
                stringBuilder.append(programNameMap.get(benefitProgram));
            }
            return stringBuilder.toString();
        }
    }
}
