package ru.tinkoff.piapi.robot.db.repositories.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.robot.db.repositories.LastPricesRepository;
import ru.tinkoff.piapi.robot.utils.DateUtils;
import ru.tinkoff.piapi.robot.utils.MoneyUtils;

import java.util.Map;

@RequiredArgsConstructor
@Repository
public class LastPricesRepositoryImpl implements LastPricesRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final String INSERT_SQL = "insert into last_prices (figi, last_price, timestamp) values (:figi, :lastPrice, :timestamp) " +
            "ON CONFLICT (figi) DO UPDATE SET (figi, last_price, timestamp) = (excluded.figi, excluded.last_price, excluded.timestamp)";

    @Override
    public void addLastPrice(LastPrice lastPrice) {
        var figi = lastPrice.getFigi();
        var price = lastPrice.getPrice();
        var timestamp = lastPrice.getTime();
        addLastPrice(figi, price, timestamp);
    }

    @Override
    public void addLastPrice(String figi, Quotation lastPrice, Timestamp timestamp) {
        jdbcTemplate.update(INSERT_SQL, Map.of("figi", figi, "lastPrice", MoneyUtils.quotationToBigDecimal(lastPrice), "timestamp", DateUtils.timestampToDate(timestamp)));
    }
}
