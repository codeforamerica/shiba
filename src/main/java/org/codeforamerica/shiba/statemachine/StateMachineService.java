package org.codeforamerica.shiba.statemachine;

import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.stereotype.Service;

@Service
public  class StateMachineService extends DefaultStateMachineService {

    public StateMachineService(StateMachineFactory stateMachineFactory) {
        super(stateMachineFactory);
    }
}
