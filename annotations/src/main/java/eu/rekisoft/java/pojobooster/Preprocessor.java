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
        for(Element elem : instances) {
            if(elem.getKind() == ElementKind.CLASS) {
                TypeElement type = (TypeElement)elem;
                Enhance annotation = elem.getAnnotation(Enhance.class);
                for(AnnotationMirror annotationMirror : elem.getAnnotationMirrors()) {
                    String annotationClass = annotationMirror.getAnnotationType().asElement().asType().toString();
                    if(Enhance.class.getName().equals(annotationClass)) {

                        processSingle(annotationMirror, type, roundEnv);

                        Set<VariableElement> fields = ElementFilter.fieldsIn(instances);
                        System.out.println("Test " + fields.size());
                        for(VariableElement field : fields) {
                            System.out.println("#> " + field.asType() + " " + field.getSimpleName());
                        }
                    }
                }
            }
        }
        return true; // no further processing of this annotation type
    }

    private void processSingle(AnnotationMirror annotationMirror, TypeElement type, RoundEnvironment roundEnv) {
        //System.out.println(annotationMirror.getAnnotationType().asElement().asType().toString());
        CharSequence simpleBaseName = type.getSimpleName();
        String targetName = simpleBaseName.toString();
        CharSequence fullBaseName = type.getQualifiedName();
        CharSequence packageName = type.getQualifiedName().subSequence(0, fullBaseName.length() - simpleBaseName.length() - 1);
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
                        methods.addAll(impl.generateCode(type.getQualifiedName().toString(), roundEnv));
                        members.addAll(impl.generateMembers());
                        interfaces.addAll(impl.getAttentionalInterfaces());
                    } catch(ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }
            } else if("name".equals(entry.getKey().getSimpleName().toString())) {
                targetName = entry.getValue().getValue().toString();
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


    private Map.Entry<TypeElement, DeclaredType> getType(String className) {
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(className);
        DeclaredType declaredType = processingEnv.getTypeUtils().getDeclaredType(typeElement);
        return new HashMap.SimpleEntry<>(typeElement, declaredType);
    }
}