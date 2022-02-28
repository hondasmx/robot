package ru.tinkoff.piapi.robot.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.db.repositories.ErrorsRepository;

import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class Helpers {

    private final ErrorsRepository errorsRepository;

    public <T> T unaryCall(Supplier<T> consumer) {
        try {
            return consumer.get();
        } catch (Exception e) {
            var message = e.getMessage();
            var type = e.getClass().getName();
            errorsRepository.add(type, message);
            throw e;
        }
    }
}
