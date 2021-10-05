package org.codeforamerica.shiba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.MILLE_LACS_BAND_OF_OJIBWE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RoutingDestinationMessageServiceTest {
  @Autowired
  private MessageSource messageSource;
  @MockBean
  private RoutingDecisionService routingDecisionService;
  private List<RoutingDestination> routingDestinations;

  @Test
  void generateCcapMessageStrings() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(CountyRoutingDestination.builder().county(Anoka).phoneNumber("555-5555").build());
    // Add a Tribal Routing Destination to assert that it isn't included in the final output
    routingDestinations.add(new TribalNationRoutingDestination(MILLE_LACS_BAND_OF_OJIBWE));
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(routingDestinations);

    assertThat(routingDestinationMessageService.generateCcapMessageStrings(LocaleContextHolder.getLocale(),
        Anoka, routingDestinations)).isEqualTo(List.of("Anoka County", "Anoka County 555-5555"));
  }

  @Test
  void generateCafMessageStringsWhenCountyAndTribalNation() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(CountyRoutingDestination.builder().county(Anoka).phoneNumber("555-5555").build());
    routingDestinations.add(new TribalNationRoutingDestination(MILLE_LACS_BAND_OF_OJIBWE, "someFolderId", "someProviderId", "someEmail", "222-2222"));
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(routingDestinations);

    assertThat(routingDestinationMessageService.generateCafMessageStrings(LocaleContextHolder.getLocale(),
        Anoka, routingDestinations)).isEqualTo(List.of("Anoka County and Mille Lacs Band of Ojibwe Tribal Nation Servicing Agency",
        "Anoka County 555-5555 and Mille Lacs Band of Ojibwe Tribal Nation Servicing Agency 222-2222"));
  }

  @Test
  void generateCafMessageStringsWhenCountyOnly() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(CountyRoutingDestination.builder().county(Anoka).phoneNumber("555-5555").build());
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(routingDestinations);

    assertThat(routingDestinationMessageService.generateCafMessageStrings(LocaleContextHolder.getLocale(),
        Anoka, routingDestinations)).isEqualTo(List.of("Anoka County", "Anoka County 555-5555"));
  }

  @Test
  void generateCafMessageStringsWhenTribalNationOnly() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(new TribalNationRoutingDestination(MILLE_LACS_BAND_OF_OJIBWE, "someFolderId", "someProviderId", "someEmail", "222-2222"));
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(routingDestinations);

    assertThat(routingDestinationMessageService.generateCafMessageStrings(LocaleContextHolder.getLocale(),
        Anoka, routingDestinations)).isEqualTo(List.of("Mille Lacs Band of Ojibwe Tribal Nation Servicing Agency", "Mille Lacs Band of Ojibwe Tribal Nation Servicing Agency 222-2222"));
  }

  @Test
  void generateSuccessPageMessageStrings() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(CountyRoutingDestination.builder().county(Anoka).phoneNumber("555-5555").build());
    routingDestinations.add(new TribalNationRoutingDestination(MILLE_LACS_BAND_OF_OJIBWE, "someFolderId", "someProviderId", "someEmail", "222-2222"));
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(routingDestinations);

    assertThat(routingDestinationMessageService.generateSuccessPageMessageString(LocaleContextHolder.getLocale(),
        Anoka, routingDestinations)).isEqualTo("Anoka County 555-5555 and Mille Lacs Band of Ojibwe Tribal Nation Servicing Agency 222-2222");
  }
}