package eu.rekisoft.java.pojotoolkit.testing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created on 19.09.2016.
 *
 * @author René Kilczan
 */
public class AnnotationMirrorMock implements AnnotationMirror {
    private String annotationClassName;
    private Object[] data;

    public AnnotationMirrorMock(Class<? extends Annotation> annotation, Object... annotationData) {
        if(annotationData.length % 2 != 0) {
            throw new IllegalArgumentException("Wrong count of args, expecting key value pairs.");
        }
        for(int i = 0; i < annotationData.length; i += 2) {
            if(!(annotationData[i] instanceof String)) {
                throw new IllegalArgumentException("Every even element (the key) has to be a String.");
            }
        }
        data = annotationData;
        annotationClassName = annotation.getName();
    }

    @Override
    public DeclaredType getAnnotationType() {
        DeclaredType declaredType = mock(DeclaredType.class);
        Element element = mock(Element.class);
        TypeMirror type = mock(TypeMirror.class);
        when(element.asType()).thenReturn(type);
        when(type.toString()).thenReturn(annotationClassName);
        when(declaredType.asElement()).thenReturn(element);
        return declaredType;
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
        Map<ExecutableElement, AnnotationValue> map = new HashMap<>(data.length / 2);

        for(int i = 0; i < data.length; i += 2) {
            String annotationKey = (String) data[i];
            Object annotationValue = data[i + 1];
            ExecutableElement key = mock(ExecutableElement.class);
            Name keyName = mock(Name.class);
            when(key.getSimpleName()).thenReturn(keyName);
            when(keyName.toString()).thenReturn(annotationKey);
            AnnotationValue value = mock(AnnotationValue.class);
            if(annotationValue.getClass().isArray()) {
                List<AnnotationValue> list = new ArrayList<>(Array.getLength(annotationValue));
                for(int j = 0; j < list.size(); j++) {
                    DeclaredType declaredType = mock(DeclaredType.class);
                    when(value.getValue()).thenReturn(declaredType);
                    when(declaredType.toString()).thenReturn(Array.get(annotationValue, j).toString());
                    list.add(value);
                }
                when(value.getValue()).thenReturn(list);
            } else {
                when(value.getValue()).thenReturn(annotationValue);
            }
            map.put(key, value);
        }
        return map;
    }
}