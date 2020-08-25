package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationFile;

import java.util.Collection;

public interface PdfFieldFiller {
    ApplicationFile fill(Collection<PdfField> fields, String applicationId);
}
