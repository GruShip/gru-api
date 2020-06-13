package com.tech.dream.service.order;

import com.tech.dream.util.Constants.OrderEvents;
import com.tech.dream.util.Constants.OrderStates;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@Configuration
@EnableStateMachineFactory
public class SimpleEnumStateMachineConfiguration extends StateMachineConfigurerAdapter<OrderStates, OrderEvents>{

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStates, OrderEvents> config) throws Exception {
        StateMachineListenerAdapter<OrderStates, OrderEvents> adapter = new StateMachineListenerAdapter<OrderStates,OrderEvents>(){
            @Override
            public void stateChanged(State<OrderStates,OrderEvents> from, State<OrderStates,OrderEvents> to){
                System.out.printf("statechanged from: %s to: %s", from + " ", to + " ");
            };
        };
        config.withConfiguration()
        .autoStartup(false)
        .listener(adapter); 
        super.configure(config);
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderStates, OrderEvents> states) throws Exception {

        // initital - starting state 
        // state - intermediary states
        // end - end states (this can be the possible final state for an order)
        states.withStates()
            .initial(OrderStates.CREATED)
            .state(OrderStates.ACCEPTED)
            .stateEntry(OrderStates.ACCEPTED, new Action<OrderStates,OrderEvents>(){
                @Override
                public void execute(StateContext<OrderStates, OrderEvents> context) {
                    System.out.println("entering into ACCEPTED state");
                }
            })
            .state(OrderStates.ACCEPTED)
            .stateEntry(OrderStates.ACCEPTED, new Action<OrderStates,OrderEvents>(){
                @Override
                public void execute(StateContext<OrderStates, OrderEvents> context) {
                    System.out.println("entering into ACCEPTED state");
                }
            })
            .state(OrderStates.INTRANSIT)
            .stateEntry(OrderStates.INTRANSIT, new Action<OrderStates,OrderEvents>(){
                @Override
                public void execute(StateContext<OrderStates, OrderEvents> context) {
                    System.out.println("entering into INTRANSIT state");
                }
            })
            .end(OrderStates.REJECTED)
            .stateEntry(OrderStates.REJECTED, new Action<OrderStates,OrderEvents>(){
                @Override
                public void execute(StateContext<OrderStates, OrderEvents> context) {
                    System.out.println("entering into REJECTED state");
                }
            })
            .end(OrderStates.CANCELLED)
            .stateEntry(OrderStates.CANCELLED, new Action<OrderStates,OrderEvents>(){
                @Override
                public void execute(StateContext<OrderStates, OrderEvents> context) {
                    System.out.println("entering into CANCELLED state");
                }
            })
            .end(OrderStates.NOTDELIVERED)
            .stateEntry(OrderStates.NOTDELIVERED, new Action<OrderStates,OrderEvents>(){
                @Override
                public void execute(StateContext<OrderStates, OrderEvents> context) {
                    System.out.println("entering into NOTDELIVERED state");
                }
            })
            .end(OrderStates.DELIVERED)
            .stateEntry(OrderStates.DELIVERED, new Action<OrderStates,OrderEvents>(){
                @Override
                public void execute(StateContext<OrderStates, OrderEvents> context) {
                    System.out.println("entering into DELIVERED state");
                }
            })
            .end(OrderStates.RETURNED)
            .stateEntry(OrderStates.RETURNED, new Action<OrderStates,OrderEvents>(){
                @Override
                public void execute(StateContext<OrderStates, OrderEvents> context) {
                    System.out.println("entering into RETURNED state");
                }
            });
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStates, OrderEvents> transitions) throws Exception {

        // created to (accepted, rejected) from seller 
        // created to cancelled from buyer
        transitions
        .withExternal().source(OrderStates.CREATED).target(OrderStates.ACCEPTED).event(OrderEvents.SELLER_ACCEPT)
        .and()
        .withExternal().source(OrderStates.CREATED).target(OrderStates.REJECTED).event(OrderEvents.SELLER_REJECT)
        .and()
        .withExternal().source(OrderStates.CREATED).target(OrderStates.CANCELLED).event(OrderEvents.ORDER_CANCELLED)
        .and()
        
        // accepted to (intransit, delivered, notdelivered) from seller
        // accepted to cancelled from buyer
        .withExternal().source(OrderStates.ACCEPTED).target(OrderStates.INTRANSIT).event(OrderEvents.ORDER_INTRANSIT)
        .and()
        .withExternal().source(OrderStates.ACCEPTED).target(OrderStates.DELIVERED).event(OrderEvents.ORDER_DELIVERED)
        .and()
        .withExternal().source(OrderStates.ACCEPTED).target(OrderStates.NOTDELIVERED).event(OrderEvents.ORDER_NOTDELIVERED)
        .and()
        .withExternal().source(OrderStates.ACCEPTED).target(OrderStates.CANCELLED).event(OrderEvents.ORDER_CANCELLED)
        .and()

    
        // from intransit to (delivered, not delivered)
        .withExternal().source(OrderStates.INTRANSIT).target(OrderStates.DELIVERED).event(OrderEvents.ORDER_DELIVERED)
        .and()
        .withExternal().source(OrderStates.INTRANSIT).target(OrderStates.NOTDELIVERED).event(OrderEvents.ORDER_NOTDELIVERED);

    }
}