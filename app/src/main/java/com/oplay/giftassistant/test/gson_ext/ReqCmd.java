package com.oplay.giftassistant.test.gson_ext;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by zsigui on 15-12-24.
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface ReqCmd {
	int value() default 0x10;
}
