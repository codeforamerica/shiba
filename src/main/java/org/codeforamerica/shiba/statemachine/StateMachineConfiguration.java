package org.codeforamerica.shiba.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfiguration extends StateMachineConfigurerAdapter<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> config) throws Exception {
        config.withConfiguration().autoStartup(true);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> transitions) throws Exception {

        transitions
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.READY)
                .event(StatesAndEvents.DeliveryEvents.SENDING)
                .target(StatesAndEvents.DeliveryStates.APPLICATION_SENDING)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.READY)
                .event(StatesAndEvents.DeliveryEvents.SENDING)
                .target(StatesAndEvents.DeliveryStates.DOCUMENT_SENDING)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.APPLICATION_SENDING)
                .event(StatesAndEvents.DeliveryEvents.DELIVERY_SUCCESS)
                .target(StatesAndEvents.DeliveryStates.SENT)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.DOCUMENT_SENDING)
                .event(StatesAndEvents.DeliveryEvents.DELIVERY_SUCCESS)
                .target(StatesAndEvents.DeliveryStates.SENT)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.APPLICATION_SENDING)
                .event(StatesAndEvents.DeliveryEvents.SEND_ERROR)
                .target(StatesAndEvents.DeliveryStates.RETRYING)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.DOCUMENT_SENDING)
                .event(StatesAndEvents.DeliveryEvents.SEND_ERROR)
                .target(StatesAndEvents.DeliveryStates.RETRYING)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.RETRYING)
                .event(StatesAndEvents.DeliveryEvents.DELIVERY_SUCCESS)
                .target(StatesAndEvents.DeliveryStates.SENT)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.RETRYING)
                .event(StatesAndEvents.DeliveryEvents.SEND_ERROR)
                .target(StatesAndEvents.DeliveryStates.FAILED)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.RETRYING)
                .event(StatesAndEvents.DeliveryEvents.SEND_ERROR)
                .target(StatesAndEvents.DeliveryStates.FAILED);
    }

    @Override
    public void configure(StateMachineStateConfigurer<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> states) throws Exception {
        states
                .withStates()
                .initial(StatesAndEvents.DeliveryStates.READY)
                .end(StatesAndEvents.DeliveryStates.SENT)
                .end(StatesAndEvents.DeliveryStates.FAILED);
    }
}


