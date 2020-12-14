package org.codeforamerica.shiba.output.xml;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.DocumentType;
import org.codeforamerica.shiba.output.Recipient;

public interface FileGenerator {
    ApplicationFile generate(String applicationId, DocumentType documentType, Recipient recipient);
}
