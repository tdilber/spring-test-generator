package com.beyt.generator.util;

import org.springframework.util.Assert;

/**
 * Created by tdilber at 14-Sep-19
 */
public class StringUtil {
    public static String limitString(String text, int length) {
        if (text != null && text.length() > length) {
            text = text.substring(0, length);
        }
        return text;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String lowerFirstLetter(String simpleName) {
        Assert.hasText(simpleName, "must have text!");
        return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }
}
