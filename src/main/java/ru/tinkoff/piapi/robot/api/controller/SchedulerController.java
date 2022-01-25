package ru.tinkoff.piapi.robot.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tinkoff.piapi.robot.api.controller.dto.InitSchedulerResponse;
import ru.tinkoff.piapi.robot.services.InstrumentService;
import ru.tinkoff.piapi.robot.services.StreamService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scheduler")
public class SchedulerController {

    private final InstrumentService instrumentService;

    @GetMapping("/md_status")
    public ResponseEntity<InitSchedulerResponse> mdStatus() {
        instrumentService.updateMDTradingStatus();
        return ResponseEntity.ok(InitSchedulerResponse.of("ok"));
    }
}
