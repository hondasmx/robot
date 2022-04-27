package ru.tinkoff.piapi.robot.db.entities;

import lombok.Data;

@Data
public class Instrument {

    private String tradingStatus;

    private String figi;

    private String isin = "";

    private String classCode;

    private String ticker;

    private String instrumentType;

    private Boolean apiTradeAvailableFlag;

    private Boolean otcFlag;

    private String exchange;

    private Integer lot;

    private String currency;

    private String realExchange;
}
