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
    private final StreamService streamService;

    @GetMapping("/instruments")
    public ResponseEntity<InitSchedulerResponse> initInstruments() {
        instrumentService.initInstruments();
        return ResponseEntity.ok(InitSchedulerResponse.of("ok"));
    }

    @GetMapping("/md_status")
    public ResponseEntity<InitSchedulerResponse> mdStatus() {
        instrumentService.updateMDTradingStatus();
        return ResponseEntity.ok(InitSchedulerResponse.of("ok"));
    }

    @GetMapping("/md_streams")
    public ResponseEntity<InitSchedulerResponse> initOtherStreams() {
        streamService.initMDStreams();
        return ResponseEntity.ok(InitSchedulerResponse.of("ok"));
    }

    @GetMapping("/info_stream")
    public ResponseEntity<InitSchedulerResponse> initInfoStream() {
        streamService.initInfoStream();
        return ResponseEntity.ok(InitSchedulerResponse.of("ok"));
    }
}
