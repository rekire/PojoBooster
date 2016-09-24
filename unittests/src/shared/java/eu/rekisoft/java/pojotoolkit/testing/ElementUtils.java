package eu.rekisoft.java.pojotoolkit.testing;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

/**
 * Created on 18.09.2016.
 *
 * @author René Kilczan
 */
public class ElementUtils implements Elements {
    public ArrayList<Element> members = new ArrayList<>();

    public ElementUtils() {
        members.add(new ElementMock("java.lang.String", ElementKind.FIELD, TypeKind.DECLARED));
        members.add(new ElementMock("java.lang.String", ElementKind.METHOD, TypeKind.EXECUTABLE));
    }

    @Override
    public List<? extends Element> getAllMembers(TypeElement type) {
        return members;
    }

    @Override
    public PackageElement getPackageElement(CharSequence name) {
        return null;
    }

    @Override
    public TypeElement getTypeElement(CharSequence name) {
        return null;
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) {
        return null;
    }

    @Override
    public String getDocComment(Element e) {
        return null;
    }

    @Override
    public boolean isDeprecated(Element e) {
        return false;
    }

    @Override
    public Name getBinaryName(TypeElement type) {
        return null;
    }

    @Override
    public PackageElement getPackageOf(Element type) {
        return null;
    }

    @Override
    public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
        return null;
    }

    @Override
    public boolean hides(Element hider, Element hidden) {
        return false;
    }

    @Override
    public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
        return false;
    }

    @Override
    public String getConstantExpression(Object value) {
        return null;
    }

    @Override
    public void printElements(Writer w, Element... elements) {

    }

    @Override
    public Name getName(CharSequence cs) {
        return null;
    }

    @Override
    public boolean isFunctionalInterface(TypeElement type) {
        return false;
    }
}