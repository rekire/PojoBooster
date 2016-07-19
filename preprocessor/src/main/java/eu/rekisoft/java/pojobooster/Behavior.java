package eu.rekisoft.java.pojobooster;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by rene on 26.05.2016.
 */
public enum Behavior {
    Default("default"),
    True("true"),
    False("false");

    private final String id;
    Behavior(String id) {
        this.id = id;
    }

    @SupportedAnnotationTypes({"eu.rekisoft.java.pojobooster.Field", "eu.rekisoft.java.pojobooster.PojoBooster"})
    @SupportedSourceVersion(SourceVersion.RELEASE_8)
    public static class Preprocessor extends AbstractProcessor {

        public Preprocessor() {
            super();
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            HashMap<String, PojoBooster> mapping = new HashMap<>();
            for (Element elem : roundEnv.getElementsAnnotatedWith(PojoBooster.class)) {
                PojoBooster field = elem.getAnnotation(PojoBooster.class);
                String message = "PojoBooster annotation found in " + elem.getSimpleName()
                        + " with the serializers: " + Arrays.toString(field.value());
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);

                mapping.put(elem.getSimpleName().toString(), field);
            }
            for (Element elem : roundEnv.getElementsAnnotatedWith(Field.class)) {
                Field field = elem.getAnnotation(Field.class);
                String message = "Field annotation found in " + elem.getSimpleName()
                        + " with complexity " + elem.getSimpleName() + " -> " + field.value();
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
            }
            return true; // no further processing of this annotation type
        }
    }
}