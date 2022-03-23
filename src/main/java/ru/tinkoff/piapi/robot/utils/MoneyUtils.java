package ru.tinkoff.piapi.robot.utils;

import ru.tinkoff.piapi.contract.v1.GetFuturesMarginResponse;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyUtils {

    public static BigDecimal quotationDiffPercent(Quotation q1, Quotation q2) {
        var q1bd = quotationToBigDecimal(q1); //100
        if (q1bd.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        var q2bd = quotationToBigDecimal(q2); //115
        var diff = q1bd.subtract(q2bd); //15
        return diff.divide(q1bd, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100)).abs();
    }

    public static BigDecimal quotationToBigDecimal(Quotation quotation) {
        return quotation.getUnits() == 0 && quotation.getNano() == 0 ?
                BigDecimal.ZERO :
                BigDecimal.valueOf(quotation.getUnits()).add(BigDecimal.valueOf(quotation.getNano(), 9));
    }

    public static BigDecimal futuresPrice(Quotation pricePoints, GetFuturesMarginResponse futuresMarginResponse) {
        var minPriceIncrement = quotationToBigDecimal(futuresMarginResponse.getMinPriceIncrement());
        var minPriceIncrementAmount = quotationToBigDecimal(futuresMarginResponse.getMinPriceIncrementAmount());
        return quotationToBigDecimal(pricePoints).multiply(minPriceIncrementAmount).divide(minPriceIncrement, RoundingMode.HALF_UP);
    }

    public static Quotation bigDecimalToQuotation(BigDecimal value) {
        return Quotation.newBuilder()
                .setUnits(getUnits(value))
                .setNano(getNano(value))
                .build();
    }

    public static MoneyValue bigDecimalToMoneyValue(BigDecimal value, String currency) {
        return MoneyValue.newBuilder()
                .setUnits(getUnits(value))
                .setNano(getNano(value))
                .setCurrency(toLowerCaseNullable(currency))
                .build();
    }

    public static long getUnits(BigDecimal value) {
        return value != null ? value.longValue() : 0;
    }

    public static int getNano(BigDecimal value) {
        return value != null ? value.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(1_000_000_000L)).intValue() : 0;
    }

    private static String toLowerCaseNullable(String value) {
        return value != null ? value.toLowerCase() : "";
    }

    public static MoneyValue bigDecimalToMoneyValue(BigDecimal value) {
        return bigDecimalToMoneyValue(value, null);
    }

    public static BigDecimal moneyValueToBigDecimal(MoneyValue value) {
        if (value == null) {
            return null;
        }
        return mapUnitsAndNanos(value.getUnits(), value.getNano());
    }

    public static BigDecimal mapUnitsAndNanos(long units, int nanos) {
        if (units == 0 && nanos == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(units).add(BigDecimal.valueOf(nanos, 9));
    }
}
