package eu.rekisoft.java.pojobooster;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.Field;

/**
 * Created by René Kilczan on 19.07.16.
 */
public class Jsoner extends Extension {

    public Jsoner(TypeName className) {
        super(className);
    }

    @Override
    public List<TypeName> getAttentionalInterfaces() {
        return new ArrayList<>(0);
    }

    @Override
    public List<TypeName> getAttentionalImports() {
        return new ArrayList<>(0);
    }

    @Override
    public List<MethodSpec> generateCode(String filter, RoundEnvironment environment) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("toJSON")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);
        method.addStatement("StringBuilder sb = new StringBuilder(\"{\")");
        String delimiter = "";
        for(Element elem : environment.getElementsAnnotatedWith(eu.rekisoft.java.pojotoolkit.Field.class)) {
            if(elem.getEnclosingElement().asType().toString().equals(filter)) {
                eu.rekisoft.java.pojotoolkit.Field field = elem.getAnnotation(eu.rekisoft.java.pojotoolkit.Field.class);
                String fieldName;
                if(field.value().isEmpty()) {
                    fieldName = elem.getSimpleName().toString();
                } else {
                    fieldName = field.value();
                }
                method.addStatement("sb.append(\"$L\\\"$L\\\":\")", delimiter, fieldName);
                if(delimiter.isEmpty()) {
                    delimiter = ", ";
                }

                // TODO add type safety
                addElementToJson(method, elem, field);
            }
        }
        method.addStatement("return sb.toString()");
        return Collections.singletonList(method.build());
    }

    private void addElementToJson(MethodSpec.Builder method, Element elem, Field annotation) {//} throws IOException {
        if(!elem.asType().getKind().isPrimitive()) {
            method.beginControlFlow("if($L == null)", elem.getSimpleName())
                    .addStatement("sb.append(\"null\")")
                    .nextControlFlow("else");
        }
        switch(elem.asType().getKind()) {
        case BOOLEAN:
        case SHORT:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
            method.addStatement("sb.append($L)", elem.getSimpleName());
            break;
        case BYTE:
            method.addStatement("sb.append(Integer.toHexString($L))", elem.getSimpleName());
            break;
        case ARRAY:
            // TODO
            break;
        default:
            // TODO check if method has a toJson() method
            if(!String.class.getName().equals(elem.asType().toString())) {
                method.addStatement("sb.append('\"').append($L.toString()).append('\"')", elem.getSimpleName());
            } else {
                method.addStatement("sb.append($L)", elem.getSimpleName());
            }
        }
        if(!elem.asType().getKind().isPrimitive()) {
            method.endControlFlow();
        }
    }

    @Override
    public List<FieldSpec> generateMembers() {
        return new ArrayList<>(0);
    }
}