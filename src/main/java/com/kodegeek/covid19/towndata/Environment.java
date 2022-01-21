package com.kodegeek.covid19.towndata;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Environment {
    String value();
}
