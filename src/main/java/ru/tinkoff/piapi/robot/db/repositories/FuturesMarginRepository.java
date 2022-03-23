package ru.tinkoff.piapi.robot.db.repositories;

import ru.tinkoff.piapi.contract.v1.GetFuturesMarginResponse;

public interface FuturesMarginRepository {

    void addFutureMargin(String figi, GetFuturesMarginResponse futuresMarginResponse);

}
