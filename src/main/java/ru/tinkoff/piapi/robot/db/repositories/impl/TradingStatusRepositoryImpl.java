package ru.tinkoff.piapi.robot.db.repositories.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.robot.db.repositories.TradingStatusRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TradingStatusRepositoryImpl implements TradingStatusRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final String GET_FIGI_BY_TRADING_STATUS = "Select figi from trading_status where status = :status";

    private final String ADD_TRADING_STATUS_HISTORY = "INSERT INTO trading_status_history (updated_at, status, status_updated_at, figi) VALUES  (now(), :status, :statusUpdatedAt, :figi)";

    private final String ADD_TRADING_STATUS = "INSERT INTO trading_status (figi, status) VALUES  (:figi, :status) " +
            "ON CONFLICT (figi) DO UPDATE SET status = :status";

    @Override
    public void addTradingStatus(String figi, String tradingStatus, Timestamp googleTimestamp) {
        jdbcTemplate.update(ADD_TRADING_STATUS, Map.of(
                "figi", figi,
                "status", tradingStatus)
        );
        addTradingStatusHistory(figi, tradingStatus, googleTimestamp);
    }

    private void addTradingStatusHistory(String figi, String tradingStatus, Timestamp googleTimestamp) {
        var tradingStatusUpdatedAt = java.sql.Timestamp.from(Instant.ofEpochSecond(googleTimestamp.getSeconds()));
        jdbcTemplate.update(ADD_TRADING_STATUS_HISTORY, Map.of(
                "status", tradingStatus,
                "figi", figi,
                "statusUpdatedAt", tradingStatusUpdatedAt)
        );
    }


    @Override
    public List<String> figiByTradingStatus(String tradingStatus) {
        return jdbcTemplate.query(GET_FIGI_BY_TRADING_STATUS, Map.of("status", tradingStatus), (rs, rowNum) -> rs.getString(1));
    }
}
