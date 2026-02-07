package com.posts.common.auth.annotation;

import java.lang.annotation.*;

/*
Annotations are a declarative way to specify join points. They make code cleaner and more understandable.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAuth {
    String[] roles() default {};
}
