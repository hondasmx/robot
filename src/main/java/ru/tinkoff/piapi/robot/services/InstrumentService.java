package ru.tinkoff.piapi.robot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.grpc.SdkService;
import ru.tinkoff.piapi.robot.services.mapper.InstrumentMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final StreamService streamService;
    private final SdkService sdkService;
    private final InstrumentMapper instrumentMapper;

    private InstrumentsService getInstrumentService() {
        return sdkService.getInvestApi().getInstrumentsService();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initInstruments() {
        getEtfs();
        getBonds();
        getCurrencies();
        getFutures();
        getShares();
        streamService.collectFigi();
        streamService.initInfoStream();
        streamService.initMDStreams();
        streamService.initOrdersStream();
    }

    private void getEtfs() {
        var etfs = getInstrumentService().getAllEtfsSync();

        var instruments = instrumentMapper.mapEtf(etfs, "etf");
        instrumentRepository.addInstruments(instruments);
    }


    private void getFutures() {
        var futures = getInstrumentService().getAllFuturesSync();

        var instruments = instrumentMapper.mapFuture(futures, "fufures");
        instrumentRepository.addInstruments(instruments);
    }

    private void getShares() {
        var shares = getInstrumentService().getAllSharesSync();

        var instruments = instrumentMapper.mapShare(shares, "share");
        instrumentRepository.addInstruments(instruments);
    }

    private void getCurrencies() {
        var currencies = getInstrumentService().getAllCurrenciesSync();

        var instruments = instrumentMapper.mapCurrency(currencies, "currency");
        instrumentRepository.addInstruments(instruments);
    }

    private void getBonds() {
        var bonds = getInstrumentService().getAllBondsSync();

        var instruments = instrumentMapper.mapBond(bonds, "bond");
        instrumentRepository.addInstruments(instruments);
    }
}
