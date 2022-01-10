package ru.tinkoff.piapi.robot.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.robot.db.entities.Instrument;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/instruments")
public class InstrumentsController {

    private final InstrumentRepository instrumentRepository;

    @GetMapping("/status/normal")
    public List<String> normalTrading() {
        return instrumentRepository.figiByTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING.name());
    }

    @GetMapping("/status/not_available")
    public List<String> notAvailableForTrading() {
        return instrumentRepository.figiByTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NOT_AVAILABLE_FOR_TRADING.name());
    }

    @GetMapping("/status/break_in_trading")
    public List<String> breakInTrading() {
        return instrumentRepository.figiByTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING.name());
    }

    @GetMapping("/base_unspecified")
    public List<Instrument> baseUnspecified() {
        return instrumentRepository.getBaseUnspecifiedInstruments();
    }
}
