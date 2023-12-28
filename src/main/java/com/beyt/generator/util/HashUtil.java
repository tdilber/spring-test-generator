package com.beyt.generator.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;

/**
 * Created by tdilber at 14-Sep-19
 */
public class HashUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getHash(Object... values) {
        Assert.notNull(values, "Values cannot be null");
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            try {
                sb.append(objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                sb.append(value.toString());
            }
        }
        return String.valueOf(sb.toString().hashCode());
    }
}
