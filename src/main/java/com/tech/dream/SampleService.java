package com.tech.dream;

import com.tech.dream.util.Constants.OrderEvents;
import com.tech.dream.util.Constants.OrderStates;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

@Component
public class SampleService implements ApplicationRunner {

    private final StateMachineFactory<OrderStates,OrderEvents> factory;


    public SampleService(StateMachineFactory<OrderStates,OrderEvents> factory) {
        this.factory = factory;
    }


	@Override
	public void run(ApplicationArguments args) throws Exception {
        StateMachine<OrderStates, OrderEvents> machine = this.factory.getStateMachine("1232");
        machine.start();
		System.out.println("current state: " + machine.getState().getId().name() );
	}

}