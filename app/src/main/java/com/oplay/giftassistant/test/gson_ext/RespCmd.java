package com.oplay.giftassistant.test.gson_ext;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by zsigui on 15-12-24.
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface RespCmd {
	int value() default 0x10;
}
