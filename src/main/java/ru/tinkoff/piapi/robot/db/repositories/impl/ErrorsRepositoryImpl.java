package ru.tinkoff.piapi.robot.db.repositories.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.robot.db.repositories.ErrorsRepository;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Repository
public class ErrorsRepositoryImpl implements ErrorsRepository {

    private final static String INSERT = "insert into errors (exception_type, message) values (:exceptionType, :message)";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void add(String exceptionType, String message) {
        jdbcTemplate.update(INSERT, Map.of(
                "exceptionType", exceptionType,
                "message", message
        ));
    }
}
