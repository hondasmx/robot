package ru.tinkoff.piapi.robot.db.repositories.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.robot.db.repositories.TradeRepository;
import ru.tinkoff.piapi.robot.utils.DateUtils;

import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Repository
public class TradeRepositoryImpl implements TradeRepository {

    private final static String INSERT_TRADES = "insert into trades (figi, timestamp, direction, lot, price) values (:figi, :timestamp, :direction, :lot, :price)";

    private final static String LAST_TRADE_BY_INSTRUMENT_TYPE = "select max(created_at) from trades join instruments i on trades.figi = i.figi where i.instrument_type = :instrumentType";


    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addTrade(String figi, Timestamp timestamp, String direction, long lot, BigDecimal price) {
        jdbcTemplate.update(INSERT_TRADES, Map.of(
                "figi", figi,
                "timestamp", DateUtils.timestampToDate(timestamp),
                "direction", direction,
                "lot", lot,
                "price", price
        ));
    }

    @Override
    public java.sql.Timestamp lastTrade(String instrumentType) {
        return jdbcTemplate.query(LAST_TRADE_BY_INSTRUMENT_TYPE, Map.of("instrumentType", instrumentType), (rs, rowNum) -> rs.getTimestamp(1)).get(0);
    }
}
