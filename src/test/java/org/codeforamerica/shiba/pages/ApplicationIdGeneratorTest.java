package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.ApplicationRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationIdGeneratorTest {

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    ApplicationIdGenerator applicationIdGenerator = new ApplicationIdGenerator(applicationRepository);

    @Test
    void shouldGenerateIdForNextApplication() {
        when(applicationRepository.getNextId()).thenReturn(22);

        String id = applicationIdGenerator.generate();

        assertThat(id).contains("22");
        assertThat(id).hasSize(10);

        String idAgain = applicationIdGenerator.generate();

        assertThat(id).isNotEqualTo(idAgain);
    }

    @Test
    void shouldPadZerosSoIdSizeIs10() {
        when(applicationRepository.getNextId()).thenReturn(22222);

        String id = applicationIdGenerator.generate();

        assertThat(id).hasSize(10);
    }
}