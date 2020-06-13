package com.tech.dream.service.order;

import java.util.Optional;

import com.tech.dream.db.entity.Order;
import com.tech.dream.db.repository.OrderRepository;
import com.tech.dream.util.Constants;
import com.tech.dream.util.Constants.OrderEvents;
import com.tech.dream.util.Constants.OrderStates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;


@Component
public class OrderStateMachine {

    @Autowired
    private OrderRepository repository;
    
    private static final String ORDER_ID_HEADER = "orderId";
    private final StateMachineFactory<OrderStates,OrderEvents> factory;

    public OrderStateMachine(StateMachineFactory<OrderStates,OrderEvents> factory) {
        this.factory = factory;
    }

    public boolean acceptOrder(Order order){
        StateMachine<OrderStates,OrderEvents> sm = this.build(order);
        Message<OrderEvents> acceptedOrder = MessageBuilder.withPayload(OrderEvents.SELLER_ACCEPT).setHeader(ORDER_ID_HEADER, order.getId()).build();
        return sm.sendEvent(acceptedOrder);
    }

    public boolean rejectOrder(Order order){
        StateMachine<OrderStates,OrderEvents> sm = this.build(order);
        Message<OrderEvents> acceptedOrder = MessageBuilder.withPayload(OrderEvents.SELLER_REJECT).setHeader(ORDER_ID_HEADER, order.getId()).build();
        return sm.sendEvent(acceptedOrder);
    }

    public boolean cancelOrder(Order order){
        StateMachine<OrderStates,OrderEvents> sm = this.build(order);
        Message<OrderEvents> cancelOrder = MessageBuilder.withPayload(OrderEvents.ORDER_CANCELLED).setHeader(ORDER_ID_HEADER, order.getId()).build();
        return sm.sendEvent(cancelOrder);
    }

    public boolean inTransitOrder(Order order){
        StateMachine<OrderStates,OrderEvents> sm = this.build(order);
        Message<OrderEvents> orderStatusUpdate = MessageBuilder.withPayload(OrderEvents.ORDER_INTRANSIT).setHeader(ORDER_ID_HEADER, order.getId()).build();
        return sm.sendEvent(orderStatusUpdate);
    }

    public boolean deliveredOrder(Order order){
        StateMachine<OrderStates,OrderEvents> sm = this.build(order);
        Message<OrderEvents> orderStatusUpdate = MessageBuilder.withPayload(OrderEvents.ORDER_DELIVERED).setHeader(ORDER_ID_HEADER, order.getId()).build();
        return sm.sendEvent(orderStatusUpdate);
    }

    public boolean notDeliveredOrder(Order order){
        StateMachine<OrderStates,OrderEvents> sm = this.build(order);
        Message<OrderEvents> orderStatusUpdate = MessageBuilder.withPayload(OrderEvents.ORDER_NOTDELIVERED).setHeader(ORDER_ID_HEADER, order.getId()).build();
        return sm.sendEvent(orderStatusUpdate);
    }
    
    private StateMachine<OrderStates, OrderEvents> build (Order order){
        String orderIdKey = Long.toString(order.getId());

        StateMachine<OrderStates, OrderEvents> sm= this.factory.getStateMachine(orderIdKey);
        sm.stop();

        sm.getStateMachineAccessor()
        .doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(new StateMachineInterceptorAdapter<OrderStates,OrderEvents>(){
                
                @Override
                public void preStateChange(State<OrderStates,OrderEvents> state, Message<OrderEvents> message, Transition<OrderStates,OrderEvents> transition, StateMachine<OrderStates,OrderEvents> stateMachine){
                    Optional.ofNullable(message).ifPresent(msg -> {
                        Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(ORDER_ID_HEADER, -1L)))
                        .ifPresent(orderId1 -> {
                            Order order1 = repository.getOne(orderId1);
                            order1.setStatus(state.getId().name());
                            repository.save(order1);
                        });;
                    });
                }                
            });
            sma.resetStateMachine(new DefaultStateMachineContext<Constants.OrderStates,Constants.OrderEvents>(OrderStates.valueOf(order.getStatus()),null,null,null ));
        });
        sm.start();
        return sm;
    }         

}