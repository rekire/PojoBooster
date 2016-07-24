package eu.rekisoft.java.pojotoolkit;

import android.support.annotation.NonNull;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.util.Types;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public abstract class Extension {
    @NonNull
    protected final AnnotatedClass annotatedClass;
    @NonNull
    private final ExtensionSettings settings;

    public Extension(@NonNull ExtensionSettings settings) {
        this.annotatedClass = settings.annotatedClass;
        this.settings = settings;
    }

    protected Types getTypeHelper() {
        return settings.processingEnv.getTypeUtils();
    }

    @NonNull
    public abstract List<TypeName> getAttentionalInterfaces();

    @NonNull
    public abstract List<FieldSpec> generateMembers();

    @NonNull
    public abstract List<MethodSpec> generateCode();
}