package com.deemons.modulerouter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author： deemons
 * date:    2017/4/27
 * desc:
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RouterService {
    String processName();
}
