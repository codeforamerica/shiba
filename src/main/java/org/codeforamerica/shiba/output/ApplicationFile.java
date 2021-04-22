package org.codeforamerica.shiba.output;

import lombok.*;

@Value
@ToString(exclude = {"fileBytes"})
public class ApplicationFile {
    byte[] fileBytes;
    String fileName;
}