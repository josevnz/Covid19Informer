package com.kodegeek.covid19.towndata;

import java.lang.annotation.*;

/**
 * Container for the Version annotation
 * @author josevnz
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface Versions {
    @SuppressWarnings("unused")
    Version [] value();
}
