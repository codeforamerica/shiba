package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.codeforamerica.shiba.BenefitProgram.*;

class ProgramSelectionPresenterTest {
    private final StaticMessageSource messageSource = new StaticMessageSource();
    private final Locale defaultLocale = Locale.ENGLISH;

    @BeforeEach
    void setup() {
        messageSource.addMessage("how-it-works.emergency", defaultLocale, "emergency");
        messageSource.addMessage("how-it-works.child-care", defaultLocale, "child care");
        messageSource.addMessage("how-it-works.food", defaultLocale, "food");
        messageSource.addMessage("how-it-works.cash", defaultLocale, "cash");
    }

    @Test
    void shouldNotAllowEmptyProgramSelection() {
        ProgramSelection programSelection = new ProgramSelection();
        programSelection.setPrograms(new TreeSet<>());

        assertThatThrownBy(() -> new ProgramSelectionPresenter(programSelection, messageSource, defaultLocale))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldConstructPageTitle_withASingleProgram() {
        ProgramSelection programSelection = new ProgramSelection();
        SortedSet<BenefitProgram> programs = new TreeSet<>(List.of(EMERGENCY));
        programSelection.setPrograms(programs);
        ProgramSelectionPresenter subject = new ProgramSelectionPresenter(programSelection, messageSource, defaultLocale);

        assertThat(subject.getTitleString()).isEqualTo("emergency");
    }

    @Test
    void shouldConstructPageTitle_withMultiplePrograms_inTheGivenLocale() {
        ProgramSelection programSelection = new ProgramSelection();
        SortedSet<BenefitProgram> programs = new TreeSet<>(List.of(EMERGENCY, CHILD_CARE, FOOD, CASH));
        programSelection.setPrograms(programs);
        messageSource.addMessage("how-it-works.emergency", Locale.FRENCH, "emergency in french");
        messageSource.addMessage("how-it-works.child-care", Locale.FRENCH, "child care in french");
        messageSource.addMessage("how-it-works.food", Locale.FRENCH, "food in french");
        messageSource.addMessage("how-it-works.cash", Locale.FRENCH, "cash in french");
        ProgramSelectionPresenter subject = new ProgramSelectionPresenter(programSelection, messageSource, Locale.FRENCH);

        assertThat(subject.getTitleString()).containsOnlyOnce("emergency in french");
        assertThat(subject.getTitleString()).containsOnlyOnce("child care in french");
        assertThat(subject.getTitleString()).containsOnlyOnce("cash in french");
        assertThat(subject.getTitleString()).containsOnlyOnce("food in french");
    }

    @Test
    void shouldConstructPageTitle_withTwoPrograms() {
        ProgramSelection programSelection = new ProgramSelection();
        SortedSet<BenefitProgram> programs = new TreeSet<>(List.of(EMERGENCY, CHILD_CARE));
        programSelection.setPrograms(programs);
        ProgramSelectionPresenter subject = new ProgramSelectionPresenter(programSelection, messageSource, defaultLocale);

        assertThat(subject.getTitleString()).isEqualTo("emergency and child care");
    }

    @Test
    void shouldConstructPageTitle_withMoreThanTwoPrograms() {
        ProgramSelection programSelection = new ProgramSelection();
        SortedSet<BenefitProgram> programs = new TreeSet<>(List.of(EMERGENCY, CHILD_CARE, CASH, FOOD));
        programSelection.setPrograms(programs);
        ProgramSelectionPresenter subject = new ProgramSelectionPresenter(programSelection, messageSource, defaultLocale);

        assertThat(subject.getTitleString()).containsPattern(Pattern.compile("[a-z ]+, [a-z ]+, [a-z ]+ and [a-z ]+"));
        assertThat(subject.getTitleString()).containsOnlyOnce("emergency");
        assertThat(subject.getTitleString()).containsOnlyOnce("child care");
        assertThat(subject.getTitleString()).containsOnlyOnce("cash");
        assertThat(subject.getTitleString()).containsOnlyOnce("food");
    }
}