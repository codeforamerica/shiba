package org.codeforamerica.shiba;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {
    @Test
    void shouldConstructPageTitle_withASingleProgram() {
        String titleString = Utils.getTitleString(List.of("emergency"));

        assertThat(titleString).isEqualTo("emergency");
    }

    @Test
    void shouldConstructPageTitle_withTwoPrograms() {
        String titleString = Utils.getTitleString(List.of("emergency", "child care"));

        assertThat(titleString).isEqualTo("emergency and child care");
    }

    @Test
    void shouldConstructPageTitle_withMoreThanTwoPrograms() {
        String titleString = Utils.getTitleString(List.of("emergency", "child care", "cash", "food"));

        assertThat(titleString).isEqualTo("emergency, child care, cash and food");
    }
}