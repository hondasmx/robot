package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.robot.utils.DateUtils;

import java.util.Map;

@Repository
@AllArgsConstructor
@Slf4j
public class CandlesRepositoryImpl implements CandlesRepository {

    private final static String INSERT_CANDLE = "insert into candles (figi, timestamp) values (:figi, :timestamp)";
    private final static String LAST_CANDLE = "select timestamp from candles order by timestamp desc limit 1";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addCandle(String figi, Timestamp timestamp) {
        jdbcTemplate.update(INSERT_CANDLE, Map.of("figi", figi, "timestamp", DateUtils.timestampToDate(timestamp)));
    }

    @Override
    public java.sql.Timestamp lastCandle() {
        return jdbcTemplate.query(LAST_CANDLE, (rs, rowNum) -> rs.getTimestamp(1)).get(0);
    }
}
