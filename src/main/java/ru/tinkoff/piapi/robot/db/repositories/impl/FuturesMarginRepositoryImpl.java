package ru.tinkoff.piapi.robot.db.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.contract.v1.GetFuturesMarginResponse;
import ru.tinkoff.piapi.robot.db.repositories.FuturesMarginRepository;
import ru.tinkoff.piapi.robot.utils.MoneyUtils;

import java.util.Map;

@RequiredArgsConstructor
@Repository
public class FuturesMarginRepositoryImpl implements FuturesMarginRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final String INSERT_SQL = "insert into futures_margin (figi, margin_on_buy, margin_on_sell, min_price_increment, min_price_increment_amount) values (:figi, :marginOnBuy, :marginOnSell, :minPriceIncrement, :minPriceIncrementAmount) " +
            "ON CONFLICT (figi) DO UPDATE SET (margin_on_buy, margin_on_sell, min_price_increment, min_price_increment_amount, created_at) = (excluded.margin_on_buy, excluded.margin_on_sell, excluded.min_price_increment, excluded.min_price_increment_amount, now())";


    @Override
    public void addFutureMargin(String figi, GetFuturesMarginResponse resp) {
        var marginOnBuy = MoneyUtils.moneyValueToBigDecimal(resp.getInitialMarginOnBuy());
        var marginOnSell = MoneyUtils.moneyValueToBigDecimal(resp.getInitialMarginOnBuy());
        var minPriceIncrement = MoneyUtils.quotationToBigDecimal(resp.getMinPriceIncrement());
        var minPriceIncrementAmount = MoneyUtils.quotationToBigDecimal(resp.getMinPriceIncrementAmount());
        jdbcTemplate.update(INSERT_SQL, Map.of(
                "figi", figi,
                "marginOnBuy", marginOnBuy,
                "marginOnSell", marginOnSell,
                "minPriceIncrement", minPriceIncrement,
                "minPriceIncrementAmount", minPriceIncrementAmount)
        );
    }
}
