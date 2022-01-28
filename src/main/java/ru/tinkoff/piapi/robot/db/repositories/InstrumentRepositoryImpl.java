package ru.tinkoff.piapi.robot.db.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.piapi.robot.db.entities.Instrument;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InstrumentRepositoryImpl implements InstrumentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // insert or replace
    private final String INSERT_SQL = "insert into instruments (figi, isin, class_code, ticker, instrument_type, api_trade_flag, otc_flag, instrument_status, exchange, lot, currency) values (:figi, :isin, :classCode, :ticker, :instrumentType, :apiTradeFlag, :otcFlag, :instrumentStatus, :exchange, :lot, :currency)" +
            "ON CONFLICT (figi) DO UPDATE SET ( isin, class_code, ticker, instrument_type, api_trade_flag, otc_flag, instrument_status, exchange, lot, currency) = (excluded.isin, excluded.class_code, excluded.ticker, excluded.instrument_type, excluded.api_trade_flag, excluded.otc_flag, excluded.instrument_status, excluded.exchange, excluded.lot, excluded.currency)";


    private final String GET_FIGI_BY_INSTRUMENT_TYPE = "select figi from instruments where instrument_type = :instrumentType";

    private final String GET_ALL = "select figi from instruments";

    private final String GET_EXCHANGES = "select distinct exchange from instruments where trading_status = 'SECURITY_TRADING_STATUS_NORMAL_TRADING'";

    private final String GET_BASE_UNSPECIFIED_INSTRUMENTS = "select *\n" +
            "from instruments\n" +
            "where trading_status = 'SECURITY_TRADING_STATUS_UNSPECIFIED'\n" +
            "  and instrument_status = 'INSTRUMENT_STATUS_BASE'";

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

    @Override
    public List<Instrument> getBaseUnspecifiedInstruments() {
        return jdbcTemplate.query(GET_BASE_UNSPECIFIED_INSTRUMENTS, new BeanPropertyRowMapper<>(Instrument.class));
    }

    @Override
    public List<String> getExchanges() {
        return jdbcTemplate.query(GET_EXCHANGES, (rs, rowNum) -> rs.getString(1));
    }
}
