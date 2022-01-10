package ru.tinkoff.piapi.robot.utils;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateUtils {
    public static OffsetDateTime timestampToDate(Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos())
                .atZone(ZoneOffset.UTC)
                .toOffsetDateTime();
    }
}
