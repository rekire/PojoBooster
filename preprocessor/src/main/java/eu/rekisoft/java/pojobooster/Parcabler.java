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
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;

import eu.rekisoft.java.pojotoolkit.Extension;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public class Parcabler extends Extension {

    public Parcabler(AnnotatedClass annotatedClass, RoundEnvironment environment) {
        super(annotatedClass, environment);
    }

    @Override
    public List<TypeName> getAttentionalInterfaces() {
        return Collections.singletonList(TypeName.get(Parcelable.class));
    }

    @Override
    public List<MethodSpec> generateCode() {
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
        for(AnnotatedClass.Member member : annotatedClass.members) {

            TypeKind fieldKind = member.element.asType().getKind();
            String suffix = "";
            if(fieldKind == TypeKind.ARRAY) {
                suffix = "Array";
                fieldKind = ((ArrayType)member.element.asType()).getComponentType().getKind();
            }
            String type = null;
            switch(fieldKind) {
            case BOOLEAN:
                writeToParcel.addStatement("dest.writeByte($L ? 0 : 1)", member.element);
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
                if(String.class.getName().equals(member.element.toString())) {
                    type = "String";
                }
                //System.out.println(((DeclaredType)element.getKey()).asElement());
                break;
            case ARRAY:
            default:
                throw new RuntimeException("Not supported! " + fieldKind.name() + " " + member.element.toString());
            }
            writeToParcel.addStatement("dest.write$L$L($L)", type, suffix, member.element.toString());
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
        TypeName className = annotatedClass.targetType;
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