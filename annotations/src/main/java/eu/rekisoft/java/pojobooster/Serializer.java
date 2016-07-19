package eu.rekisoft.java.pojobooster;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

import eu.rekisoft.java.pojotoolkit.Extension;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public class Serializer extends Extension {
    private long hash;

    public Serializer(String className) {
        super(className);
    }

    @Override
    public List<Class<?>> getAttentionalInterfaces() {
        return Collections.singletonList(Serializable.class);
    }

    @Override
    public List<Class<?>> getAttentionalImports() {
        return Collections.singletonList(Serializable.class);
    }

    @Override
    public String generateCode(String target, RoundEnvironment environment) {
        System.out.println("Serializer processes " + target + "...");
        String hashInfo = target;
        for(Element elem : environment.getElementsAnnotatedWith(eu.rekisoft.java.pojotoolkit.Field.class)) {
            if(elem.getEnclosingElement().asType().toString().equals(target)) {
                eu.rekisoft.java.pojotoolkit.Field field = elem.getAnnotation(eu.rekisoft.java.pojotoolkit.Field.class);
                String message = "Field annotation found in " + elem.getSimpleName()
                        + " with " + elem.getSimpleName() + " -> " + field.value();
                //System.out.println(elem.getEnclosingElement().asType() + " - " + elem.asType() + " " + elem.getSimpleName());
                hashInfo += "\n" + elem.asType() + " " + elem.getSimpleName();
                //classes.get(typeElement).add(elem);
                //String message = "Field annotation found in " + elem.getSimpleName()
                //        + " with " + elem.getSimpleName() + " -> " + field.value();
                //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
            }
        }
        hash = hashInfo.hashCode();
        return null;
    }

    @Override
    public String generateMembers() {
        return "private static final long serialVersionUID = " + hash + "L;";
    }
}
