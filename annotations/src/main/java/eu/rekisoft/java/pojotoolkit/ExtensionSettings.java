package eu.rekisoft.java.pojotoolkit;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

/**
 * Created on 23.07.2016.
 *
 * @author René Kilczan
 */
public class ExtensionSettings {
    final RoundEnvironment environment;
    final AnnotatedClass annotatedClass;
    final ProcessingEnvironment processingEnv;

    ExtensionSettings(AnnotatedClass annotatedClass, RoundEnvironment environment, ProcessingEnvironment processingEnv) {
        this.annotatedClass = annotatedClass;
        this.environment = environment;
        this.processingEnv = processingEnv;
    }
}
