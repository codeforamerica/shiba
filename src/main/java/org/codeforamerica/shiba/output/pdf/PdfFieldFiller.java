package org.codeforamerica.shiba.output.pdf;

import java.util.Collection;
import org.codeforamerica.shiba.output.ApplicationFile;

public interface PdfFieldFiller {

  ApplicationFile fill(Collection<PdfField> fields, String applicationId, String fileName);
}
