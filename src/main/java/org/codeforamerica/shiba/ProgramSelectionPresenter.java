package org.codeforamerica.shiba;

import org.springframework.context.MessageSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProgramSelectionPresenter {
    private final ProgramSelection programSelection;

    private final Map<BenefitProgram, String> benefitProgramNameMap;

    public ProgramSelectionPresenter(ProgramSelection programSelection, MessageSource messageSource, Locale locale) {
        Set<ConstraintViolation<ProgramSelection>> constraintViolations = Validation.buildDefaultValidatorFactory().getValidator().validate(programSelection);
        if (!constraintViolations.isEmpty()) {
            throw new IllegalArgumentException();
        }

        benefitProgramNameMap = Map.of(
                BenefitProgram.EMERGENCY, messageSource.getMessage("how-it-works.emergency", new Object[]{}, locale),
                BenefitProgram.CHILD_CARE, messageSource.getMessage("how-it-works.child-care", new Object[]{}, locale),
                BenefitProgram.FOOD, messageSource.getMessage("how-it-works.food", new Object[]{}, locale),
                BenefitProgram.CASH, messageSource.getMessage("how-it-works.cash", new Object[]{}, locale)
        );

        this.programSelection = programSelection;
    }

    public String getTitleString() {
        Set<BenefitProgram> programs = programSelection.getPrograms();
        if (programs.size() == 1) {
            BenefitProgram benefitProgram = programs.iterator().next();
            return this.benefitProgramNameMap.get(benefitProgram);
        } else {
            Iterator<BenefitProgram> iterator = programs.iterator();
            StringBuilder stringBuilder = new StringBuilder(benefitProgramNameMap.get(iterator.next()));
            while (iterator.hasNext()) {
                BenefitProgram benefitProgram = iterator.next();
                if (iterator.hasNext()) {
                    stringBuilder.append(", ");
                } else {
                    stringBuilder.append(" and ");
                }
                stringBuilder.append(benefitProgramNameMap.get(benefitProgram));
            }
            return stringBuilder.toString();
        }
    }
}
