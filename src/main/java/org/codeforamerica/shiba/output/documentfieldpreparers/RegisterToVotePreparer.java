package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.REGISTER_TO_VOTE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class RegisterToVotePreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document _document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {
    return map(application.getApplicationData().getPagesData());
  }

  private List<DocumentField> map(PagesData pagesData) {
    String registerToVote = getFirstValue(pagesData, REGISTER_TO_VOTE);
    if (registerToVote == null) {
      // Not answered
      return Collections.emptyList();
    }

    return switch (registerToVote) {
      case "NO", "NO_ALREADY_REGISTERED" -> createApplicationInput("false");
      case "YES" -> createApplicationInput("true");
      default -> Collections.emptyList();
    };
  }

  @NotNull
  private List<DocumentField> createApplicationInput(String value) {
    return List.of(new DocumentField("registerToVote", "registerToVoteSelection",
        List.of(value),
        DocumentFieldType.ENUMERATED_SINGLE_VALUE));
  }
}
