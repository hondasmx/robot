package ru.tinkoff.piapi.robot.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.core.InvestApi;

@Service
@Slf4j
@RequiredArgsConstructor
public class SdkService {
    @Value("${auth.token}")
    private String token;
    @Value("${auth.account}")
    private String accountId;

    private InvestApi investApi;


    public InvestApi getInvestApi() {
        if (investApi == null) {
            investApi = InvestApi.create(token);
        }
        return investApi;
    }
}
