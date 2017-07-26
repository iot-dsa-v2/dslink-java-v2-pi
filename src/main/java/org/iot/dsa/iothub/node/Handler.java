package org.iot.dsa.iothub.node;

public interface Handler<T> {

    void handle(T event);

}
