package com.beyt.generator.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Profile("integration-test-generator")
public class LiveTestGenerateAspect {

    @Autowired
    private LiveTestGenerateContext liveTestGenerateContext;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    // Endpoint türünü seçebilirsiniz (örneğin, @PostMapping)
    public Object restInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        liveTestGenerateContext.setTestGenerating(true);
        liveTestGenerateContext.setMethod(method);
        liveTestGenerateContext.setReturnValue(proceed);
        liveTestGenerateContext.setParameterArgs(joinPoint.getArgs());
//        integrationTestMethodGenerator.appendLiveTest(method, proceed, joinPoint.getArgs());
        return proceed;
    }
}
