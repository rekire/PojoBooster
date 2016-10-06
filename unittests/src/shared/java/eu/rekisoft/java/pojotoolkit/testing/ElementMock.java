package eu.rekisoft.java.pojotoolkit.testing;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created on 19.09.2016.
 *
 * @author René Kilczan
 */
public class ElementMock implements TypeElement, ExecutableElement {
    private final String qualifiedName;
    private final ElementKind kind;
    private final List<AnnotationMirror> annotations;
    private final TypeMirrorMock type;
    private final List<TypeMirror> parameters;

    public ElementMock(String qualifiedName, ElementKind kind, TypeKind type) {
        this.qualifiedName = qualifiedName;
        this.kind = kind;
        this.type = new TypeMirrorMock(qualifiedName, type);
        this.annotations = new ArrayList<>();
        this.parameters = new ArrayList<>();
    }

    public ElementMock(String qualifiedName, ElementKind kind, TypeKind type, AnnotationMirror... annotations) {
        this(qualifiedName, kind, type);
        Collections.addAll(this.annotations, annotations);
    }

    public ElementMock(String qualifiedName, ElementKind kind, TypeKind type, TypeMirror... parameters) {
        this(qualifiedName, kind, type);
        Collections.addAll(this.parameters, parameters);
        //when(this.type.getParameterTypes()).thenReturn(this.parameters);
        //getParameterTypes
    }

    @Override
    public TypeMirror asType() {
        return type;
    }

    @Override
    public ElementKind getKind() {
        return kind;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return annotations;
    }

    @Override
    public Name getQualifiedName() {
        Name name = mock(Name.class);
        when(name.toString()).thenReturn(qualifiedName);
        return name;
    }

    @Override
    public Name getSimpleName() {
        Name name = mock(Name.class);
        when(name.toString()).thenReturn(qualifiedName.substring(Math.max(qualifiedName.lastIndexOf("$"), qualifiedName.lastIndexOf(".")) + 1));
        return name;
    }

    @Override
    public String toString() {
        return getQualifiedName().toString();
    }

    // nothing interesting from here

    @Override
    public Set<Modifier> getModifiers() {
        return new HashSet<>(0);
    }

    @Override
    public TypeMirror getSuperclass() {
        return null;
    }

    @Override
    public List<? extends TypeMirror> getInterfaces() {
        return null;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        return null;
    }

    @Override
    public TypeMirror getReturnType() {
        return new TypeMirrorMock(String.class);
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        return null;
    }

    @Override
    public TypeMirror getReceiverType() {
        return null;
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
        return null;
    }

    @Override
    public AnnotationValue getDefaultValue() {
        return null;
    }

    @Override
    public Element getEnclosingElement() {
        return null;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return null;
    }

    @Override
    public NestingKind getNestingKind() {
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
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return null;
    }
}