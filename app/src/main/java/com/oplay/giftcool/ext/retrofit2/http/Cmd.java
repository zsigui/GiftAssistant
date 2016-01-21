package com.oplay.giftcool.ext.retrofit2.http;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * It defines a command number when use a retrofit service
 *
 * Created by zsigui on 15-12-24.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Cmd {
	int value() default 0;
}
