package eu.rekisoft.java.pojobooster;

import android.annotation.SuppressLint;
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

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.ExtensionSettings;
import eu.rekisoft.java.pojotoolkit.JsonWriter;
import eu.rekisoft.java.pojotoolkit.LocaleHelper;

/**
 * Created on 19.07.16.
 *
 * @author René Kilczan
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
        String format = (String)member.getAnnotatedProperty(Formatter.class, "value");
        LocaleHelper locale = LocaleHelper.from(member);
        // TODO when is the null check really required? -> when is "null" required and not null
        // TODO add a policy to hide null values
        boolean needsNullCheck = /*!isInstanceOf(member, Number.class) && !isInstanceOf(member, Boolean.class) &&*/
                !member.type.getKind().isPrimitive() && member.type.getKind() != TypeKind.ARRAY;
        if(needsNullCheck) {
            method.beginControlFlow("if($L == null)", member.name)
                    .addStatement("sb.append(\"null\")")
                    .nextControlFlow("else");
        }
        boolean isArray = false;
        switch(member.type.getKind()) {
        case BOOLEAN:
        case SHORT:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case BYTE:
            if(format == null) {
                method.addStatement("sb.append($L)", member.name);
            } else {
                TypeName stringType = ClassName.get(String.class);
                method.addStatement("sb.append($T.format($L$T$L, $S, $L))", stringType, locale.prefix, locale.type, locale.suffix, format, member.name);
            }
            break;
        case CHAR:
            method.addStatement("sb.append(\"\\\"\").append($L).append(\"\\\"\")", member.name);
            break;
        case ARRAY:
            method.beginControlFlow("if($L == null)", member.name)
                    .addStatement("sb.append(\"null\")")
                    .nextControlFlow("if($L.length == 0)", member.name)
                    .addStatement("sb.append(\"[]\")")
                    .nextControlFlow("else")
                    .addStatement("sb.append(\"[\")")
                    .beginControlFlow("for(int $LIndex = 0; $LIndex < $L.length; $LIndex++)", member.name, member.name, member.name, member.name)
                    .beginControlFlow("if($LIndex > 0)", member.name)
                    .addStatement("sb.append(\", \")")
                    .endControlFlow()
                    .addStatement("sb.append($L[$LIndex])", member.name, member.name)
                    .endControlFlow()
                    .addStatement("sb.append(\"]\")")
                    .endControlFlow();
            break;
        default:
            if(isInstanceOf(member, List.class)) {
                method.addStatement("// hit");
                method.beginControlFlow("if($L == null)", member.name)
                        .addStatement("sb.append(\"null\")")
                        .nextControlFlow("if($L.isEmpty())", member.name)
                        .addStatement("sb.append(\"[]\")")
                        .nextControlFlow("else")
                        .addStatement("sb.append(\"[\")")
                        .beginControlFlow("for(int $LIndex = 0; $LIndex < $L.size(); $LIndex++)", member.name, member.name, member.name, member.name)
                        .beginControlFlow("if($LIndex > 0)", member.name)
                        .addStatement("sb.append(\", \")")
                        .endControlFlow()
                        .addStatement("sb.append($L.get($LIndex))", member.name, member.name)
                        .endControlFlow()
                        .addStatement("sb.append(\"]\")")
                        .endControlFlow();
                // TODO this here has to be a recursive call however no idea how.
            } else if(isInstanceOf(member, JsonWriter.class)) {
                method.addStatement("sb.append($L.toJson())", member.name);
            } else if(isInstanceOf(member, Number.class) || isInstanceOf(member, Boolean.class)) {
                if(format == null) {
                    method.addStatement("sb.append($L)", member.name);
                } else {
                    TypeName stringType = ClassName.get(String.class);
                    method.addStatement("sb.append($T.format($L$T$L, $S, $L))", stringType, locale.prefix, locale.type, locale.suffix, format, member.name);
                }
            } else if(format != null && isInstanceOf(member, Date.class)) {
                TypeName type = ClassName.get(SimpleDateFormat.class);
                method.addStatement("$T $LFormatter = new $T($S, $L$T$L)", type, member.name, type, format, locale.prefix, locale.type, locale.suffix);
                method.addStatement("sb.append($LFormatter.format($L))", member.name, member.name);
            } else if(String.class.getName().equals(member.typeName)) {
                // TODO mask the string (\ -> \\, " -> \")
                method.addStatement("sb.append($L)", member.name);
            } else {
                method.addStatement("sb.append('\"').append($L.toString()).append('\"')", member.name);
            }
        }
        if(needsNullCheck) {
            method.endControlFlow();
        }
    }

    @NonNull
    @Override
    public List<FieldSpec> generateMembers() {
        return new ArrayList<>(0);
    }
}