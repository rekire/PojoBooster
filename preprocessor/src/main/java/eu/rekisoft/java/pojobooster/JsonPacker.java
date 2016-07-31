package eu.rekisoft.java.pojobooster;

import android.annotation.SuppressLint;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.lang.model.element.Modifier;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.ExtensionSettings;
import eu.rekisoft.java.pojotoolkit.JsonWriter;
import eu.rekisoft.java.pojotoolkit.LocaleHelper;

/**
 * Created by René Kilczan on 19.07.16.
 */
@SuppressLint("NewApi")
public class JsonPacker extends Extension {

    public JsonPacker(@NonNull ExtensionSettings settings) {
        super(settings);
    }

    @NonNull
    @Override
    public List<TypeName> getAttentionalInterfaces() {
        return Collections.singletonList(TypeName.get(JsonWriter.class));
    }

    @NonNull
    @Override
    public List<MethodSpec> generateCode() {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("toJson")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);
        method.addStatement("StringBuilder sb = new StringBuilder(\"{\")");
        String delimiter = "";
        for(AnnotatedClass.Member member : annotatedClass.members) {
            String fieldName = (String)member.getAnnotatedProperty(Field.class, "value");
            if(fieldName == null || fieldName.isEmpty()) {
                fieldName = member.name;
            }
            method.addStatement("sb.append(\"$L\\\"$L\\\":\")", delimiter, fieldName);
            if(delimiter.isEmpty()) {
                delimiter = ", ";
            }

            // TODO add type safety
            addElementToJson(method, member);
        }
        method.addStatement("return sb.toString()");
        return Collections.singletonList(method.build());
    }

    private void addElementToJson(MethodSpec.Builder method, AnnotatedClass.Member member) {
        if(!member.type.getKind().isPrimitive()) {
            method.beginControlFlow("if($L == null)", member.name)
                    .addStatement("sb.append(\"null\")")
                    .nextControlFlow("else");
        }
        boolean isArray = false;
        String format = (String)member.getAnnotatedProperty(Formatter.class, "value");
        switch(member.type.getKind()) {
        case BOOLEAN:
        case SHORT:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
            if(format == null) {
                method.addStatement("sb.append($L)", member.name);
            } else {
                addFormatNumberCode(method, member, format);
            }
            break;
        case BYTE:
            method.addStatement("sb.append(Integer.toHexString($L))", member.name);
            break;
        case ARRAY:
            isArray = true;
        default:
            if(isInstanceOf(member, JsonWriter.class)) {
                method.addStatement("sb.append($L.toJson())", member.name);
            } else if(format != null && isInstanceOf(member, Date.class)) {
                addFormatDateCode(method, member, format);
            } else if(format != null && isInstanceOf(member, Number.class)) {
                addFormatNumberCode(method, member, format);
            } else if(String.class.getName().equals(member.typeName)) {
                // TODO mask the string (\ -> \\, " -> \")
                method.addStatement("sb.append($L)", member.name);
            } else {
                method.addStatement("sb.append('\"').append($L.toString()).append('\"')", member.name);
            }
        }
        if(!member.type.getKind().isPrimitive()) {
            method.endControlFlow();
        }
    }

    private void addFormatDateCode(MethodSpec.Builder method, AnnotatedClass.Member member, String format) {
        LocaleHelper locale = LocaleHelper.from(member);
        TypeName type = ClassName.get(SimpleDateFormat.class);
        method.addStatement("$T $LFormatter = new $T($S, $L$T$L)", type, member.name, type, format, locale.prefix, locale.type, locale.suffix);
        method.addStatement("sb.append($LFormatter.format($L))", member.name, member.name);
    }

    private void addFormatNumberCode(MethodSpec.Builder method, AnnotatedClass.Member member, String format) {
        LocaleHelper locale = LocaleHelper.from(member);
        TypeName stringType = ClassName.get(String.class);
        method.addStatement("sb.append($T.format($L$T$L, $S, $L))", stringType, locale.prefix, locale.type, locale.suffix, format, member.name);
    }

    @NonNull
    @Override
    public List<FieldSpec> generateMembers() {
        return new ArrayList<>(0);
    }
}