package ru.tinkoff.piapi.robot.processor;

public interface StreamProcessor<T> {

    void process(T response);

    String streamName();
}