package org.codeforamerica.shiba.output.pdf;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PdfResourcesComponents {

  @Bean
  public List<Resource> getDefaultResources(@Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops) {

    return (List.of(coverPages, certainPops));
  }

  @Bean
  public List<Resource> getAdditionalIncome(
      @Value("classpath:certain-pops-additional-income.pdf") Resource certainPopsAddIncome) {
    return (List.of(certainPopsAddIncome));
  }

  @Bean
  public List<Resource> getAdditionalHousehold1(
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers) {
    return List.of(certainPopsAddHHMembers);
  }

  @Bean
  public List<Resource> getAdditionalHousehold2(
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1) {
    return List.of(certainPopsAddHHMembers, certainPopsAddHHMembers1);
  }

  @Bean
  public List<Resource> getAdditionalHousehold3(
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2) {
    return List.of(certainPopsAddHHMembers, certainPopsAddHHMembers1, certainPopsAddHHMembers2);
  }

  @Bean
  public List<Resource> getAdditionalHousehold4(
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3) {
    return List.of(certainPopsAddHHMembers, certainPopsAddHHMembers1, certainPopsAddHHMembers2,
        certainPopsAddHHMembers3);
  }

  @Bean
  public List<Resource> getAdditionalHousehold5(
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4) {
    return List.of(certainPopsAddHHMembers, certainPopsAddHHMembers1, certainPopsAddHHMembers2,
        certainPopsAddHHMembers3, certainPopsAddHHMembers4);
  }

  @Bean
  public List<Resource> getAdditionalHousehold6(
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4,
      @Value("classpath:certain-pops-additional-household-members5.pdf") Resource certainPopsAddHHMembers5) {
    return List.of(certainPopsAddHHMembers, certainPopsAddHHMembers1, certainPopsAddHHMembers2,
        certainPopsAddHHMembers3, certainPopsAddHHMembers4, certainPopsAddHHMembers5);
  }

  @Bean
  public List<Resource> getAdditionalHousehold7(
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4,
      @Value("classpath:certain-pops-additional-household-members5.pdf") Resource certainPopsAddHHMembers5,
      @Value("classpath:certain-pops-additional-household-members6.pdf") Resource certainPopsAddHHMembers6) {
    return List.of(certainPopsAddHHMembers, certainPopsAddHHMembers1, certainPopsAddHHMembers2,
        certainPopsAddHHMembers3, certainPopsAddHHMembers4, certainPopsAddHHMembers5,
        certainPopsAddHHMembers6);
  }
  
  @Bean
  public List<Resource> getWhohasDisabilitySupp(
      @Value("classpath:certain-pops-add-disability.pdf") Resource whoHasDisability) {
    return List.of(whoHasDisability);
  }

  @Bean
  public Map<Recipient, Map<String, List<Resource>>> pdfResourceFillers(
      List<Resource> getDefaultResources, 
      List<Resource> getAdditionalIncome,
      List<Resource> getAdditionalHousehold1,
      List<Resource> getAdditionalHousehold2,
      List<Resource> getAdditionalHousehold3,
      List<Resource> getAdditionalHousehold4,
      List<Resource> getAdditionalHousehold5,
      List<Resource> getAdditionalHousehold6,
      List<Resource> getAdditionalHousehold7,
      List<Resource> getWhohasDisabilitySupp) {
    return Map.of(
        CASEWORKER,
        Map.of("default", getDefaultResources, 
            "addIncome", getAdditionalIncome,
            "addHousehold1.0", getAdditionalHousehold1,
            "addHousehold2.0", getAdditionalHousehold2,
            "addHousehold3.0", getAdditionalHousehold3,
            "addHousehold4.0", getAdditionalHousehold4,
            "addHousehold5.0", getAdditionalHousehold5,
            "addHousehold6.0", getAdditionalHousehold6,
            "addHousehold7.0", getAdditionalHousehold7,
            "addDisabilitySupp", getWhohasDisabilitySupp), 
        CLIENT,
        Map.of("default", getDefaultResources, 
            "addIncome", getAdditionalIncome,
            "addHousehold1.0", getAdditionalHousehold1,
            "addHousehold2.0", getAdditionalHousehold2,
            "addHousehold3.0", getAdditionalHousehold3,
            "addHousehold4.0", getAdditionalHousehold4,
            "addHousehold5.0", getAdditionalHousehold5,
            "addHousehold6.0", getAdditionalHousehold6,
            "addHousehold7.0", getAdditionalHousehold7,
            "addDisabilitySupp", getWhohasDisabilitySupp));
  }

}
