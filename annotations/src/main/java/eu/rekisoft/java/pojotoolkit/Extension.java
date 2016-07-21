package eu.rekisoft.java.pojotoolkit;

import android.support.annotation.NonNull;

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
    @NonNull protected final AnnotatedClass annotatedClass;
    @NonNull private final RoundEnvironment environment;

    public Extension(@NonNull AnnotatedClass annotatedClass, @NonNull RoundEnvironment environment) {
        this.annotatedClass = annotatedClass;
        this.environment = environment;
    }

    @NonNull
    public abstract List<TypeName> getAttentionalInterfaces();

    @NonNull
    public abstract List<FieldSpec> generateMembers();

    @NonNull
    public abstract List<MethodSpec> generateCode();
}