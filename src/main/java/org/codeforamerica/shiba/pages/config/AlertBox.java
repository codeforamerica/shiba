package org.codeforamerica.shiba.pages.config;

import lombok.Data;

@Data
public class AlertBox {
    private AlertBoxType type;
    private String message;

    public String getAlertBoxType() {
        return switch (type) {
            case CHOOSE_NONE_WARNING -> "warning";
            case NOTICE -> "notice";
        };
    }
}
