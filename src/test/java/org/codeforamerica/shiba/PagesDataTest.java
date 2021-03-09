package org.codeforamerica.shiba;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PagesDataTest {
    @Test
    void scrubsInputsInPagesData() throws JsonProcessingException {
        PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("contactInfo", Map.of(
                        "phoneNumber", List.of()
                )),
                new PageDataBuilder("choosePrograms", Map.of(
                        "programs", List.of("SNAP", "GRH", "EA", "CCAP")
                ))
        ));

        assertThat(new ObjectMapper().writeValueAsString(pagesData)).isEqualTo(
                "{\"contactInfo\":{\"phoneNumber\":\"\"},\"choosePrograms\":{\"programs\":\"filled\"}}"
        );
    }
}
