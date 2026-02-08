package com.posts.common.auth.aspect;

import lombok.experimental.UtilityClass;

/*
A utility class in Java is a specialized class that acts as a container for static methods and fields that provide general-purpose,
reusable functionality across an application. These classes are typically stateless,
meaning they do not maintain any internal data that changes during execution, and cannot be instantiated
 */
@UtilityClass
public class AuthContext {

    private final ThreadLocal<AuthVerifyResponse> CURRENT_USER = new ThreadLocal<>();
}
