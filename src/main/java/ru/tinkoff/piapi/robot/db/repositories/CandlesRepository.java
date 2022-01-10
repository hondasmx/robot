package ru.tinkoff.piapi.robot.db.repositories;

import com.google.protobuf.Timestamp;
import ru.tinkoff.piapi.robot.db.entities.Instrument;

import java.util.List;

public interface CandlesRepository {

    void addCandle(String figi, Timestamp timestamp);

}
