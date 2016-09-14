package eu.rekisoft.java.pojobooster;

import android.support.annotation.NonNull;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.ExtensionSettings;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public class Serializer extends Extension {
    private long hash;

    public Serializer(@NonNull ExtensionSettings settings) {
        super(settings);
        String hashInfo = annotatedClass.targetType.toString();
        for(AnnotatedClass.Member member : annotatedClass.members) {
            //Field field = member.annotation;
            //String message = "Field annotation found in " + elem.getSimpleName()
            //        + " with " + elem.getSimpleName() + " -> " + field.value();
            //System.out.println(elem.getEnclosingElement().asType() + " - " + elem.asType() + " " + elem.getSimpleName());
            hashInfo += "\n" + member.type + " " + member.typeName;
            //classes.get(typeElement).add(elem);
            //String message = "Field annotation found in " + elem.getSimpleName()
            //        + " with " + elem.getSimpleName() + " -> " + element.value();
            //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
        }
        hash = hashInfo.hashCode();
    }

    @NonNull
    @Override
    public List<TypeName> getAttentionalInterfaces() {
        return Collections.singletonList(TypeName.get(Serializable.class));
    }

    @NonNull
    @Override
    public List<MethodSpec> generateCode() {
        return new ArrayList<>(0);
    }

    @NonNull
    @Override
    public List<FieldSpec> generateMembers() {
        return Collections.singletonList(
                FieldSpec.builder(TypeName.get(long.class), "serialVersionUID")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(hash + "L")
                        .build());
    }
}