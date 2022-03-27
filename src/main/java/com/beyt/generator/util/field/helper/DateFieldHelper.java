package com.beyt.generator.util.field.helper;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tdilber at 11/17/2020
 */
@Slf4j
public class DateFieldHelper implements IFieldHelper<Date> {
    @Override
    public Date fillRandom() {
        return new Date(System.currentTimeMillis() + random.nextInt(1000000000));
    }

    @Override
    public Date fillValue(String value) {
        DateFormat dateFormat = new SimpleDateFormat();
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
                return sdf.parse(value);
            } catch (ParseException ex) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    @Override
    public String createGeneratorCode(String value) {
        return "new Date(" + fillValue(value).getTime() + "L)";
    }
}
