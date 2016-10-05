package eu.rekisoft.java.pojotoolkit;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created on 02.08.2016.
 *
 * @author René Kilczan
 */
public final class Method {
    private final TypeMirror returnType;
    private final List<? extends VariableElement> args;

    public static Method from(ExecutableElement method) {
        return new Method(method);
    }

    private Method(ExecutableElement method) {
        returnType = method.getReturnType();
        args = method.getParameters();
    }

    public boolean matchesTypes(Class<?> returnType, Class<?>... args) {
        if(!this.returnType.toString().equals(returnType.getName()) || this.args.size() != args.length) {
            return false;
        }
        if(this.args.size() == 0 && args.length == 0) {
            return true;
        }
        for(int i = 0; i < this.args.size(); i++) {
            if(!this.args.get(i).asType().toString().equals(args[i].getName())) {
                return false;
            }
        }
        return true;
    }
}