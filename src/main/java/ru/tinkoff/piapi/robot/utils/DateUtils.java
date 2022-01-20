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

    public static String secondsToString(long totalSeconds) {
        var hours = totalSeconds / 3600;
        var minutes = (totalSeconds % 3600) / 60;
        var seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
