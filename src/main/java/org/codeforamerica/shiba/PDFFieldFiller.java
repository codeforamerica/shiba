package org.codeforamerica.shiba;

import java.util.Collection;

public interface PDFFieldFiller {
    PdfFile fill(Collection<PDFField> fields);
}
