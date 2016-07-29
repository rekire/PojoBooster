package eu.rekisoft.java.pojotoolkit;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * This class should hold meta information about the class which was annotated which are relevant
 * for custom Extensions.
 * <p>
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
    public final TypeName superType;

    // package local to avoid that "external" people initialize this class
    AnnotatedClass(List<Class<?>> extensions, ClassName targetType, ClassName sourceType, List<Element> fields, List<Element> methods, TypeElement type) {
        this.extensions = (List<Class<? extends Extension>>)(Object)extensions;
        this.targetType = targetType;
        this.sourceType = sourceType;
        this.members = convertFields(fields);
        this.type = type;
        // TODO
        // this.methods = methods; // this should be abstracted too
        this.interfaces = new HashSet<>();
        this.superType = null;
    }

    private List<Member> convertFields(List<Element> fields) {
        List<Member> list = new ArrayList<>(fields.size());
        for(Element field : fields) {
            Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(fields.size());
            for(AnnotationMirror annotationMirror : field.getAnnotationMirrors()) {
                annotations.put(annotationMirror.getAnnotationType().toString(), annotationMirror.getElementValues());
            }
            list.add(new Member(field, annotations));
        }
        return list;
    }

    public static class Member {
        private final Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations;
        public final Element element;
        public final TypeMirror type;
        public final String typeName;
        public final String name;

        public Member(Element element, Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations) {
            this.type = element.asType();
            this.typeName = type.toString();
            this.name = element.toString();
            this.element = element;
            this.annotations = annotations;
        }

        public Object getAnnotatedProperty(Class<? extends Annotation> type, String field) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> entries = annotations.get(type.getName());
            if(entries != null) {
                for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : entries.entrySet()) {
                    if(field.equals(entry.getKey().getSimpleName().toString())) {
                        return entry.getValue().getValue();
                    }
                }
            }
            return null;
        }
    }
}