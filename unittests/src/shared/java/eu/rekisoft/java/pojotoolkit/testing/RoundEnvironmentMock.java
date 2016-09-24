package eu.rekisoft.java.pojotoolkit.testing;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Created on 19.09.2016.
 *
 * @author René Kilczan
 */
public class RoundEnvironmentMock implements RoundEnvironment {
    public boolean processingOver = false;
    public boolean errorRaised = false;
    public final Set<? extends Element> roots = new HashSet<>();
    public final HashMap<String, Set<? extends Element>> annotatedElements = new HashMap<>();

    @Override
    public boolean processingOver() {
        return processingOver;
    }

    @Override
    public boolean errorRaised() {
        return errorRaised;
    }

    @Override
    public Set<? extends Element> getRootElements() {
        return roots;
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
        Set<? extends Element> set = annotatedElements.get(a.getQualifiedName().toString());
        if(set == null) {
            set = new HashSet<>(0);
        }
        return set;
    }

    @Override
    public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
        Set<? extends Element> set = annotatedElements.get(a.getName());
        if(set == null) {
            set = new HashSet<>(0);
        }
        return set;
    }
}