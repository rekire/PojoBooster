package eu.rekisoft.java.pojotoolkit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rene on 03.06.2016.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface JSON {
    boolean read() default true;
    boolean write() default false;
    boolean hideNull() default false;
}
