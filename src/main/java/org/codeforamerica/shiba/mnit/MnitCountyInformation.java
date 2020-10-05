package org.codeforamerica.shiba.mnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MnitCountyInformation {
    private String folderId;
    private String dhsProviderId;
    private String email;
}
