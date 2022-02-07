package com.beyt.generator.util.field.helper;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Created by tdilber at 11/17/2020
 */
@Slf4j
public class ZonedDateTimeFieldHelper implements IFieldHelper<ZonedDateTime> {
    @Override
    public ZonedDateTime fillRandom() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() + random.nextInt(1000000000)), ZoneId.systemDefault());
    }

    @Override
    public ZonedDateTime fillValue(String value) {
        return ZonedDateTime.parse(value);
    }

    @Override
    public String createGeneratorCode(String value) {
        return "ZonedDateTime.parse(\"" + value + "\")";
    }
}
