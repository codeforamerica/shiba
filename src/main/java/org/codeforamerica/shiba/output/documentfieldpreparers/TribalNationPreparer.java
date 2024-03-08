package org.codeforamerica.shiba.output.documentfieldpreparers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.LIVING_IN_TRIBAL_NATION_BOUNDARY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SELECTED_TRIBAL_NATION;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.TRIBAL_NATION;

import java.util.ArrayList;
import java.util.List;

@Component
public class TribalNationPreparer implements DocumentFieldPreparer {

    private static final List<String> TRIBES_REQUIRING_BOUNDARY_ANSWER = List.of(
    		 "Lower Sioux", "Upper Sioux", "Prairie Island","Red Lake Nation","Shakopee Mdewakanton"
    );

    @Override
    public List<DocumentField> prepareDocumentFields(Application application, Document document, Recipient recipient) {
        List<DocumentField> result = new ArrayList<>();
        Boolean isTribalNationMember = getBooleanValue(application.getApplicationData().getPagesData(), TRIBAL_NATION);
        String selectedTribe = getFirstValue(application.getApplicationData().getPagesData(), SELECTED_TRIBAL_NATION);
        Boolean livesInNationBoundary = getBooleanValue(application.getApplicationData().getPagesData(), LIVING_IN_TRIBAL_NATION_BOUNDARY);
        boolean tribeRequiresBoundaryAnswer = TRIBES_REQUIRING_BOUNDARY_ANSWER.contains(selectedTribe);
       
        if (Boolean.TRUE.equals(isTribalNationMember) && tribeRequiresBoundaryAnswer) {
        	 result.add(new DocumentField("nationsBoundary", "boundaryMember",livesInNationBoundary ? "Yes" : "No",
                     DocumentFieldType.SINGLE_VALUE));
            if (Boolean.TRUE.equals(livesInNationBoundary)) {
            	result.add(new DocumentField("nationsBoundary", "selectedNationBoundaryTribe", selectedTribe,
                        DocumentFieldType.SINGLE_VALUE));
            	}
       
    }

        return result;
    }


}

