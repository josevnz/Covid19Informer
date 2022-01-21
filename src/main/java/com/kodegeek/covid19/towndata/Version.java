package com.kodegeek.covid19.towndata;

import java.lang.annotation.*;

/**
 * Keep track of author and versions
 * @author josevnz
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Repeatable(Versions.class)
@Inherited
public @interface Version {
    int number() default 1;
    String author();
}
