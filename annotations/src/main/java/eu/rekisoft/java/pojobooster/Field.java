package eu.rekisoft.java.pojobooster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rene on 26.05.2016.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Field {
    String value() default "";
    //Behavior serialize() default Behavior.Default;
    //Behavior deserialize() default Behavior.Default;
    //Behavior getter() default Behavior.Default;
    //Behavior setter() default Behavior.Default;
}