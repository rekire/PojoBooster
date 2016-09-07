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
    final String logLevel;
    final String variantName;
    final boolean createStub;

    ExtensionSettings(AnnotatedClass annotatedClass, RoundEnvironment environment,
                      ProcessingEnvironment processingEnv, String logLevel, String variantName,
                      boolean stub) {
        this.annotatedClass = annotatedClass;
        this.environment = environment;
        this.processingEnv = processingEnv;
        this.logLevel = logLevel;
        this.variantName = variantName;
        this.createStub = stub;
    }
}