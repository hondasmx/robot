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
    private final String INSERT_SQL = "insert into instruments (figi, isin, class_code, ticker, instrument_type, trading_status, api_trade_flag, otc_flag, instrument_status, exchange) values (:figi, :isin, :classCode, :ticker, :instrumentType, :tradingStatus, :apiTradeFlag, :otcFlag, :instrumentStatus, :exchange)" +
            "ON CONFLICT (figi) DO UPDATE SET updated_at = now(), trading_status = :tradingStatus, instrument_status = :instrumentStatus";

    private final String GET_FIGI = "select figi from instruments where trading_status = :tradingStatus";

    private final String GET_ALL = "select figi from instruments";

    private final String GET_EXCHANGES = "select distinct exchange from instruments where trading_status = 'SECURITY_TRADING_STATUS_NORMAL_TRADING'";

    private final String UPDATE = "UPDATE instruments SET updated_at = now(), trading_status = :tradingStatus where figi = :figi";

    private final String UPDATE_MD_TRADING_STATUS = "UPDATE instruments SET  trading_status_md = :tradingStatus where figi = :figi";

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
    public List<String> figiByTradingStatus(String tradingStatus) {
        return jdbcTemplate.query(GET_FIGI, Map.of("tradingStatus", tradingStatus), (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public List<String> findAll() {
        return jdbcTemplate.query(GET_ALL, (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public void updateInstrument(String figi, String tradingStatus) {
        jdbcTemplate.update(UPDATE, Map.of("tradingStatus", tradingStatus, "figi", figi));
    }

    @Override
    public void updateMDTradingStatus(String figi, String tradingStatus) {
        jdbcTemplate.update(UPDATE_MD_TRADING_STATUS, Map.of("tradingStatus", tradingStatus, "figi", figi));
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
