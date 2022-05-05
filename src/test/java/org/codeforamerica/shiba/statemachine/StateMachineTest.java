package org.codeforamerica.shiba.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class StateMachineTest {
    @Autowired
    private StateMachineService service;
    private StateMachine<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> testMachine;

    @BeforeEach
    void setUp() {
        this.testMachine = this.service.acquireStateMachine("testMachine");
    }

    @Test
    void shouldStartinReadyState() {
        assertThat(this.testMachine.getState().getId()).isEqualTo(StatesAndEvents.DeliveryStates.READY);
    }
}


