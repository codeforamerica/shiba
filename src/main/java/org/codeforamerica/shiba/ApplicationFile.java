package org.codeforamerica.shiba;

import lombok.Value;

@Value
public class ApplicationFile {
    byte[] fileBytes;
    String fileName;
}