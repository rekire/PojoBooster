package eu.rekisoft.java.pojotoolkit;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

/**
 * Helper class to create the ExtensionSettings for unit tests.
 * Created on 21.09.2016.
 *
 * @author René Kilczan
 */
public class SettingsFactory {
   public static ExtensionSettings create(List<AnnotatedClass.Member> members, RoundEnvironment environment,
                                          ProcessingEnvironment processingEnv, String logLevel, String variantName, boolean stub) {
       AnnotatedClass annotatedClass = new AnnotatedClass(
               ClassName.get("com.example", "Source"),
               ClassName.get("com.example", "Target"),
               members,
               new ArrayList<Element>(0),
               new ArrayList<Element>(0),
               null
       );

       return new ExtensionSettings(annotatedClass, environment, processingEnv, logLevel, variantName, stub);
   }
}