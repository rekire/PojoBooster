package eu.rekisoft.java.pojobooster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rene on 03.06.2016.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Field {
    String value() default "";
    boolean serialize() default true;
    boolean deserialize() default true;
}