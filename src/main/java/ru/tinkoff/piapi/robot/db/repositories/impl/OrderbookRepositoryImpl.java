package ru.tinkoff.piapi.robot.db.repositories.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Repository
public class OrderbookRepositoryImpl implements OrderbookRepository {

    private final static String INSERT_ORDERBOOK = "insert into orderbook (figi, timestamp, bid, ask, is_consistent, limit_up, limit_down) values (:figi, :timestamp, :bid, :ask, :isConsistent, :limitUp, :limitDown)";

    private final static String LAST_ORDERBOOK_BY_INSTRUMENT_TYPE = "select max(created_at) from orderbook join instruments i on orderbook.figi = i.figi where i.instrument_type = :instrumentType";

    private final static String FAILED_ORDERBOOK = "select * from orderbook where now()::timestamptz - created_at <= interval '10 minutes' and bid > ask and ask != 0 and bid != 0";

    private final static String TIME_DIFF_ORDERBOOK = "select figi, created_at, timestamp, EXTRACT(EPOCH FROM (created_at::timestamp - timestamp::timestamp)) as diff from orderbook where now()::timestamptz - created_at <= interval '10 minutes' and created_at - timestamp >= interval '5 minutes'";

    private final static String LIMITS_ORDERBOOK = "select *\n" +
            "from orderbook\n" +
            "where now()::timestamptz - created_at <= interval '60 minutes'\n" +
            "and limit_down != 0\n" +
            "and limit_up != 0\n" +
            "and bid != 0\n" +
            "and ask != 0\n" +
            "and (bid > orderbook.limit_up or bid < orderbook.limit_down or ask > orderbook.limit_up or ask < orderbook.limit_down )";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addOrderbook(OrderBook orderbook) {
        var figi = orderbook.getFigi();
        var time = orderbook.getTime();

        var bids = orderbook.getBidsList();
        var lastBid = bids.size() > 0 ? MoneyUtils.quotationToBigDecimal(bids.get(0).getPrice()) : BigDecimal.ZERO;

        var asks = orderbook.getAsksList();
        var lastAsk = asks.size() > 0 ? MoneyUtils.quotationToBigDecimal(asks.get(0).getPrice()) : BigDecimal.ZERO;

        var limitUp = MoneyUtils.quotationToBigDecimal(orderbook.getLimitUp());
        var limitDown = MoneyUtils.quotationToBigDecimal(orderbook.getLimitDown());

        var isConsistent = orderbook.getIsConsistent();
        jdbcTemplate.update(INSERT_ORDERBOOK, Map.of(
                "figi", figi,
                "timestamp", DateUtils.timestampToDate(time),
                "bid", lastBid,
                "ask", lastAsk,
                "limitUp", limitUp,
                "limitDown", limitDown,
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
                rs.getBigDecimal("limit_up"),
                rs.getBigDecimal("limit_down"),
                rs.getString("figi"),
                rs.getTimestamp("timestamp"),
                rs.getTimestamp("created_at")
        ));
    }

    @Override
    public Set<TimeDiffResponse> timeDiffOrderbook() {
        return new HashSet<>(jdbcTemplate.query(TIME_DIFF_ORDERBOOK, new HashMap<>(), (rs, rowNum) -> new TimeDiffResponse(
                rs.getString("figi"),
                rs.getTimestamp("timestamp"),
                DateUtils.secondsToString(rs.getInt("diff"))
        )));
    }

    @Override
    public Set<OrderbookResponse> failedLimits() {
        return new HashSet<>(jdbcTemplate.query(LIMITS_ORDERBOOK, new HashMap<>(), (rs, rowNum) -> new OrderbookResponse(
                rs.getBigDecimal("bid"),
                rs.getBigDecimal("ask"),
                rs.getBigDecimal("limit_up"),
                rs.getBigDecimal("limit_down"),
                rs.getString("figi"),
                rs.getTimestamp("timestamp"),
                rs.getTimestamp("created_at")
        )));
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"figi"})
    public static class OrderbookResponse {
        BigDecimal bid;
        BigDecimal ask;
        BigDecimal limitUp;
        BigDecimal limitDown;
        String figi;
        Timestamp timestamp;
        Timestamp createdAt;
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"figi"})
    public static class TimeDiffResponse {
        String figi;
        Timestamp timestamp;
        String diff;
    }
}
