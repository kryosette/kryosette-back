package com.posts.post.domain.resolver;

import com.posts.post.domain.annotations.ExtractAuthorizationToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthorizationTokenArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ExtractAuthorizationToken.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, // Information about the controller method argument.
                                  ModelAndViewContainer mavContainer, // Used to add attributes to the model
                                  NativeWebRequest webRequest, // The object that provides access to the HTTP request.
                                  WebDataBinderFactory binderFactory // It is used to associate data from a query with objects.
                                  ) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String authHeader = request.getHeader("Authorization");
        ExtractAuthorizationToken annotation = parameter.getParameterAnnotation(ExtractAuthorizationToken.class);

//        assert annotation != null; // not safe (java -ea MyClass  # -ea = enable assertions, It won't work without it, and it's not reliable.)
        if (annotation == null) {
            throw new IllegalStateException("Security annotation is missing!"); // safe
        }

        if (annotation.required() && (authHeader == null || !authHeader.startsWith("Bearer "))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return authHeader != null ? authHeader.replace("Bearer ", "") : null;

    }
}
