//package com.posts.post.infrastructure.config;
//
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.util.List;
//
//@ControllerAdvice
//public class PageableConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
//        resolver.setMaxPageSize(100);
//        resolver.setFallbackPageable(PageRequest.of(0, 10));
//        resolvers.add(resolver);
//    }
//}
