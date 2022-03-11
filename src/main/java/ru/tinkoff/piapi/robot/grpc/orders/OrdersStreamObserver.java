package ru.tinkoff.piapi.robot.grpc.orders;

import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import ru.tinkoff.piapi.robot.grpc.BaseService;
import ru.tinkoff.piapi.robot.processor.StreamProcessor;
import ru.tinkoff.piapi.robot.services.events.StreamErrorEvent;

@Getter
@Slf4j
@RequiredArgsConstructor
public class OrdersStreamObserver<T> implements StreamObserver<T> {
    private final StreamProcessor<T> streamProcessor;
    private final ApplicationEventPublisher publisher;
    private final BaseService baseService;


    @Override
    public void onNext(T value) {
        streamProcessor.process(value);
    }

    @Override
    public void onError(Throwable t) {
        log.error("onError was invoked. stream: {}, error: {}",
                streamProcessor.streamName(),
                t.toString());
        baseService.shutdown();
        publisher.publishEvent(new StreamErrorEvent(streamProcessor.streamName()));
    }

    @Override
    public void onCompleted() {
        log.info("onCompleted was invoked. stream: {}", streamProcessor.streamName());
    }
}
