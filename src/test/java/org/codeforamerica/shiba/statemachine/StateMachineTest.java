package org.codeforamerica.shiba.statemachine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
public class StateMachineTest {

    @Autowired
    private StateMachineService<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> service;
    private StateMachine<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> testMachine;

    @BeforeEach
    void setUp() {
        assertNotNull(this.service);
        this.testMachine = service.acquireStateMachine("testMachine");
        assertNotNull(this.testMachine);
    }

    @Test
    void shouldStartinReadyState() {
        assertThat(this.testMachine.getState().getId()).isEqualTo(StatesAndEvents.DeliveryStates.READY);
    }

    @Test
    void shouldRespondToOneEvent() throws Exception {

        Message<StatesAndEvents.DeliveryEvents> event = MessageBuilder.withPayload(StatesAndEvents.DeliveryEvents.SENDING_APP).build();
        AtomicBoolean complete = new AtomicBoolean(false);

        this.testMachine.sendEvent(Mono.just(event))
                .doOnComplete(() -> {
                    complete.set(true);
                })
                .doOnError(t -> { fail("Sending event to test machine failed: " + t.getMessage());
                })
                .subscribe();

        assertThat(complete.get()).isEqualTo(true);
        assertThat(this.testMachine.getState().getId()).isEqualTo(StatesAndEvents.DeliveryStates.APPLICATION_SENDING);
    }

    @Test
    void fullSequencewithNoErrors() {

        Message<StatesAndEvents.DeliveryEvents> sending_event = MessageBuilder.withPayload(StatesAndEvents.DeliveryEvents.SENDING_APP).build();

        AtomicBoolean complete = new AtomicBoolean(false);

        this.testMachine.sendEvent(Mono.just(sending_event))
                .doOnComplete(() -> {
                    complete.set(true);
                })
                .doOnError(t -> { fail("Sending event to test machine failed: " + t.getMessage());
                })
                .subscribe();

        assertThat(complete.get()).isEqualTo(true);

        assertThat(this.testMachine.getState().getId()).isEqualTo(StatesAndEvents.DeliveryStates.APPLICATION_SENDING);

        Message<StatesAndEvents.DeliveryEvents> success_event = MessageBuilder.withPayload(StatesAndEvents.DeliveryEvents.DELIVERY_SUCCESS).build();

        this.testMachine.sendEvent(Mono.just(success_event))
                .doOnComplete(() -> {
                    complete.set(true);
                })
                .doOnError(t -> { fail("Sending event to test machine failed: " + t.getMessage());
                })
                .subscribe();

        assertThat(complete.get()).isEqualTo(true);

        assertThat(this.testMachine.getState().getId()).isEqualTo(StatesAndEvents.DeliveryStates.SENT);
    }

    @AfterEach
    void tearDown() {
        service.releaseStateMachine("testMachine");
    }
}


