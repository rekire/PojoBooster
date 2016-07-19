package eu.rekisoft.java.pojotoolkit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.cert.*;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Enhance {
    Class<? extends Extension>[] extensions();
    String name();
}