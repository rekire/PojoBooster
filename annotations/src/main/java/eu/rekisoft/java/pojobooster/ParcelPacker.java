package eu.rekisoft.java.pojobooster;

import android.annotation.SuppressLint;
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
@SuppressLint("NewApi")
public class ParcelPacker extends Extension {

    public ParcelPacker(@NonNull ExtensionSettings settings) {
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
                fieldKind = ((ArrayType)member.type).getComponentType().getKind();
            }
            String args = "";
            String type = null;
            TypeName castTo = null;
            switch(fieldKind) {
            case BOOLEAN:
                if(suffix.isEmpty()) {
                    writeToParcel.addStatement("dest.writeByte(($T)($L ? 0 : 1))", TypeName.BYTE, member.name);
                    constructor.addStatement("$L = in.readByte() == 1", member.name);
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
                    writeToParcel.addStatement("dest.writeCharArray($L)", member.name);
                    constructor.addStatement("$L = in.createCharArray()", member.name);
                    continue;
                }
                break;
            case SHORT:
                if(suffix.isEmpty()) {
                    castTo = TypeName.SHORT;
                    type = "Int";
                } else {
                    writeToParcel.addStatement("// convert short[] to int[]");
                    writeToParcel.addStatement("$T[] $LAsIntArray = new $T[$L.length]", TypeName.INT, member.name, TypeName.INT, member.name);
                    writeToParcel.beginControlFlow("for(int i = 0; i < $LAsIntArray.length; i++)", member.name);
                    writeToParcel.addStatement("$LAsIntArray[i] = ($T)$L[i]", member.name, TypeName.INT, member.name);
                    writeToParcel.endControlFlow();
                    writeToParcel.addStatement("dest.writeIntArray($LAsIntArray)", member.name);
                    constructor.addStatement("// convert int[] to short[]", member.name);
                    constructor.addStatement("$T[] $LAsIntArray = in.createIntArray()", TypeName.INT, member.name);
                    constructor.addStatement("$L = new $T[$LAsIntArray.length]", member.name, TypeName.SHORT, member.name);
                    constructor.beginControlFlow("for(int i = 0; i < $LAsIntArray.length; i++)", member.name);
                    constructor.addStatement("$L[i] = ($T)$LAsIntArray[i]", member.name, TypeName.SHORT, member.name);
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
                System.out.println("Error with " + member.typeName);
            case DECLARED:
                //System.out.println("Processing " + member.element.asType() + " (" + member.element.asType().getClass() + ")");

                if(Bundle.class.getName().equals(member.typeName)) {
                    type = "Bundle";
                } else if(String.class.getName().equals(member.typeName)) {
                    type = "String";
                } else if(member.typeName.startsWith(List.class.getName() + "<")) {
                    suffix = "List";
                    String typeName = member.typeName;
                    //DeclaredType declaredType = (DeclaredType)((TypeElement)member.element.asType()).getInterfaces().get(0);
                    //System.out.println("###> " + declaredType);
                    typeName = typeName.substring(typeName.indexOf("<") + 1, typeName.length() - 1);
                    if(String.class.getName().equals(typeName)) {
                        type = "String";
                    }
                } else {
                    for(TypeMirror supertype : getTypeHelper().directSupertypes(member.type)) {
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

                //System.out.println(annotatedClass.targetType + " has this interfaces " + annotatedClass.interfaces.size());
                //System.out.println(((DeclaredType)element.getKey()).asElement());
                break;
            case ARRAY:
            default:
                throw new RuntimeException("Not supported! " + fieldKind.name() + " " + member.name);
            }
            writeToParcel.addStatement("dest.write$L$L($L$L)", type, suffix, member.name, args);
            if("List".equals(suffix)) {
                suffix = "ArrayList";
            }
            if("Parcelable".equals(type)) {
                constructor.addStatement("$L = in.readParcelable$L($T.class.getClassLoader())", member.name, suffix, member.type);
            } else if("Serializable".equals(type)) {
                constructor.addStatement("$L = ($T)in.readSerializable$L()", member.name, member.type, suffix);
            } else if(castTo != null) {
                constructor.addStatement("$L = ($T$L)in.read$L$L()", member.name, castTo, suffix.isEmpty() ? suffix : "[]", type, suffix);
            } else if("Value".equals(type)) {
                constructor.addStatement("$L = ($T)in.read$L$L($T.class.getClassLoader())", member.name, member.type, type, suffix, member.type);
            } else if("Bundle".equals(type)) {
                constructor.addStatement("$L = in.read$L($T.class.getClassLoader())", member.name, type, member.type);
            } else if(suffix.isEmpty()) {
                constructor.addStatement("$L = in.read$L()", member.name, type);
            } else {
                constructor.addStatement("$L = in.create$L$L()", member.name, type, suffix);
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