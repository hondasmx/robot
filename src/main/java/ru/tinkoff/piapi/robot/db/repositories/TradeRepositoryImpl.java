package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.robot.utils.DateUtils;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Repository
public class TradeRepositoryImpl implements TradeRepository {

    private final static String INSERT_TRADES = "insert into trades (figi, timestamp, thread_name) values (:figi, :timestamp, :thread)";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addTrade(String figi, Timestamp timestamp) {
        var thread =  Thread.currentThread().getName();
        jdbcTemplate.update(INSERT_TRADES, Map.of("figi", figi, "timestamp", DateUtils.timestampToDate(timestamp), "thread", thread));
    }
}
