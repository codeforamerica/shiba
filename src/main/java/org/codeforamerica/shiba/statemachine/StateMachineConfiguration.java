package org.codeforamerica.shiba.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

import javax.annotation.PostConstruct;

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
                .event(StatesAndEvents.DeliveryEvents.SENDING_APP)
                .target(StatesAndEvents.DeliveryStates.APPLICATION_SENDING)
                .and()
                .withExternal()
                .source(StatesAndEvents.DeliveryStates.READY)
                .event(StatesAndEvents.DeliveryEvents.SENDING_DOC)
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
                .state(StatesAndEvents.DeliveryStates.APPLICATION_SENDING)
                .state(StatesAndEvents.DeliveryStates.DOCUMENT_SENDING)
                .state(StatesAndEvents.DeliveryStates.RETRYING)
                .end(StatesAndEvents.DeliveryStates.SENT)
                .end(StatesAndEvents.DeliveryStates.FAILED);
    }


    @Configuration
    class ShibaMachineFactory {

        @Bean
        @Primary
        public StateMachineFactory<StatesAndEvents.DeliveryStates,StatesAndEvents.DeliveryEvents> getStateMachineFactory(StateMachineFactory<StatesAndEvents.DeliveryStates,StatesAndEvents.DeliveryEvents> stateMachineFactory) throws Exception {
            return stateMachineFactory;
        }
    }

    @Configuration
    public static class StateMachineServiceConfig {

        @Bean
        public StateMachineService<StatesAndEvents.DeliveryStates,StatesAndEvents.DeliveryEvents> stateMachineService(StateMachineFactory<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> stateMachineFactory) {
            return new DefaultStateMachineService<StatesAndEvents.DeliveryStates,StatesAndEvents.DeliveryEvents>(stateMachineFactory);
        }
    }
}





