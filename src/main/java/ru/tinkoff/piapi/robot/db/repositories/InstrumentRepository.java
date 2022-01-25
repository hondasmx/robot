package ru.tinkoff.piapi.robot.db.repositories;

import ru.tinkoff.piapi.robot.db.entities.Instrument;

import java.util.List;

public interface InstrumentRepository {

    void addInstrument(Instrument figi);

    void addInstruments(List<Instrument> figis);

    List<String> figiByTradingStatus(String tradingStatus);

    List<String> findAll();

    void updateInstrument(String figi, String tradingStatus);

    void updateMDTradingStatus(String figi, String tradingStatus);

    List<Instrument> getBaseUnspecifiedInstruments();

    List<String> getExchanges();
}
