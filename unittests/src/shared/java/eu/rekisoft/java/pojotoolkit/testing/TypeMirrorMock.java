package eu.rekisoft.java.pojotoolkit.testing;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

/**
 * Created on 30.09.2016.
 *
 * @author René Kilczan
 */
public class TypeMirrorMock implements ExecutableType, ArrayType {
    private final TypeKind kind;
    private final TypeMirrorMock component;
    private final String name;

    public TypeMirrorMock(Class<?> type) {
        if(type.isArray()) {
            kind = TypeKind.ARRAY;
            component = new TypeMirrorMock(type.getComponentType());
        } else {
            kind = getKind(type);
            component = null;
        }
        name = type.getName();
    }

    private TypeKind getKind(Class<?> type) {
        if(boolean.class.equals(type)) {
            return TypeKind.BOOLEAN;
        } else if(byte.class.equals(type)) {
            return TypeKind.BYTE;
        } else if(short.class.equals(type)) {
            return TypeKind.SHORT;
        } else if(int.class.equals(type)) {
            return TypeKind.INT;
        } else if(long.class.equals(type)) {
            return TypeKind.LONG;
        } else if(char.class.equals(type)) {
            return TypeKind.CHAR;
        } else if(float.class.equals(type)) {
            return TypeKind.FLOAT;
        } else if(double.class.equals(type)) {
            return TypeKind.DOUBLE;
        } else {
            return TypeKind.DECLARED;
        }
    }

    public TypeMirrorMock(String name, TypeKind kind) {
        this.name = name;
        this.kind = kind;
        component = null;
    }

    public TypeMirrorMock(String name) {
        this(name, TypeKind.DECLARED);
    }

    @Override
    public TypeKind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public TypeMirror getComponentType() {
        return component;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return null;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return null;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return null;
    }

    @Override
    public List<? extends TypeVariable> getTypeVariables() {
        return null;
    }

    @Override
    public TypeMirror getReturnType() {
        return null;
    }

    @Override
    public List<? extends TypeMirror> getParameterTypes() {
        return new ArrayList<>(0);
    }

    @Override
    public TypeMirror getReceiverType() {
        return null;
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
        return null;
    }
}