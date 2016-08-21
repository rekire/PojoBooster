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
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import eu.rekisoft.java.pojobooster.Enhance;
import eu.rekisoft.java.pojobooster.FactoryOf;
import eu.rekisoft.java.pojobooster.JsonDecorator;

@SupportedAnnotationTypes({
        "eu.rekisoft.java.pojobooster.Enhance",
        "eu.rekisoft.java.pojobooster.JsonDecorator",
        "eu.rekisoft.java.pojobooster.FactoryOf"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@TargetApi(24) // STFU
public class Preprocessor extends AbstractProcessor {

    private final JavaCompiler compiler;
    private String sourcePath = null;

    public Preprocessor() {
        super();
        compiler = ToolProvider.getSystemJavaCompiler();
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
            } catch(IOException e) {

            }
        }

        Set<? extends Element> instances = roundEnv.getElementsAnnotatedWith(Enhance.class);
        for(Element elem : instances) {
            if(elem.getKind() != ElementKind.CLASS) {
                throw new IllegalArgumentException("No class was annotated");
            }
            List<? extends Element> members = processingEnv.getElementUtils().getAllMembers((TypeElement)elem);
            // look at all methods and fields
            List<Element> fields = new ArrayList<>();
            List<Element> methods = new ArrayList<>();
            List<Element> constructors = new ArrayList<>();
            for(Element member : members) {
                // limit to the fields
                if(member.asType().getKind() == TypeKind.EXECUTABLE) {
                    if(member.getSimpleName().toString().equals("<init>")) {
                        constructors.add(member);
                    } else {
                        methods.add(member);
                    }
                } else if(!member.asType().toString().equals(elem.toString() + "." + member.getSimpleName())) {
                    fields.add(member);
                } // else we have a nested enum or nested class
            }
            for(AnnotationMirror annotationMirror : elem.getAnnotationMirrors()) {
                String annotationClass = annotationMirror.getAnnotationType().asElement().asType().toString();
                // select our annotation
                if(Enhance.class.getName().equals(annotationClass)) {
                    writeFile(collectInfo(annotationMirror, (TypeElement)elem, fields, methods, constructors), constructors, roundEnv);
                }
            }
        }

        for(Element elem : roundEnv.getElementsAnnotatedWith(JsonDecorator.class)) {
            Method method = Method.from((ExecutableElement)elem);
            if(!method.matchesTypes(String.class, StringBuilder.class)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Yey " + elem + " this is fine!");
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "No! " + elem + " has the wrong args!");
            }
        }

        // The idea is to annotate the factory of a interface or the concreate constructor of the desired class.
        for(Element elem : roundEnv.getElementsAnnotatedWith(FactoryOf.class)) {
            for(AnnotationMirror annotationMirror : elem.getAnnotationMirrors()) {
                String annotationClass = annotationMirror.getAnnotationType().asElement().asType().toString();
                // select our annotation
                if(FactoryOf.class.getName().equals(annotationClass)) {
                    for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                        if("value".equals(entry.getKey().getSimpleName().toString())) {
                            System.out.println("type of " + entry.getValue().getValue().getClass().getName());
                        }
                    }
                }
            }
            //if(!method.matchesTypes(Object.class, String.class)) {
            //    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Yey " + elem + " this is fine!");
            //} else {
            //    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "No! " + elem + " has the wrong args!");
            //}
        }

        return true; // no further processing of this annotation type
    }

    private void writeFile(AnnotatedClass annotatedClass, List<Element> constructors, RoundEnvironment environment) {
        HashSet<TypeName> interfaces = new HashSet<>();
        List<MethodSpec> methods = new ArrayList<>();
        List<FieldSpec> members = new ArrayList<>();

        if(constructors.isEmpty()) {
            // generate the default constructor
            methods.add(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("super()")
                    .build());
        } else {
            // copy the constructors including modifiers
            for(Element element : constructors) {
                MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
                StringBuilder sb = new StringBuilder("super(");
                List<? extends TypeMirror> argTypes = ((ExecutableType)element.asType()).getParameterTypes();
                for(int i = 0; i < argTypes.size(); i++) {
                    // TODO find some trick to read out the real name
                    String argName = "arg" + i;
                    if(sb.length() > 6) {
                        sb.append(", ");
                    }
                    sb.append(argName);
                    constructor.addParameter(ClassName.get(argTypes.get(i)), argName);
                    //constructor.addParameter(ClassName.get(arg), argName);
                }
                sb.append(")");
                constructor.addStatement(sb.toString());
                constructor.addModifiers(element.getModifiers());
                methods.add(constructor.build());
            }
        }

        Extension[] extensions = new Extension[annotatedClass.extensions.size()];
        int i = 0;

        for(Class<? extends Extension> extension : annotatedClass.extensions) {
            try {
                Extension impl = extension.getDeclaredConstructor(ExtensionSettings.class).newInstance(new ExtensionSettings(annotatedClass, environment, processingEnv));
                extensions[i++] = impl;
                annotatedClass.interfaces.addAll(impl.getAttentionalInterfaces());
            } catch(ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        for(Extension impl : extensions) {
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
            String module, dir;
            if(sourcePath.indexOf("/build/classes/") > 0) {
                module = sourcePath.substring(0, sourcePath.indexOf("/build/classes/"));
                dir = module + "/src/generated/" + annotatedClass.targetType.packageName().replace(".", "/");
            } else {
                // TODO the debug string should not been hardcoded
                module = sourcePath.substring(0, sourcePath.indexOf("/build/"));
                dir = module + "/build/generated/source/pojo/debug/" + annotatedClass.targetType.packageName().replace(".", "/");
            }
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

            compile(targetFile);
        } catch(IOException e) {
            e.printStackTrace();
        } catch(StringIndexOutOfBoundsException e) {
            throw new RuntimeException("Huston we have a problem at " + sourcePath, e);
        }
    }

    private void compile(String targetFile) {
        // https://www.javacodegeeks.com/2015/09/java-compiler-api.html
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final StandardJavaFileManager manager = compiler.getStandardFileManager(
                diagnostics, null, null);

        final Iterable<? extends JavaFileObject> sources =
                manager.getJavaFileObjectsFromFiles(Arrays.asList(new File(targetFile)));

        File target = new File("D:\\Programmierung\\PojoBooster\\examplelibrary\\build\\intermediates\\classes\\debug");
        if(!target.exists()) {
            target.mkdirs();
        }

        // gradlew cle publishToMavenLocal
        // gradlew :ex:aDeb --stacktrace

        // set compiler's classpath to be same as the runtime's
        List<String> optionList = Arrays.asList("-classpath", "C:\\Users\\rekir_000\\.m2\\repository\\eu\\rekisoft\\pojobooster\\Annotations\\0.0.0\\Annotations-0.0.0.jar;" +
                "C:\\Users\\rekir_000\\.m2\\repository\\eu\\rekisoft\\pojobooster\\Preprocessor\\0.0.0\\Preprocessor-0.0.0.jar;" +
                "C:\\Users\\rekir_000\\.gradle\\caches\\modules-2\\files-2.1\\com.squareup\\javapoet\\1.7.0\\4fdcf1fc27c1a8f55d1109df986c923152f07759\\javapoet-1.7.0.jar;" +
                "G:\\sdk\\extras\\android\\m2repository\\com\\android\\support\\support-annotations\\24.1.0\\support-annotations-24.1.0.jar;" +
                "C:\\Users\\rekir_000\\.gradle\\caches\\modules-2\\files-2.1\\org.robolectric\\android-all\\6.0.0_r1-robolectric-0\\ae05eb8f25e9ec919c029f04164994f3c53e255d\\android-all-6.0.0_r1-robolectric-0.jar;" +
                "D:\\Programmierung\\PojoBooster\\examplelibrary\\src\\main\\java;D:\\Programmierung\\PojoBooster\\examplelibrary\\build\\intermediates\\classes\\debug",
                "-d", "D:\\Programmierung\\PojoBooster\\examplelibrary\\build\\intermediates\\classes\\debug");

        final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics,
                optionList, null, sources);
        task.call();

        for(final Diagnostic<? extends JavaFileObject> diagnostic :
                diagnostics.getDiagnostics()) {

            System.out.format("SELF-COMPILE: %s, line %d in %s\n",
                    diagnostic.getMessage(null),
                    diagnostic.getLineNumber(),
                    diagnostic.getSource().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private AnnotatedClass collectInfo(AnnotationMirror annotationMirror, TypeElement type, List<Element> fields, List<Element> methods, List<Element> constructors) {
        List<Class<?>> extensions = new ArrayList<>();
        String targetName = type.getSimpleName().toString();
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if("extensions".equals(entry.getKey().getSimpleName().toString())) {
                for(AnnotationValue extension : (List<AnnotationValue>)entry.getValue().getValue()) {
                    DeclaredType extensionType = (DeclaredType)extension.getValue();
                    try {
                        extensions.add(Class.forName(extensionType.toString()));
                    } catch(ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }
            } else if("name".equals(entry.getKey().getSimpleName().toString())) {
                targetName = entry.getValue().getValue().toString();
            }
        }
        String packageName = type.getQualifiedName().toString();
        packageName = packageName.substring(0, packageName.indexOf(type.getSimpleName().toString()));
        return new AnnotatedClass(extensions, ClassName.bestGuess(packageName + targetName), ClassName.bestGuess(type.toString()), fields, methods);
    }
}