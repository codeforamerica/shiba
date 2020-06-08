package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThatThrownBy(() -> new ProgramSelectionPresenter(messageSource, defaultLocale, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldConstructPageTitle_withASingleProgram() {
        ProgramSelectionPresenter subject = new ProgramSelectionPresenter(messageSource, defaultLocale, List.of("EMERGENCY"));

        assertThat(subject.getTitleString()).isEqualTo("emergency");
    }

    @Test
    void shouldConstructPageTitle_withMultiplePrograms_inTheGivenLocale() {
        messageSource.addMessage("how-it-works.emergency", Locale.FRENCH, "emergency in french");
        messageSource.addMessage("how-it-works.child-care", Locale.FRENCH, "child care in french");
        messageSource.addMessage("how-it-works.food", Locale.FRENCH, "food in french");
        messageSource.addMessage("how-it-works.cash", Locale.FRENCH, "cash in french");
        ProgramSelectionPresenter subject = new ProgramSelectionPresenter(messageSource, Locale.FRENCH, List.of("EMERGENCY", "CHILD_CARE", "FOOD", "CASH"));

        assertThat(subject.getTitleString()).containsOnlyOnce("emergency in french");
        assertThat(subject.getTitleString()).containsOnlyOnce("child care in french");
        assertThat(subject.getTitleString()).containsOnlyOnce("cash in french");
        assertThat(subject.getTitleString()).containsOnlyOnce("food in french");
    }

    @Test
    void shouldConstructPageTitle_withTwoPrograms() {
        ProgramSelectionPresenter subject = new ProgramSelectionPresenter(messageSource, defaultLocale, List.of("EMERGENCY", "CHILD_CARE"));

        assertThat(subject.getTitleString()).matches(Pattern.compile("[a-z ]+ and [a-z ]+"));
        assertThat(subject.getTitleString()).containsOnlyOnce("emergency");
        assertThat(subject.getTitleString()).containsOnlyOnce("child care");
    }

    @Test
    void shouldConstructPageTitle_withMoreThanTwoPrograms() {
        ProgramSelectionPresenter subject = new ProgramSelectionPresenter(messageSource, defaultLocale, List.of("EMERGENCY", "CHILD_CARE", "CASH", "FOOD"));

        assertThat(subject.getTitleString()).matches(Pattern.compile("[a-z ]+, [a-z ]+, [a-z ]+ and [a-z ]+"));
        assertThat(subject.getTitleString()).containsOnlyOnce("emergency");
        assertThat(subject.getTitleString()).containsOnlyOnce("child care");
        assertThat(subject.getTitleString()).containsOnlyOnce("cash");
        assertThat(subject.getTitleString()).containsOnlyOnce("food");
    }
}