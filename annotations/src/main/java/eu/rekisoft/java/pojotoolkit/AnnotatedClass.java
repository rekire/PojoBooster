package eu.rekisoft.java.pojotoolkit;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashSet;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import eu.rekisoft.java.pojobooster.Field;

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
    public final TypeElement type;
    public final HashSet<TypeName> interfaces;

    public AnnotatedClass(List<Class<?>> extensions, ClassName targetType, ClassName sourceType, List<Member> members, TypeElement type) {
        this.extensions = (List<Class<? extends Extension>>)(Object)extensions;
        this.targetType = targetType;
        this.sourceType = sourceType;
        this.members = members;
        this.type = type;
        this.interfaces = new HashSet<>();
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