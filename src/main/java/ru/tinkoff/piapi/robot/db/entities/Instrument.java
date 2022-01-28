package ru.tinkoff.piapi.robot.db.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Instrument {

    private String tradingStatus;

    private String figi;

    private String isin;

    private String classCode;

    private String ticker;

    private String instrumentType;

    private Boolean apiTradeFlag;

    private Boolean otcFlag;

    private String instrumentStatus;

    private String exchange;

    private Integer lot;

    private String currency;
}
