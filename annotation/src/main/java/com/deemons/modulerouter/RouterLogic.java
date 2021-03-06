package com.deemons.modulerouter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/4/27 17:53
 * 包名       com.app.annotation.superrouter
 * 描述
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RouterLogic {
    String processName();
    int Priority(); //优先级
}
