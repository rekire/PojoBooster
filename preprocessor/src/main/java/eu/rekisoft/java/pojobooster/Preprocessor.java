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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;

import eu.rekisoft.java.pojotoolkit.Enhance;
import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.Field;

@SupportedAnnotationTypes({"eu.rekisoft.java.pojotoolkit.Enhance"/*, "eu.rekisoft.java.pojotoolkit.Field"*/})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@TargetApi(24) // STFU
public class Preprocessor extends AbstractProcessor {

    public Preprocessor() {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        HashMap<TypeMirror, List<AnnotatedClass.Member>> fieldsPerType = new HashMap<>();
        for(Element elem : roundEnv.getElementsAnnotatedWith(eu.rekisoft.java.pojotoolkit.Field.class)) {
            TypeMirror typeMirror = elem.getEnclosingElement().asType();
            Field field = elem.getAnnotation(Field.class);
            List<AnnotatedClass.Member> list = fieldsPerType.get(typeMirror);
            if(list == null) {
                list = new ArrayList<>();
                fieldsPerType.put(typeMirror, list);
            }
            list.add(new AnnotatedClass.Member(field, elem));
        }

        Set<? extends Element> instances = roundEnv.getElementsAnnotatedWith(Enhance.class);
        for(Element elem : instances) {
            if(elem.getKind() == ElementKind.CLASS) {
                for(AnnotationMirror annotationMirror : elem.getAnnotationMirrors()) {
                    String annotationClass = annotationMirror.getAnnotationType().asElement().asType().toString();
                    if(Enhance.class.getName().equals(annotationClass)) {

                        writeFile(collectInfo(annotationMirror, (TypeElement)elem, roundEnv, fieldsPerType), roundEnv);

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

    private void writeFile(AnnotatedClass annotatedClass, RoundEnvironment environment) {
        HashSet<TypeName> interfaces = new HashSet<>();
        List<MethodSpec> methods = new ArrayList<>();
        List<FieldSpec> members = new ArrayList<>();

        for(Class<? extends Extension> extension : annotatedClass.extensions) {
            try {
                Extension impl = extension.getDeclaredConstructor(AnnotatedClass.class, RoundEnvironment.class).newInstance(annotatedClass, environment);
                methods.addAll(impl.generateCode());
                members.addAll(impl.generateMembers());
                interfaces.addAll(impl.getAttentionalInterfaces());
            } catch(ReflectiveOperationException e) {
                e.printStackTrace();
            }
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
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(annotatedClass.targetType.toString());
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

    @SuppressWarnings("unchecked")
    private AnnotatedClass collectInfo(AnnotationMirror annotationMirror, TypeElement type, RoundEnvironment roundEnv, HashMap<TypeMirror, List<AnnotatedClass.Member>> fieldsPerType) {
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
        return new AnnotatedClass(extensions, ClassName.bestGuess(packageName + targetName), ClassName.bestGuess(type.toString()), fieldsPerType.get(type.asType()));
    }

    private Map.Entry<TypeElement, DeclaredType> getType(String className) {
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(className);
        DeclaredType declaredType = processingEnv.getTypeUtils().getDeclaredType(typeElement);
        return new HashMap.SimpleEntry<>(typeElement, declaredType);
    }
}