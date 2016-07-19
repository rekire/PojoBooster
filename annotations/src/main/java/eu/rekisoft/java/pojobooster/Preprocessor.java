package eu.rekisoft.java.pojobooster;

import android.annotation.TargetApi;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;

import eu.rekisoft.java.pojotoolkit.Enhance;
import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.Field;

@SupportedAnnotationTypes({"eu.rekisoft.java.pojotoolkit.Enhance", "eu.rekisoft.java.pojotoolkit.Field"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@TargetApi(24) // STFU
public class Preprocessor extends AbstractProcessor {

    public Preprocessor() {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> instances = roundEnv.getElementsAnnotatedWith(Enhance.class);
        HashMap<TypeElement, List<Element>> classes = new HashMap<>(instances.size());
        HashMap<String, String> renaming = new HashMap<>(instances.size());
        for(Element elem : instances) {
            if(elem.getKind() == ElementKind.CLASS) {
                TypeElement type = (TypeElement)elem;
                classes.put(type, new ArrayList<>());
                Enhance annotation = elem.getAnnotation(Enhance.class);
                renaming.put(type.getQualifiedName().toString(), annotation.name());
                for(AnnotationMirror annotationMirror : elem.getAnnotationMirrors()) {
                    String annotationClass = annotationMirror.getAnnotationType().asElement().asType().toString();
                    if(Enhance.class.getName().equals(annotationClass)) {

                        processSingle(annotationMirror, type, roundEnv);

                        Set<VariableElement> fields = ElementFilter.fieldsIn(instances);
                        System.out.println("Test " + fields.size());
                        for(VariableElement field : fields) {
                            System.out.println("#> " + field.asType() + " " + field.getSimpleName());
                        }

                        //getType(type.getQualifiedName().toString())


                    }
                    /*annotationMirror.getElementValues().get("extensions").getValue()
                    try {
                        Extension extension = extensionClass.newInstance();
                        extension.generateCode(roundEnv);
                    } catch(InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }*/
                }
            }
        }
        /*
        for (Element elem : roundEnv.getElementsAnnotatedWith(Pool.class)) {
            if(elem.getKind() == ElementKind.CLASS) {
                classes.put((TypeElement)elem, new ArrayList<>());
            }
        }
        for (Element elem : roundEnv.getElementsAnnotatedWith(Field.class)) {
            Field field = elem.getAnnotation(Field.class);
            TypeElement typeElement = (TypeElement)elem.getEnclosingElement();
            classes.get(typeElement).add(elem);
            String message = "Field annotation found in " + elem.getSimpleName()
                    + " with " + elem.getSimpleName() + " -> " + field.value();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
            //throw new RuntimeException(message);
        }

        for(Map.Entry<TypeElement, List<Element>> entry : classes.entrySet()) {
            TypeElement classElement = entry.getKey();
            try {
                CharSequence simpleBaseName = classElement.getSimpleName();
                CharSequence fullBaseName = classElement.getQualifiedName();
                String targetName = renaming.get(classElement.getQualifiedName().toString());
                CharSequence packageName = classElement.getQualifiedName().subSequence(0, fullBaseName.length() - simpleBaseName.length() - 1);
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(packageName + "." + targetName);
                BufferedWriter bw = new BufferedWriter(jfo.openWriter());
                bw.append("// This file is generated. Wohoo!");
                bw.newLine();
                bw.newLine();
                bw.append("package ").append(packageName).append(";");
                bw.newLine();
                bw.newLine();
                //printModifies(bw, classElement.getModifiers());
                bw.append("public class ").append(targetName).append(" extends ").append(simpleBaseName);
                bw.append(" {");
                bw.newLine();
                if(!members.isEmpty()) {
                    bw.append(members);
                    bw.newLine();
                }
                bw.append("// Pool with ");//.append(elem.)
                bw.newLine();
                //addJSON(bw, entry.getValue());
                bw.append("}");
                bw.flush();
                bw.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }*/
        return true; // no further processing of this annotation type
    }

    private void processSingle(AnnotationMirror annotationMirror, TypeElement type, RoundEnvironment roundEnv) {
        //System.out.println(annotationMirror.getAnnotationType().asElement().asType().toString());
        CharSequence simpleBaseName = type.getSimpleName();
        String targetName = simpleBaseName.toString();
        CharSequence fullBaseName = type.getQualifiedName();
        CharSequence packageName = type.getQualifiedName().subSequence(0, fullBaseName.length() - simpleBaseName.length() - 1);
        HashSet<TypeName> imports = new HashSet<>();
        HashSet<TypeName> interfaces = new HashSet<>();
        List<MethodSpec> methods = new ArrayList<>();
        List<FieldSpec> members = new ArrayList<>();

        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if("extensions".equals(entry.getKey().getSimpleName().toString())) {
                //System.out.println(entry.getValue().getClass() + ": " + entry.getValue());
                List<AnnotationValue> extensions = (List<AnnotationValue>)entry.getValue().getValue();
                for(AnnotationValue extension : extensions) {
                    DeclaredType extensionType = (DeclaredType)extension.getValue();
                    System.out.println(extensionType.toString());
                    try {
                        Extension impl = (Extension)Class.forName(extensionType.toString()).getDeclaredConstructor(TypeName.class).newInstance(ClassName.bestGuess(targetName));
                        //System.out.println("Yey!");
                        methods.addAll(impl.generateCode(type.getQualifiedName().toString(), roundEnv));
                        members.addAll(impl.generateMembers());
                        imports.addAll(impl.getAttentionalImports());
                        interfaces.addAll(impl.getAttentionalInterfaces());
                    } catch(ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }
            } else if("name".equals(entry.getKey().getSimpleName().toString())) {
                // TODO this can be done better
                targetName = entry.getValue().toString();
                targetName = targetName.substring(1, targetName.length() - 1);
            }
            //System.out.println(entry.getKey().getSimpleName() + ": " + entry.getValue().getValue());
        }

        TypeSpec.Builder generated = TypeSpec
                .classBuilder(targetName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.bestGuess(type.getQualifiedName().toString()));
        for(TypeName anInterface : interfaces) {
            generated.addSuperinterface(anInterface);
        }
        for(FieldSpec member : members) {
            generated.addField(member);
        }
        for(MethodSpec method : methods) {
            generated.addMethod(method);
        }

        JavaFile javaFile = JavaFile.builder(packageName.toString(), generated.build()).indent("    ").build();


        try {
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(packageName + "." + targetName);
            BufferedWriter bw = new BufferedWriter(jfo.openWriter());
            bw.append("// This file is generated. Wohoo!");
            bw.newLine();
            bw.newLine();
            bw.append(javaFile.toString().trim());
            bw.flush();
            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void printModifies(BufferedWriter bw, Set<Modifier> modifiers) throws IOException {
        for(Modifier modifier : modifiers) {
            switch(modifier) {
            case ABSTRACT:
                bw.append("abstract ");
                break;
            case FINAL:
                bw.append("final ");
                break;
            case PRIVATE:
                bw.append("private ");
                break;
            case PUBLIC:
                bw.append("public ");
                break;
            case PROTECTED:
                bw.append("protected ");
                break;
            case STATIC:
                bw.append("static ");
                break;
            }
        }
    }

    private void addJSON(BufferedWriter bw, List<Element> elems) throws IOException {
        for(Element elem : elems) {
            Field field = elem.getAnnotation(Field.class);
            bw.append("    ");
            printModifies(bw, elem.getModifiers());
            bw.append(elem.asType().toString()).append(" ").append(elem.getSimpleName()).append(";");
            if(!field.value().isEmpty()) {
                bw.append(" // should be serialized as '").append(field.value()).append("'");
            }
            bw.newLine();
        }
        bw.newLine();
        bw.append("    public String toJSON() {");
        bw.newLine();
        /*// try one
        bw.append("        return \"{");
        for(int i = 0; i < elems.size(); i++) {
            Element elem = elems.get(i);
            Field field = elem.getAnnotation(Field.class);
            // TODO add type safety
            bw.append("\\\"");
            if(field.value().isEmpty()) {
                bw.append(elem.getSimpleName());
            } else {
                bw.append(field.value());
            }
            bw.append("\\\":\" + ").append(elem.getSimpleName());
            if(i < elems.size() - 1) {
                bw.append(" + \", ");
            }
        }//*/
        String newLine = "\n            ";
        bw.append("        StringBuilder sb = new StringBuilder(\"{\")").append(newLine);
        for(int i = 0; i < elems.size(); i++) {
            Element elem = elems.get(i);
            Field field = elem.getAnnotation(Field.class);
            //bw.append("// ")
            bw.append(".append(\"\\\"");
            if(field.value().isEmpty()) {
                bw.append(elem.getSimpleName());
            } else {
                bw.append(field.value());
            }
            // TODO add type safety
            bw.append("\\\":\")");
            addElementToJson(bw, elem, field);
            //bw.append("\"").append(elem.getSimpleName());
            if(i < elems.size() - 1) {
                bw.append("sb.append(\", \")");
            }
            //bw.append(newLine);
        }
        bw.append("return sb.append(\"}\").toString();");
        bw.newLine();
        bw.append("    }");
        bw.newLine();
    }

    private void addElementToJson(BufferedWriter bw, Element elem, Field annotation) throws IOException {
        if(!elem.asType().getKind().isPrimitive()) {
            bw.append(";\n        if(").append(elem.getSimpleName()).append(" == null) {\n" +
                    "            sb.append(\"null\");\n" +
                    "        } else {\n" +
                    "            sb.append(");
        } else {
            bw.append("\n            .append(");
        }
        switch(elem.asType().getKind()) {
        case BOOLEAN:
        case SHORT:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
            bw.append(elem.getSimpleName());
            break;
        case BYTE:
            bw.append("Integer.toHexString(")
                    .append(elem.getSimpleName())
                    .append(")");
            break;
        case ARRAY:
            // TODO
            break;
        default:
            bw.append("'\"').append(").append(elem.getSimpleName());
            //elem.as
            //processingEnv.getTypeUtils().asElement(elem.asType())
            //bw.append("/*").append(elem.getClass().getName()).append("*/");
            bw.append(".toString()");
            bw.append(").append('\"'");
        }
        if(!elem.asType().getKind().isPrimitive()) {
            bw.append(");\n        }\n        ");
        } else {
            bw.append(");\n        ");
        }
    }

    private Map.Entry<TypeElement, DeclaredType> getType(String className) {
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(className);
        DeclaredType declaredType = processingEnv.getTypeUtils().getDeclaredType(typeElement);
        return new HashMap.SimpleEntry<>(typeElement, declaredType);
    }

    // http://stackoverflow.com/a/10167558/995926
    private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for(AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if(m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if(entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }


    public static <T> T getAnnotationValue(TypeElement element, Class<? extends Annotation> annotation, String key) {
        AnnotationMirror am = getAnnotationMirror(element, annotation);
        if(am == null) {
            return null;
        }
        AnnotationValue av = getAnnotationValue(am, key);
        if(av == null) {
            return null;
        } else {
            return (T)av.getValue();
        }
    }
}