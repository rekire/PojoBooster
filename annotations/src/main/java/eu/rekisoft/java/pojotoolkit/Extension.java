package eu.rekisoft.java.pojotoolkit;

import java.util.List;

import javax.annotation.processing.RoundEnvironment;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public abstract class Extension {
    protected final String className;

    public Extension(String className) {
        this.className = className;
    }

    public abstract List<Class<?>> getAttentionalInterfaces();

    //@NonNull
    public abstract List<Class<?>> getAttentionalImports();

    public abstract String generateCode(String filter, RoundEnvironment environment);

    public abstract String generateMembers();
}