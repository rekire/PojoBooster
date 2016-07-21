package eu.rekisoft.java.pojotoolkit;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.annotation.processing.RoundEnvironment;

import eu.rekisoft.java.pojobooster.AnnotatedClass;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public abstract class Extension {
    protected final AnnotatedClass annotatedClass;
    private final RoundEnvironment environment;

    public Extension(AnnotatedClass annotatedClass, RoundEnvironment environment) {
        this.annotatedClass = annotatedClass;
        this.environment = environment;
    }

    public abstract List<TypeName> getAttentionalInterfaces();

    public abstract List<FieldSpec> generateMembers();

    public abstract List<MethodSpec> generateCode();
}