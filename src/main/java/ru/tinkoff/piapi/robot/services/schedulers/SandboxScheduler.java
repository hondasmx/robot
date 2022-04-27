package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.robot.grpc.sandbox.GrpcPublicSandboxService;
import ru.tinkoff.piapi.robot.services.StreamService;

import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class SandboxScheduler {

    private final StreamService streamService;
    private final GrpcPublicSandboxService sandboxService;

//    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 1000 * 60 * 5)
    public void postSandboxOrder() {
        log.debug("job started: failedOrderbookScheduler");
        var figiOptional = new HashSet<>(streamService.normalTradingFigi).stream().limit(1).findFirst();
        if (figiOptional.isEmpty()) {
            log.info("there is no instruments in normal_trading status");
            return;
        }

        var figi = figiOptional.get();
        sandboxService.postOrder(figi);
        log.debug("job finished: failedOrderbookScheduler");
    }

}
