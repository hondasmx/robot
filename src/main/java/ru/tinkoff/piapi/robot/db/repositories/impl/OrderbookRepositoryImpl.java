package ru.tinkoff.piapi.robot.db.repositories.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.contract.v1.OrderBook;
import ru.tinkoff.piapi.robot.db.repositories.OrderbookRepository;
import ru.tinkoff.piapi.robot.utils.DateUtils;
import ru.tinkoff.piapi.robot.utils.MoneyUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Repository
public class OrderbookRepositoryImpl implements OrderbookRepository {

    private final static String INSERT_ORDERBOOK = "insert into orderbook (figi, timestamp, bid, ask, is_consistent) values (:figi, :timestamp, :bid, :ask, :isConsistent)";

    private final static String LAST_ORDERBOOK_BY_INSTRUMENT_TYPE = "select max(created_at) from orderbook join instruments i on orderbook.figi = i.figi where i.instrument_type = :instrumentType";

    private final static String FAILED_ORDERBOOK = "select * from orderbook where now()::timestamptz - created_at <= interval '10 minutes' and bid > ask";

    private final static String TIME_DIFF_ORDERBOOK = "select figi, created_at, timestamp, created_at - timestamp as diff from orderbook where now()::timestamptz - created_at <= interval '10 minutes' and created_at - timestamp >= interval '5 minutes'";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addOrderbook(OrderBook orderbook) {
        var figi = orderbook.getFigi();
        var time = orderbook.getTime();

        var bids = orderbook.getBidsList();
        var lastBid = bids.size() > 0 ? MoneyUtils.quotationToBigDecimal(bids.get(0).getPrice()) : BigDecimal.ZERO;

        var asks = orderbook.getAsksList();
        var lastAsk = asks.size() > 0 ? MoneyUtils.quotationToBigDecimal(asks.get(0).getPrice()) : BigDecimal.ZERO;

        var isConsistent = orderbook.getIsConsistent();
        jdbcTemplate.update(INSERT_ORDERBOOK, Map.of(
                "figi", figi,
                "timestamp", DateUtils.timestampToDate(time),
                "bid", lastBid,
                "ask", lastAsk,
                "isConsistent", isConsistent
        ));
    }

    @Override
    public java.sql.Timestamp lastOrderbook(String instrumentType) {
        return jdbcTemplate.query(LAST_ORDERBOOK_BY_INSTRUMENT_TYPE, Map.of("instrumentType", instrumentType), (rs, rowNum) -> rs.getTimestamp(1)).get(0);
    }

    @Override
    public List<OrderbookResponse> failedOrderbook() {
        return jdbcTemplate.query(FAILED_ORDERBOOK, new HashMap<>(), (rs, rowNum) -> new OrderbookResponse(
                rs.getBigDecimal("bid"),
                rs.getBigDecimal("ask"),
                rs.getString("figi"),
                rs.getTimestamp("timestamp"),
                rs.getTimestamp("created_at")
        ));
    }

    @Override
    public List<TimeDiffResponse> timeDiffOrderbook() {
        return jdbcTemplate.query(TIME_DIFF_ORDERBOOK, new HashMap<>(), (rs, rowNum) -> new TimeDiffResponse(
                rs.getString("figi"),
                rs.getTimestamp("timestamp"),
                DateUtils.millisToString(rs.getTimestamp("diff").getTime())
        ));
    }

    @Data
    @AllArgsConstructor
    public static class OrderbookResponse {
        BigDecimal bid;
        BigDecimal ask;
        String figi;
        Timestamp timestamp;
        Timestamp createdAt;
    }

    @Data
    @AllArgsConstructor
    public static class TimeDiffResponse {
        String figi;
        Timestamp timestamp;
        String diff;
    }
}
