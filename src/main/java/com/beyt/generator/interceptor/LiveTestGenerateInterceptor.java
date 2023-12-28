//package com.beyt.generator.interceptor;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.util.Pair;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
//import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
//
//import javax.annotation.PostConstruct;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.lang.reflect.Method;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class LiveTestGenerateInterceptor extends HandlerInterceptorAdapter {
//
//    @Autowired
//    private RequestMappingHandlerMapping requestMappingHandlerMapping;
//    Map<Method, Pair<RequestMappingInfo, HandlerMethod>> methodPairMap = null;
//
//    @PostConstruct
//    public void init() {
//        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
//        methodPairMap = requestMappingInfoHandlerMethodMap.entrySet().stream().collect(Collectors.toMap(e -> e.getValue().getMethod(), e -> Pair.of(e.getKey(), e.getValue())));
//        log.info("LiveTestGenerateInterceptor initialized!");
//    }
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        HandlerMethod hm = (HandlerMethod) handler;
//
//        Method method = hm.getMethod();
//        if (method == null) {
//            return true;
//        }
//        Pair<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodPair = methodPairMap.get(method);
//        if (shouldNotHandle(handler)) {
//            return true;
//        }
//
//        return true;
//    }
//
//    private boolean shouldNotHandle(Object handler) {
//        if (handler instanceof HandlerMethod) {
//            return false;
//        }
//        return true;
//    }
//
//}
