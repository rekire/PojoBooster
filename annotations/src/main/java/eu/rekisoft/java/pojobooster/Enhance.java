package eu.rekisoft.java.pojobooster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.cert.*;

import eu.rekisoft.java.pojotoolkit.*;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
@ReflectiveAnnotation
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Enhance {
    Class<? extends eu.rekisoft.java.pojotoolkit.Extension>[] extensions();
    String name();
}