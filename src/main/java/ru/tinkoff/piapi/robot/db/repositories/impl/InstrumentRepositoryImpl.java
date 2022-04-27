package ru.tinkoff.piapi.robot.db.repositories.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.robot.db.entities.Instrument;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InstrumentRepositoryImpl implements InstrumentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // insert or replace
    private final String INSERT_SQL = "insert into instruments (figi, isin, class_code, ticker, instrument_type, api_trade_available_flag, otc_flag, exchange, lot, currency, real_exchange) values (:figi, :isin, :classCode, :ticker, :instrumentType, :apiTradeAvailableFlag, :otcFlag, :exchange, :lot, :currency, :realExchange)" +
            "ON CONFLICT (figi) DO UPDATE SET ( isin, class_code, ticker, instrument_type, api_trade_available_flag, otc_flag, exchange, lot, currency, real_exchange) = (excluded.isin, excluded.class_code, excluded.ticker, excluded.instrument_type, excluded.api_trade_available_flag, excluded.otc_flag, excluded.exchange, excluded.lot, excluded.currency, excluded.real_exchange)";


    private final String GET_FIGI_BY_INSTRUMENT_TYPE = "select figi from instruments where instrument_type = :instrumentType";

    private final String GET_ALL = "select figi from instruments";


    @Override
    public void addInstrument(Instrument instrument) {
        jdbcTemplate.update(INSERT_SQL, new BeanPropertySqlParameterSource(instrument));
    }

    @Override
    public void addInstruments(List<Instrument> figis) {
        for (Instrument figi : figis) {
            addInstrument(figi);
        }
    }


    @Override
    public List<String> figiByInstrumentType(String instrumentType) {
        return jdbcTemplate.query(GET_FIGI_BY_INSTRUMENT_TYPE, Map.of("instrumentType", instrumentType), (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public List<String> findAll() {
        return jdbcTemplate.query(GET_ALL, (rs, rowNum) -> rs.getString(1));
    }
}
