package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.util.ArrayList;
import java.util.List;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;

public class DocumentListParser {
    public static List<Document> parse(ApplicationData applicationData) {
        ArrayList<Document> documents = new ArrayList<>();
        if (applicationData.isCCAPApplication()) {
            documents.add(CCAP);
        }
        if (applicationData.isCAFApplication()) {
            documents.add(CAF);
        }

        return documents;
    }
}
