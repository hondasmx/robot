package ru.tinkoff.piapi.robot.db.repositories;

import ru.tinkoff.piapi.robot.db.entities.Instrument;

import java.util.List;

public interface InstrumentRepository {

    void addInstrument(Instrument figi);

    void addInstruments(List<Instrument> figis);

    List<String> figiByInstrumentType(String instrumentType);

    List<String> findAll();

    List<String> getExchanges();
}
