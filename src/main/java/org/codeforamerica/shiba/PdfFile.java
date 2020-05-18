package org.codeforamerica.shiba;

import lombok.Value;

@Value
public class PdfFile{
    byte[] fileBytes;
    String fileName;
}

