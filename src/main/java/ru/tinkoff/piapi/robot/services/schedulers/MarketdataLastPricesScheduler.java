package ru.tinkoff.piapi.robot.services.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.robot.db.repositories.LastPricesRepository;
import ru.tinkoff.piapi.robot.grpc.SdkService;
import ru.tinkoff.piapi.robot.grpc.marketdata.GrpcPublicMarketdataService;
import ru.tinkoff.piapi.robot.services.StreamService;
import ru.tinkoff.piapi.robot.services.TelegramService;
import ru.tinkoff.piapi.robot.utils.MoneyUtils;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static ru.tinkoff.piapi.robot.utils.DateUtils.secondsToString;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketdataLastPricesScheduler {

    private static final BigDecimal DEFAULT_DIFF_PERCENT = BigDecimal.valueOf(60);
    private static final Map<String, Quotation> result = new HashMap<>();
    private final SdkService sdkService;
    private final StreamService streamService;
    private final TelegramService telegramService;
    private final LastPricesRepository lastPricesRepository;

    /**
     * Проверяем, last_price скачет не более, чем на 60%
     */
    @Scheduled(fixedRate = 1000 * 60)
    public void lastPriceCheck() {
        log.debug("job started: lastPriceCheck");
        var lastPrices = sdkService.getInvestApi().getMarketDataService().getLastPricesSync(List.of());
        for (LastPrice lastPrice : lastPrices) {
            lastPricesRepository.addLastPrice(lastPrice);
            var figi = lastPrice.getFigi();
            var normalTradingFigi = new HashSet<>(streamService.normalTradingFigi);
            if (!normalTradingFigi.contains(figi)) {
                continue;
            }
            var currentPrice = lastPrice.getPrice();
            var currentPriceBd = MoneyUtils.quotationToBigDecimal(currentPrice);

            if (result.containsKey(figi)) {
                var prevPrice = result.get(figi);
                if (MoneyUtils.quotationDiffPercent(currentPrice, prevPrice).compareTo(DEFAULT_DIFF_PERCENT) >= 0) {
                    var prevPriceBd = MoneyUtils.quotationToBigDecimal(prevPrice);
                    log.error("price with 60% diff for 3 seconds. figi {}. current price {}, previous price {}", figi, currentPriceBd, prevPriceBd);
                    var telegramMessage = MessageFormat.format("*price with 60% diff for 3 seconds* \n\n figi: {0}\n current price: {1}\n previous price: {2}\n", figi, currentPriceBd, prevPriceBd);
                    telegramService.sendMessage(telegramMessage);
                }
            }
            result.put(figi, currentPrice);
        }
    }
}
