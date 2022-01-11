package ru.tinkoff.piapi.robot.services;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.robot.db.entities.Instrument;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.grpc.instruments.GrpcPublicInstrumentService;
import ru.tinkoff.piapi.robot.grpc.marketdata.GrpcPublicMarketdataService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentService {

    private final GrpcPublicInstrumentService publicInstrumentService;
    private final GrpcPublicMarketdataService publicMarketdataService;
    private final InstrumentRepository instrumentRepository;
    private final StreamService streamService;

    @EventListener(ApplicationReadyEvent.class)
    public void initInstruments() {
        for (InstrumentStatus instrumentStatus : List.of(InstrumentStatus.INSTRUMENT_STATUS_ALL, InstrumentStatus.INSTRUMENT_STATUS_BASE)) {
            getEtfs(instrumentStatus);
            getBonds(instrumentStatus);
            getCurrencies(instrumentStatus);
            getFutures(instrumentStatus);
            getShares(instrumentStatus);
        }
        streamService.collectFigi();
    }


    public void updateMDTradingStatus() {
        var figiList = instrumentRepository.findAll();
        log.info("updating MD trading status for {} figi", figiList.size());
        for (String figi : figiList) {
            var tradingStatus = "INSTRUMENT_NOT_FOUND_IN_MARKETDATA";
            try {
                tradingStatus = publicMarketdataService.getTradingStatus(figi).name();
            } catch (StatusRuntimeException exception) {
                log.error("error getting MD trading status for figi {}", figi);
            }
            instrumentRepository.updateMDTradingStatus(figi, tradingStatus);
        }
    }

    private void getEtfs(InstrumentStatus instrumentStatus) {
        var instruments = publicInstrumentService
                .getEtfs(instrumentStatus)
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin(el.getIsin())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("etf")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .tradingStatus(el.getTradingStatus().name())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }

    private void getFutures(InstrumentStatus instrumentStatus) {
        var instruments = publicInstrumentService
                .getFutures(instrumentStatus)
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin("")
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("future")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .tradingStatus(el.getTradingStatus().name())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }

    private void getShares(InstrumentStatus instrumentStatus) {
        var instruments = publicInstrumentService
                .getShares(instrumentStatus)
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin(el.getIsin())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("share")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .tradingStatus(el.getTradingStatus().name())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }

    private void getCurrencies(InstrumentStatus instrumentStatus) {
        var instruments = publicInstrumentService
                .getCurrencies(instrumentStatus)
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin(el.getIsin())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("currency")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .tradingStatus(el.getTradingStatus().name())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }

    private void getBonds(InstrumentStatus instrumentStatus) {
        var instruments = publicInstrumentService
                .getBonds(instrumentStatus)
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin(el.getIsin())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("bond")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .tradingStatus(el.getTradingStatus().name())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }
}
