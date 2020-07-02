package org.codeforamerica.shiba.output;

import lombok.Value;

@Value
public class ApplicationFile {
    byte[] fileBytes;
    String fileName;
}