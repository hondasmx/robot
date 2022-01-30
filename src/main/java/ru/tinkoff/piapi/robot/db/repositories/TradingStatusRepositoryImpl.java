package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TradingStatusRepositoryImpl implements TradingStatusRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final String GET_FIGI_BY_TRADING_STATUS = "Select figi, trading_status, trading_status_updated_at " +
            "from (select figi, " +
            "             trading_status, " +
            "             trading_status_updated_at, " +
            "             row_number() over (partition by figi order by trading_status_updated_at desc) as num " +
            "      from trading_status) a " +
            "where num = 1 and trading_status = 'SECURITY_TRADING_STATUS_NORMAL_TRADING'";

    private final String UPDATE_TRADING_STATUS = "INSERT INTO trading_status (updated_at, trading_status, trading_status_updated_at, figi) VALUES  (now(), :tradingStatus, :tradingStatusUpdatedAt, :figi)";
    @Override
    public void addTradingStatus(String figi, String tradingStatus, Timestamp googleTimestamp) {
        var tradingStatusUpdatedAt = java.sql.Timestamp.from(Instant.ofEpochSecond(googleTimestamp.getSeconds()));
        jdbcTemplate.update(UPDATE_TRADING_STATUS, Map.of("tradingStatus", tradingStatus, "figi", figi, "tradingStatusUpdatedAt", tradingStatusUpdatedAt));
    }


    @Override
    public List<String> figiByTradingStatus(String tradingStatus) {
        return jdbcTemplate.query(GET_FIGI_BY_TRADING_STATUS, Map.of("tradingStatus", tradingStatus), (rs, rowNum) -> rs.getString(1));
    }
}
