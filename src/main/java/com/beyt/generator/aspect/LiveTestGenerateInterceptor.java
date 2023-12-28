package com.beyt.generator.aspect;

import com.beyt.generator.generation.IntegrationTestGenerateManager;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

public class LiveTestGenerateInterceptor implements HandlerInterceptor {

    @Autowired
    private IntegrationTestGenerateManager integrationTestMethodGenerator;

    @Autowired
    private LiveTestGenerateContext liveTestGenerateContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            handlerMethod.getMethodParameters();
            Method method = handlerMethod.getMethod();
            if (Objects.nonNull(liveTestGenerateContext) && BooleanUtils.isTrue(liveTestGenerateContext.getTestGenerating())) {
                integrationTestMethodGenerator.appendLiveTest(request, response, liveTestGenerateContext.getMethod(), liveTestGenerateContext.getReturnValue(), liveTestGenerateContext.getParameterArgs());
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
