package org.codeforamerica.shiba.statemachine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void shouldRespondToOneEvent() {
        boolean eventResult = this.testMachine.sendEvent(StatesAndEvents.DeliveryEvents.SENDING_APP);
        assertThat(eventResult).isEqualTo(true);
        assertThat(this.testMachine.getState().getId()).isEqualTo(StatesAndEvents.DeliveryStates.APPLICATION_SENDING);
    }

    @AfterEach
    void tearDown() {
        service.releaseStateMachine("testMachine");
    }
}


