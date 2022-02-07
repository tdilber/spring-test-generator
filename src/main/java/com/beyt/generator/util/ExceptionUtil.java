package com.beyt.generator.util;

import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by tdilber at 11/16/2020
 */
@Slf4j
public final class ExceptionUtil {
    private ExceptionUtil() {
    }

    public static String getPrintStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}
