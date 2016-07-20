package eu.rekisoft.java.pojobooster;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import eu.rekisoft.java.pojotoolkit.Extension;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public class Serializer extends Extension {
    private long hash;

    public Serializer(TypeName className) {
        super(className);
    }

    @Override
    public List<TypeName> getAttentionalInterfaces() {
        return Collections.singletonList(TypeName.get(Serializable.class));
    }

    @Override
    public List<MethodSpec> generateCode(String target, RoundEnvironment environment) {
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
        return new ArrayList<>(0);
    }

    @Override
    public List<FieldSpec> generateMembers() {
        return Collections.singletonList(
                FieldSpec.builder(TypeName.get(long.class), "serialVersionUID")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(hash + "L")
                        .build());
    }
}