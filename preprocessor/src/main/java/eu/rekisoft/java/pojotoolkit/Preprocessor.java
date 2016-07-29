package eu.rekisoft.java.pojotoolkit;

import android.annotation.TargetApi;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import eu.rekisoft.java.pojobooster.Enhance;

@SupportedAnnotationTypes({"eu.rekisoft.java.pojobooster.Enhance"/*, "eu.rekisoft.java.pojobooster.PojoBooster.ReflectiveAnnotation"*/})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@TargetApi(24) // STFU
public class Preprocessor extends AbstractProcessor {

    private String sourcePath = null;

    public Preprocessor() {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(sourcePath == null) {
            try {
                JavaFileObject generationForPath = processingEnv.getFiler().createSourceFile("Killme" + System.currentTimeMillis());
                Writer writer = generationForPath.openWriter();
                sourcePath = generationForPath.toUri().getPath();
                writer.flush();
                writer.close();
                generationForPath.delete();
                //System.out.println(sourcePath);
            } catch(IOException e) {

            }
        }

        Set<? extends Element> instances = roundEnv.getElementsAnnotatedWith(Enhance.class);
        for(Element elem : instances) {
            if(elem.getKind() != ElementKind.CLASS) {
                throw new IllegalArgumentException("No class was annotated");
            }
            //System.out.println("?> " + elem.getAnnotationMirrors());
            List<? extends Element> members = processingEnv.getElementUtils().getAllMembers((TypeElement)elem);
            // look at all methods and fields
            List<Element> fields = new ArrayList<>();
            List<Element> methods = new ArrayList<>();
            for(Element member : members) {
                // limit to the fields
                if(member.asType().getKind() == TypeKind.EXECUTABLE) {
                    methods.add(member);
                } else if(!member.asType().toString().equals(elem.toString() + "." + member.getSimpleName())) {
                    fields.add(member);
                } // else we have a nested enum or nested class
            }
            for(AnnotationMirror annotationMirror : elem.getAnnotationMirrors()) {
                String annotationClass = annotationMirror.getAnnotationType().asElement().asType().toString();
                // select our annotation
                if(Enhance.class.getName().equals(annotationClass)) {
                    writeFile(collectInfo(annotationMirror, (TypeElement)elem, fields, methods), roundEnv);
                }
            }
        }
        return true; // no further processing of this annotation type
    }

    private void writeFile(AnnotatedClass annotatedClass, RoundEnvironment environment) {
        HashSet<TypeName> interfaces = new HashSet<>();
        List<MethodSpec> methods = new ArrayList<>();
        List<FieldSpec> members = new ArrayList<>();

        Extension[] extensions = new Extension[annotatedClass.extensions.size()];
        int i=0;

        for(Class<? extends Extension> extension : annotatedClass.extensions) {
            try {
                Extension impl = extension.getDeclaredConstructor(ExtensionSettings.class).newInstance(new ExtensionSettings(annotatedClass, environment, processingEnv));
                extensions[i++] = impl;
                annotatedClass.interfaces.addAll(impl.getAttentionalInterfaces());
            } catch(ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        for (Extension impl : extensions) {
            methods.addAll(impl.generateCode());
            members.addAll(impl.generateMembers());
            interfaces.addAll(impl.getAttentionalInterfaces());
        }

        TypeSpec.Builder generated = TypeSpec
                .classBuilder(annotatedClass.targetType)
                .addModifiers(Modifier.PUBLIC)
                .superclass(annotatedClass.sourceType);
        for(TypeName anInterface : interfaces) {
            generated.addSuperinterface(anInterface);
        }
        for(FieldSpec member : members) {
            generated.addField(member);
        }
        for(MethodSpec method : methods) {
            generated.addMethod(method);
        }

        JavaFile javaFile = JavaFile.builder(annotatedClass.targetType.packageName(), generated.build()).indent("    ").build();

        try {
            String module = sourcePath.substring(0, sourcePath.indexOf("/build/classes/"));
            // should be written to build/generated/source/pojo
            String dir = module + "/src/generated/" + annotatedClass.targetType.packageName().replace(".", "/");
            File directory = new File(dir);
            directory.mkdirs();
            String targetFile = dir + "/" + annotatedClass.targetType.simpleName() + ".java";
            System.out.println("write to: " + targetFile);
            BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile));
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

    @SuppressWarnings("unchecked")
    private AnnotatedClass collectInfo(AnnotationMirror annotationMirror, TypeElement type, List<Element> fields, List<Element> methods) {
        List<Class<?>> extensions = new ArrayList<>();
        String targetName = type.getSimpleName().toString();
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if("extensions".equals(entry.getKey().getSimpleName().toString())) {
                for(AnnotationValue extension : (List<AnnotationValue>)entry.getValue().getValue()) {
                    DeclaredType extensionType = (DeclaredType)extension.getValue();
                    //System.out.println(extensionType.toString());
                    try {
                        extensions.add(Class.forName(extensionType.toString()));
                    } catch(ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }
            } else if("name".equals(entry.getKey().getSimpleName().toString())) {
                targetName = entry.getValue().getValue().toString();
            }
            //System.out.println(entry.getKey().getSimpleName() + ": " + entry.getValue().getValue());
        }
        String packageName = type.getQualifiedName().toString();
        packageName = packageName.substring(0, packageName.indexOf(type.getSimpleName().toString()));
        return new AnnotatedClass(extensions, ClassName.bestGuess(packageName + targetName), ClassName.bestGuess(type.toString()), fields, methods, type);
    }

    private Map.Entry<TypeElement, DeclaredType> getType(String className) {
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(className);
        DeclaredType declaredType = processingEnv.getTypeUtils().getDeclaredType(typeElement);
        return new HashMap.SimpleEntry<>(typeElement, declaredType);
    }
}