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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
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
        MethodSpec.Builder writeToParcel = MethodSpec.methodBuilder("writeToParcel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Parcel.class, "dest")
                .addParameter(int.class, "flags");
        for(Map.Entry<TypeMirror, Name> field : fields.entrySet()) {
            TypeKind fieldKind = field.getKey().getKind();
            String suffix = "";
            if(fieldKind == TypeKind.ARRAY) {
                suffix = "Array";
                fieldKind = ((ArrayType)field.getKey()).getComponentType().getKind();
            }
            String type = null;
            switch(fieldKind) {
            case BOOLEAN:
                writeToParcel.addStatement("dest.writeByte($L ? 0 : 1)", field.getValue());
                continue;
            case SHORT:
            case INT:
                type = "Int";
                break;
            case LONG:
                type = "Long";
                break;
            case FLOAT:
                type = "Float";
                break;
            case DOUBLE:
                type = "Double";
                break;
            case BYTE:
                type = "Byte";
                break;
            case ERROR:
                // TODO check if the we generate this class
            case DECLARED:
                type = "Value";
                Element element = ((DeclaredType)field.getKey()).asElement();
                if(String.class.getName().equals(element.toString())) {
                    type = "String";
                }
                System.out.println(((DeclaredType)field.getKey()).asElement());
                break;
            case ARRAY:
            default:
                throw new RuntimeException("Not supported! " + fieldKind.name() + " " + field.getValue());
            }
            writeToParcel.addStatement("dest.write$L$L($L)", type, suffix, field.getValue());
        }
        methods.add(writeToParcel.build());
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Parcel.class, "in");
        methods.add(constructor.build());
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