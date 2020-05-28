package org.codeforamerica.shiba.pdf;

import org.codeforamerica.shiba.ApplicationFile;

import java.util.Collection;

public interface PdfFieldFiller {
    ApplicationFile fill(Collection<PdfField> fields);
}
