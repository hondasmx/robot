package ru.tinkoff.piapi.robot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.Currency;
import ru.tinkoff.piapi.contract.v1.Etf;
import ru.tinkoff.piapi.contract.v1.Future;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.robot.db.entities.Instrument;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.grpc.SdkService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final StreamService streamService;
    private final SdkService sdkService;


    private InstrumentsService getInstrumentService() {
        return sdkService.getInvestApi().getInstrumentsService();
    }

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
        streamService.initInfoStream();
        streamService.initMDStreams();
        streamService.initOrdersStream();
    }

    private void getEtfs(InstrumentStatus instrumentStatus) {
        List<Etf> etfs;
        if (instrumentStatus == InstrumentStatus.INSTRUMENT_STATUS_ALL) {
            etfs = getInstrumentService().getAllEtfsSync();
        } else {
            etfs = getInstrumentService().getTradableEtfsSync();
        }
        var instruments = etfs
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin(el.getIsin())
                        .classCode(el.getClassCode())
                        .ticker(el.getTicker())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("etf")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .exchange(el.getExchange())
                        .lot(el.getLot())
                        .currency(el.getCurrency())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }


    private void getFutures(InstrumentStatus instrumentStatus) {
        List<Future> futures;
        if (instrumentStatus == InstrumentStatus.INSTRUMENT_STATUS_ALL) {
            futures = getInstrumentService().getAllFuturesSync();
        } else {
            futures = getInstrumentService().getTradableFuturesSync();
        }
        var instruments = futures
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin("")
                        .classCode(el.getClassCode())
                        .ticker(el.getTicker())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("futures")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .exchange(el.getExchange())
                        .lot(el.getLot())
                        .currency(el.getCurrency())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }

    private void getShares(InstrumentStatus instrumentStatus) {
        List<Share> futures;
        if (instrumentStatus == InstrumentStatus.INSTRUMENT_STATUS_ALL) {
            futures = getInstrumentService().getAllSharesSync();
        } else {
            futures = getInstrumentService().getTradableSharesSync();
        }
        var instruments = futures
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin(el.getIsin())
                        .classCode(el.getClassCode())
                        .ticker(el.getTicker())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("share")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .exchange(el.getExchange())
                        .lot(el.getLot())
                        .currency(el.getCurrency())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }

    private void getCurrencies(InstrumentStatus instrumentStatus) {
        List<Currency> currencies;
        if (instrumentStatus == InstrumentStatus.INSTRUMENT_STATUS_ALL) {
            currencies = getInstrumentService().getAllCurrenciesSync();
        } else {
            currencies = getInstrumentService().getTradableCurrenciesSync();
        }
        var instruments = currencies
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin(el.getIsin())
                        .classCode(el.getClassCode())
                        .ticker(el.getTicker())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("currency")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .exchange(el.getExchange())
                        .lot(el.getLot())
                        .currency(el.getCurrency())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }

    private void getBonds(InstrumentStatus instrumentStatus) {
        List<Bond> bonds;
        if (instrumentStatus == InstrumentStatus.INSTRUMENT_STATUS_ALL) {
            bonds = getInstrumentService().getAllBondsSync();
        } else {
            bonds = getInstrumentService().getTradableBondsSync();
        }
        var instruments = bonds
                .stream()
                .map(el -> Instrument.builder()
                        .figi(el.getFigi())
                        .isin(el.getIsin())
                        .classCode(el.getClassCode())
                        .ticker(el.getTicker())
                        .instrumentStatus(instrumentStatus.name())
                        .instrumentType("bond")
                        .apiTradeFlag(el.getApiTradeAvailableFlag())
                        .otcFlag(el.getOtcFlag())
                        .exchange(el.getExchange())
                        .lot(el.getLot())
                        .currency(el.getCurrency())
                        .build())
                .collect(Collectors.toList());
        instrumentRepository.addInstruments(instruments);
    }
}
