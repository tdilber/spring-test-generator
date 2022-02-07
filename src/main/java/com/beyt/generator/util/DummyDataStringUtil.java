package com.beyt.generator.util;

import com.beyt.generator.domain.enumeration.eVariableType;
import com.beyt.generator.exception.DummyDataFileReadException;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by tdilber at 11/2/2020
 */
@Slf4j
public final class DummyDataStringUtil {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    private static Long count = 0l;

    public static List<String> getDummyDataFileLineSplit(String line) {
        if (line == null) {
            throw new IllegalStateException("line field mustn't be null!");
        }

        if (line.contains("\"")) {
            List<String> result = new ArrayList<>();
            int lineCursor = 0;
            while (line.length() > lineCursor) {
                int endIndex = line.indexOf("\"", lineCursor);
                String substring;

                if (endIndex == -1) {
                    substring = line.substring(lineCursor);
                } else {
                    substring = line.substring(lineCursor, endIndex);
                }

                lineCursor = endIndex + 1;

                for (String word : substring.split(" ")) {
                    if (!StringUtil.isNullOrEmpty(word)) {
                        result.add(word);
                    }
                }

                endIndex = line.indexOf("\"", lineCursor);
                if (endIndex == -1) {
                    throw new DummyDataFileReadException("\" count not even!!");
                }

                substring = line.substring(lineCursor, endIndex);
                lineCursor = endIndex + 1;
                result.add(substring);

            }

            return result;
        } else {
            return Arrays.asList(line.split(" "));
        }
    }

    public static String processFieldValue(String fieldValue) {
        while (fieldValue.contains("{" + eVariableType.COUNT + "}") || fieldValue.contains("{" + eVariableType.NOW_SIMPLE_DATE + "}") || fieldValue.contains("{" + eVariableType.NOW_MILLISECOND + "}")) {
            if (fieldValue.contains("{" + eVariableType.COUNT + "}")) {
                fieldValue = fieldValue.replace("{" + eVariableType.COUNT + "}", (count++).toString());
            } else if (fieldValue.contains("{" + eVariableType.NOW_SIMPLE_DATE + "}")) {
                fieldValue = fieldValue.replace("{" + eVariableType.NOW_SIMPLE_DATE + "}", simpleDateFormat.format(new Date()));
            } else if (fieldValue.contains("{" + eVariableType.NOW_MILLISECOND + "}")) {
                fieldValue = fieldValue.replace("{" + eVariableType.NOW_MILLISECOND + "}", Long.toString(System.currentTimeMillis()));
            }
        }

        return fieldValue;
    }
}
