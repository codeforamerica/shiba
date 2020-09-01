package org.codeforamerica.shiba;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ConfirmationData {
    private String id;
    private ZonedDateTime completedAt;
    private County county;

    public void clear() {
        this.id = null;
        this.completedAt = null;
        this.county = null;
    }
}
