package com.beyt.generator;

import com.beyt.generator.helper.IntegrationTestMethodGenerator;
import com.beyt.generator.manager.IntegrationTestGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;

/**
 * Created by tdilber at 11/6/2020
 */
@Slf4j
@Component
@ComponentScan(excludeFilters = {@ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = {IntegrationTestGenerator.class, IntegrationTestMethodGenerator.class})
})
public class TestGeneratorExcluder {
}
