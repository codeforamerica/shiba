package org.codeforamerica.shiba.pages;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper=false)
@Value
public class ApplicationSubmittedEvent extends ApplicationEvent {
    String applicationId;

    public ApplicationSubmittedEvent(Object source, String applicationId) {
        super(source);
        this.applicationId = applicationId;
    }
}
