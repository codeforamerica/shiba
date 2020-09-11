package org.codeforamerica.shiba.output;

import lombok.Value;

@Value
public class DownloadCafEvent {
    String confirmationNumber;
    String ip;
}
