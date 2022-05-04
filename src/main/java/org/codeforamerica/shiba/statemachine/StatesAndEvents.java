package org.codeforamerica.shiba.statemachine;

public class StatesAndEvents {

    // Events that cause a statemachine to transition states
    public enum DeliveryEvents {
        APPLICATION_SUBMITTED,
        DOCUMENT_SUBMITTED,
        SENDING,
        SEND_ERROR,
        RETRY_ERROR,
        DELIVERY_SUCCESS,
    }

    // Valid states for a statemachine instance
    public enum DeliveryStates {
        READY,
        APPLICATION_SENDING,
        DOCUMENT_SENDING,
        RETRYING,
        RESUBMITTING,
        SENT,
        FAILED
    }
}
