package com.beyt.generator.helper.creator;

import com.beyt.generator.manager.IntegrationTestGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by tdilber at 11/17/2020
 */
@Slf4j
public abstract class BaseMethodCreator implements IMethodCreator {
    protected final IntegrationTestGenerator integrationTestGenerator;

    protected BaseMethodCreator(IntegrationTestGenerator integrationTestGenerator) {
        this.integrationTestGenerator = integrationTestGenerator;
    }
}
