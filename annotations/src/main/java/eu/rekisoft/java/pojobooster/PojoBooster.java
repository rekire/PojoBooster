package eu.rekisoft.java.pojobooster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.cert.Extension;

/**
 * Created on 26.05.2016.
 *
 * @author René Kilczan
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PojoBooster {
    Class<? extends Extension>[] value();
    boolean getter() default true;
    boolean setter() default true;
}