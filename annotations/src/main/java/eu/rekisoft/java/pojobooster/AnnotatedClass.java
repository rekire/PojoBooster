package eu.rekisoft.java.pojobooster;

import com.squareup.javapoet.ClassName;

import java.util.List;

import javax.lang.model.element.Element;

import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.Field;

/**
 * Created on 20.07.2016.
 *
 * @author René Kilczan
 */
public class AnnotatedClass {
    public final List<Class<? extends Extension>> extensions;
    public final ClassName targetType;
    public final ClassName sourceType;
    public final List<Member> members;

    public AnnotatedClass(List<Class<?>> extensions, ClassName targetType, ClassName sourceType, List<Member> members) {
        this.extensions = (List<Class<? extends Extension>>)(Object)extensions;
        this.targetType = targetType;
        this.sourceType = sourceType;
        this.members = members;
    }

    public static class Member {
        public final Field annotation;
        public final Element element;

        public Member(Field annotation, Element element) {
            this.annotation = annotation;
            this.element = element;
        }
    }
}