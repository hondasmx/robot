package ru.tinkoff.piapi.robot.utils;

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
}
