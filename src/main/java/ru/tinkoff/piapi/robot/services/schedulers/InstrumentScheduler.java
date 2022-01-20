package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.db.entities.Instrument;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;
import ru.tinkoff.piapi.robot.services.TelegramService;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentScheduler {

    private final InstrumentRepository instrumentRepository;
    private final TelegramService telegramService;


    @Scheduled(cron = "0 22 22 * * *")
    public void baseUnspecifiedInstruments() {
        log.info("job started: baseUnspecifiedInstruments");
        var baseUnspecifiedInstruments = instrumentRepository.getBaseUnspecifiedInstruments();
        if (baseUnspecifiedInstruments.size() > 0) {
            var figi = baseUnspecifiedInstruments.stream().map(Instrument::getFigi).collect(Collectors.toList());
            if (figi.size() > 0) {
                var shortList = figi.subList(0, Math.min(figi.size(), 5));
                var message = "*Figi в Base списке с unspecified статусом* \n\n";
                message += shortList.toString();
                telegramService.sendMessage(message);
            }
        }
    }
}
