package eu.rekisoft.java.pojobooster;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

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

import javax.lang.model.element.Modifier;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.ExtensionSettings;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public class Parcabler extends Extension {

    public Parcabler(@NonNull ExtensionSettings settings) {
        super(settings);
    }

    @NonNull
    @Override
    public List<TypeName> getAttentionalInterfaces() {
        return Collections.singletonList(TypeName.get(Parcelable.class));
    }

    @NonNull
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
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Parcel.class, "in");
        for(AnnotatedClass.Member member : annotatedClass.members) {
            TypeKind fieldKind = member.element.asType().getKind();
            String suffix = "";
            if(fieldKind == TypeKind.ARRAY) {
                suffix = "Array";
                fieldKind = ((ArrayType)member.element.asType()).getComponentType().getKind();
            }
            String args = "";
            String type = null;
            TypeName castTo = null;
            switch(fieldKind) {
            case BOOLEAN:
                if(suffix.isEmpty()) {
                    writeToParcel.addStatement("dest.writeByte(($T)($L ? 0 : 1))", TypeName.BYTE, member.element);
                    constructor.addStatement("$L = in.readByte() == 1", member.element.toString());
                    continue;
                } else {
                    type = "Boolean";
                }
                break;
            case CHAR:
                if(suffix.isEmpty()) {
                    castTo = TypeName.CHAR;
                    type = "Int";
                } else {
                    writeToParcel.addStatement("dest.writeCharArray($L)", member.element);
                    constructor.addStatement("$L = in.createCharArray()", member.element);
                    continue;
                }
                break;
            case SHORT:
                if(suffix.isEmpty()) {
                    castTo = TypeName.SHORT;
                    type = "Int";
                } else {
                    writeToParcel.addStatement("// convert short[] to int[]", member.element);
                    writeToParcel.addStatement("$T[] $LAsIntArray = new $T[$L.length]", TypeName.INT, member.element, TypeName.INT, member.element);
                    writeToParcel.beginControlFlow("for(int i = 0; i < $LAsIntArray.length; i++)", member.element);
                    writeToParcel.addStatement("$LAsIntArray[i] = ($T)$L[i]", member.element, TypeName.INT, member.element);
                    writeToParcel.endControlFlow();
                    writeToParcel.addStatement("dest.writeIntArray($LAsIntArray)", member.element);
                    constructor.addStatement("// convert int[] to short[]", member.element);
                    constructor.addStatement("$T[] $LAsIntArray = in.createIntArray()", TypeName.INT, member.element);
                    constructor.addStatement("$L = new $T[$LAsIntArray.length]", member.element, TypeName.SHORT, member.element);
                    constructor.beginControlFlow("for(int i = 0; i < $LAsIntArray.length; i++)", member.element);
                    constructor.addStatement("$L[i] = ($T)$LAsIntArray[i]", member.element, TypeName.SHORT, member.element);
                    constructor.endControlFlow();
                    continue;
                }
                break;
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
                System.out.println("Error with " + member.element.asType());
            case DECLARED:
                System.out.println("Processing " + member.element.asType() + " (" + member.element.asType().getClass() + ")");

                if(Bundle.class.getName().equals(member.element.asType().toString())) {
                    type = "Bundle";
                } else if(String.class.getName().equals(member.element.asType().toString())) {
                    type = "String";
                } else if(member.element.asType().toString().startsWith(List.class.getName() + "<")) {
                    //type = "List";
                    String typeName = member.element.asType().toString();
                    type = typeName.substring(typeName.indexOf("<") + 1, typeName.length() - 1) + "List";
                } else {
                    for(TypeMirror supertype : getTypeHelper().directSupertypes(member.element.asType())) {
                        //System.out.println("member has implemented: " + supertype.toString());
                        // TODO check also its supertype
                        if(Parcelable.class.getName().equals(supertype.toString())) {
                            type = "Parcelable";
                            args = ", flags";
                            break;
                        } else if(java.io.Serializable.class.getName().equals(supertype.toString())) {
                            type = "Serializable";
                            break;
                        }
                    }
                }
                if(type == null) {
                    type = "Value";
                }

                System.out.println(annotatedClass.targetType + " has this interfaces " + annotatedClass.interfaces.size());
                //System.out.println(((DeclaredType)element.getKey()).asElement());
                break;
            case ARRAY:
            default:
                throw new RuntimeException("Not supported! " + fieldKind.name() + " " + member.element);
            }
            writeToParcel.addStatement("dest.write$L$L($L$L)", type, suffix, member.element, args);
            if("Parcelable".equals(type)) {
                constructor.addStatement("$L = in.readParcelable$L($L.getClass().getClassLoader())", member.element, suffix, member.element);
            } else if("Serializable".equals(type)) {
                constructor.addStatement("$L = ($T)in.readSerializable$L()", member.element.toString(), member.element, suffix);
            } else if(castTo != null) {
                constructor.addStatement("$L = ($T$L)in.read$L$L()", member.element, castTo, suffix.isEmpty() ? suffix : "[]", type, suffix);
            } else if("Value".equals(type) || "Bundle".equals(type)) {
                constructor.addStatement("$L = ($T)in.read$L$L($T.class.getClassLoader())", member.element, member.element.asType(), type, suffix, member.element.asType());
            } else if(suffix.isEmpty()) {
                constructor.addStatement("$L = in.read$L$L()", member.element, type, suffix);
            } else {
                constructor.addStatement("$L = in.create$L$L()", member.element, type, suffix);
            }
        }
        methods.add(writeToParcel.build());
        methods.add(constructor.build());
        return methods;
    }

    @NonNull
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
                        .addStatement("return new $T($N)", className, "source")
                        .build())
                .addMethod(MethodSpec.methodBuilder("newArray")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(int.class, "size")
                        .returns(ArrayTypeName.of(className))
                        .addStatement("return new $T[$N]", className, "size")
                        .build())
                .build();

        return Collections.singletonList(
                FieldSpec.builder(generatorType, "CREATOR")
                        .initializer("$L", creator)
                        .build());
    }
}