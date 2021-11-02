package org.codeforamerica.shiba;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.data.MaskedSerializer;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.Test;

public class PagesDataTest {

  @Test
  void scrubsInputsInPagesData() throws JsonProcessingException {
    PagesData pagesData = new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "phoneNumber", List.of(),
            "firstName", List.of("Fake")))
        .withPageData("choosePrograms", "programs", List.of("SNAP", "GRH", "EA", "CCAP"))
        .build();

    ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new SimpleModule().addSerializer(PageData.class, new MaskedSerializer()));
    assertThat(objectMapper.writeValueAsString(pagesData)).isEqualTo(
        "{\"" +
        "contactInfo\":{" +
        "\"firstName\":\"filled\"," +
        "\"phoneNumber\":\"\"" +
        "}," +
        "\"choosePrograms\":" +
        "{\"programs\":\"filled\"" +
        "}" +
        "}"
    );
  }
}
