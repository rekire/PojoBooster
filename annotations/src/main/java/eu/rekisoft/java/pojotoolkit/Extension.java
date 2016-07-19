package eu.rekisoft.java.pojotoolkit;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.annotation.processing.RoundEnvironment;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public abstract class Extension {
    protected final TypeName className;

    public Extension(TypeName className) {
        this.className = className;
    }

    public abstract List<TypeName> getAttentionalInterfaces();

    //@NonNull
    public abstract List<TypeName> getAttentionalImports();

    public abstract List<MethodSpec> generateCode(String filter, RoundEnvironment environment);

    public abstract List<FieldSpec> generateMembers();
}