package org.iot.dsa.servicebus.node;

public interface Handler<T> {

    void handle(T event);

}
