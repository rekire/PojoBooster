package eu.rekisoft.java.pojobooster;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import eu.rekisoft.java.pojotoolkit.Extension;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public class Parcabler extends Extension {
    private final ArrayList<TypeName> classes;
    private final Map<TypeMirror, Name> fields = new HashMap<>();

    public Parcabler(TypeName className) {
        super(className);
        classes = new ArrayList<>(2);
        classes.add(TypeName.get(Parcelable.class));
        classes.add(TypeName.get(Parcel.class));
    }

    @Override
    public List<TypeName> getAttentionalInterfaces() {
        return Collections.singletonList(TypeName.get(Parcelable.class));
    }

    @Override
    public List<MethodSpec> generateCode(String filter, RoundEnvironment environment) {
        for(Element elem : environment.getElementsAnnotatedWith(eu.rekisoft.java.pojotoolkit.Field.class)) {
            if(elem.getEnclosingElement().asType().toString().equals(filter)) {
                eu.rekisoft.java.pojotoolkit.Field field = elem.getAnnotation(eu.rekisoft.java.pojotoolkit.Field.class);
                String message = "Field annotation found in " + elem.getSimpleName()
                        + " with " + elem.getSimpleName() + " -> " + field.value();
                //System.out.println(elem.getEnclosingElement().asType() + " - " + elem.asType() + " " + elem.getSimpleName());
                fields.put(elem.asType(), elem.getSimpleName());
                //classes.get(typeElement).add(elem);
                //String message = "Field annotation found in " + elem.getSimpleName()
                //        + " with " + elem.getSimpleName() + " -> " + field.value();
                //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
            }
        }
        List<MethodSpec> methods = new ArrayList<>(3);
        methods.add(MethodSpec.methodBuilder("describeContents")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addStatement("return 0")
                .build());
        methods.add(MethodSpec.methodBuilder("writeToParcel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Parcel.class, "dest")
                .addParameter(int.class, "flags")
                // TODO add missing statments
                // .addStatement("")
                .build());
        methods.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Parcel.class, "in")
                // TODO add missing statments
                // .addStatement("this.id = in.readInt();")
                .build());
        return methods;
    }

    @Override
    public List<FieldSpec> generateMembers() {
        TypeName generatorType = ParameterizedTypeName.get(ClassName.get(Parcelable.Creator.class), className);
        TypeSpec creator = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(generatorType)
                .addMethod(MethodSpec.methodBuilder("createFromParcel")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Parcel.class, "source")
                        .returns(className)
                        .addStatement("return new $L($N)", className, "source")
                        .build())
                .addMethod(MethodSpec.methodBuilder("newArray")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(int.class, "size")
                        .returns(ArrayTypeName.of(className))
                        .addStatement("return new $L[$N]", className, "size")
                        .build())
                .build();

        return Collections.singletonList(
                FieldSpec.builder(generatorType, "CREATOR")
                        .initializer("$L", creator)
                        .build());
    }
}
