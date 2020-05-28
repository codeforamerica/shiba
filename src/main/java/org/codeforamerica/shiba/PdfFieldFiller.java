package org.codeforamerica.shiba;

import java.util.Collection;

public interface PdfFieldFiller {
    ApplicationFile fill(Collection<PdfField> fields);
}
