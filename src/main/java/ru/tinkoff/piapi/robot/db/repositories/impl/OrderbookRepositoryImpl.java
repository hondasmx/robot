package ru.tinkoff.piapi.robot.db.repositories.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.robot.db.repositories.OrderbookRepository;
import ru.tinkoff.piapi.robot.utils.DateUtils;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Repository
public class OrderbookRepositoryImpl implements OrderbookRepository {

    private final static String INSERT_ORDERBOOK = "insert into orderbook (figi, timestamp) values (:figi, :timestamp)";

    private final static String LAST_ORDERBOOK_BY_INSTRUMENT_TYPE = "select max(created_at) from orderbook join instruments i on orderbook.figi = i.figi where i.instrument_type = :instrumentType";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addOrderbook(String figi, Timestamp timestamp) {
        jdbcTemplate.update(INSERT_ORDERBOOK, Map.of("figi", figi, "timestamp", DateUtils.timestampToDate(timestamp)));
    }

    @Override
    public java.sql.Timestamp lastOrderbook(String instrumentType) {
        return jdbcTemplate.query(LAST_ORDERBOOK_BY_INSTRUMENT_TYPE, Map.of("instrumentType", instrumentType), (rs, rowNum) -> rs.getTimestamp(1)).get(0);
    }
}
