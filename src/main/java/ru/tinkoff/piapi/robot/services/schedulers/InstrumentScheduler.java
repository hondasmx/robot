package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.db.entities.Instrument;
import ru.tinkoff.piapi.robot.db.repositories.InstrumentRepository;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentScheduler {

    private final InstrumentRepository instrumentRepository;

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void baseUnspecifiedInstruments() {
        log.info("job started: baseUnspecifiedInstruments");
        var baseUnspecifiedInstruments = instrumentRepository.getBaseUnspecifiedInstruments();
        if (baseUnspecifiedInstruments.size() > 0) {
            var figi = baseUnspecifiedInstruments.stream().map(Instrument::getFigi).collect(Collectors.toList());
//            log.error(Markers.appendRaw(REQUEST_BODY_ARGUMENT, GrpcUtils.requestToString(request))
//                    .and(Markers.append(GRPC_STATUS_VALUE, statusValue), "error dsfkhj {}", value("key", figi));
        }
    }
}
