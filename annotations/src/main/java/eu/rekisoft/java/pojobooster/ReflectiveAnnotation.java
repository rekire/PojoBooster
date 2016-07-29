package eu.rekisoft.java.pojobooster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The idea of this Annotation is to annotate annotations to make their values easier accessible for
 * custom Extensions. In the end you should be able to get "static" classes which hold the
 * information of the annotated annotation of the annotated POJO class you annotated. Maybe this is
 * brain fuck, but I think that is a brilliant idea when you understand my idea.
 *
 * Created on 29.07.2016.
 *
 * @author René Kilczan
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ReflectiveAnnotation {
}
