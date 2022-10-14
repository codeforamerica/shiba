package org.codeforamerica.shiba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.TribalNation.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
  void generatesMessageStringsWhenCountyAndTribalNation() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(
        new CountyRoutingDestination(Anoka, "DPI", "email", "555-5555"));
    routingDestinations.add(new TribalNationRoutingDestination(MilleLacsBandOfOjibwe,
        "someProviderId", "someEmail", "222-2222"));
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(
        messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(
        routingDestinations);

    assertThat(routingDestinationMessageService.generatePhrase(LocaleContextHolder.getLocale(),
        Anoka, true, routingDestinations)).isEqualTo(
        "Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222)");
  }

  @Test
  void generatesMessageStringsWithoutPhoneNumbers() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(
        new CountyRoutingDestination(Anoka, "DPI", "email", "555-5555"));
    routingDestinations.add(new TribalNationRoutingDestination(MilleLacsBandOfOjibwe,
        "someProviderId", "someEmail", "222-2222"));
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(
        messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(
        routingDestinations);

    assertThat(routingDestinationMessageService.generatePhrase(LocaleContextHolder.getLocale(),
        Anoka, false, routingDestinations)).isEqualTo(
        "Anoka County and Mille Lacs Band of Ojibwe");
  }

  @Test
  void generatesMessageStringsWhenCountyOnly() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(new CountyRoutingDestination(Anoka, "DPI", "email", "555-5555"));
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(routingDestinations);

    assertThat(routingDestinationMessageService.generatePhrase(LocaleContextHolder.getLocale(),
        Anoka, true, routingDestinations)).isEqualTo("Anoka County (555-5555)");
  }

  @Test
  void generatesMessageStringsWhenTribalNationOnly() {
    routingDestinations = new ArrayList<>();
    routingDestinations.add(new TribalNationRoutingDestination(MilleLacsBandOfOjibwe,
        "someProviderId", "someEmail", "222-2222"));
    RoutingDestinationMessageService routingDestinationMessageService = new RoutingDestinationMessageService(
        messageSource);
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(
        routingDestinations);

    assertThat(routingDestinationMessageService.generatePhrase(LocaleContextHolder.getLocale(),
        Anoka, true, routingDestinations)).isEqualTo(
        "Mille Lacs Band of Ojibwe (222-2222)");
  }
}