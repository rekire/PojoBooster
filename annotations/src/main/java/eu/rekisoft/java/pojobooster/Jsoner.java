package eu.rekisoft.java.pojobooster;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import eu.rekisoft.java.pojotoolkit.*;
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
        return new ArrayList<>(0);
        /*
        StringBuilder sb = new StringBuilder();
        String newLine = "\n            ";
        sb.append("    public String toJSON() {").append(newLine);
        sb.append("StringBuilder sb = new StringBuilder(\"{\")").append(newLine);
        boolean first = true;
        for(Element elem : environment.getElementsAnnotatedWith(eu.rekisoft.java.pojotoolkit.Field.class)) {
            if(elem.getEnclosingElement().asType().toString().equals(filter)) {
                eu.rekisoft.java.pojotoolkit.Field field = elem.getAnnotation(eu.rekisoft.java.pojotoolkit.Field.class);
                //bw.append("// ")
                if (first) {
                    first = false;
                } else {
                    sb.append("sb.append(\", \")");
                }
                sb.append(".append(\"\\\"");
                if (field.value().isEmpty()) {
                    sb.append(elem.getSimpleName());
                } else {
                    sb.append(field.value());
                }
                // TODO add type safety
                sb.append("\\\":\")");
                addElementToJson(sb, elem, field);
                //bw.append("\"").append(elem.getSimpleName());
                //bw.append(newLine);
            }
        }
        sb.append("return sb.append(\"}\").toString();\n");
        //sb.newLine();
        sb.append("    }");
        //sb.newLine();

            return sb.toString();
            */
    }


    private void addElementToJson(StringBuilder sb, Element elem, Field annotation) {//} throws IOException {
        if(!elem.asType().getKind().isPrimitive()) {
            sb.append(";\n        if(").append(elem.getSimpleName()).append(" == null) {\n" +
                    "            sb.append(\"null\");\n" +
                    "        } else {\n" +
                    "            sb.append(");
        } else {
            sb.append("\n            .append(");
        }
        switch(elem.asType().getKind()) {
            case BOOLEAN:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                sb.append(elem.getSimpleName());
                break;
            case BYTE:
                sb.append("Integer.toHexString(")
                        .append(elem.getSimpleName())
                        .append(")");
                break;
            case ARRAY:
                // TODO
                break;
            default:
                sb.append("'\"').append(").append(elem.getSimpleName());
                //elem.as
                //processingEnv.getTypeUtils().asElement(elem.asType())
                //bw.append("/*").append(elem.getClass().getName()).append("*/");
                if(!String.class.getName().equals(elem.asType().toString()))
                sb.append(".toString()");
                sb.append(").append('\"'");
        }
        if(!elem.asType().getKind().isPrimitive()) {
            sb.append(");\n        }\n        ");
        } else {
            sb.append(");\n        ");
        }
    }

    @Override
    public List<FieldSpec> generateMembers() {
        return new ArrayList<>(0);
    }
}