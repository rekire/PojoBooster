package eu.rekisoft.java.pojobooster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import android.os.Parcel;
import android.os.Parcelable;

import eu.rekisoft.java.pojotoolkit.Extension;

/**
 * Created on 17.07.2016.
 *
 * @author René Kilczan
 */
public class Parcabler extends Extension {
    private final ArrayList<Class<?>> classes;
    private final Map<TypeMirror, Name> fields = new HashMap<>();

    public Parcabler(String className) {
        super(className);
        classes = new ArrayList<>(2);
        classes.add(Parcelable.class);
        classes.add(Parcel.class);
    }

    @Override
    public List<Class<?>> getAttentionalInterfaces() {
        return Collections.singletonList(Parcelable.class);
    }

    @Override
    public List<Class<?>> getAttentionalImports() {
        return classes;
    }

    @Override
    public String generateCode(String filter, RoundEnvironment environment) {
        for(Element elem : environment.getElementsAnnotatedWith(eu.rekisoft.java.pojotoolkit.Field.class)) {
            if(elem.getEnclosingElement().asType().toString().equals(filter)) {
                eu.rekisoft.java.pojotoolkit.Field field = elem.getAnnotation(eu.rekisoft.java.pojotoolkit.Field.class);
                String message = "Field annotation found in " + elem.getSimpleName()
                        + " with " + elem.getSimpleName() + " -> " + field.value();
                //System.out.println(elem.getEnclosingElement().asType() + " - " + elem.asType() + " " + elem.getSimpleName());
                fields.put(elem.asType(), elem.getSimpleName());
                //classes.get(typeElement).add(elem);
                //String message = "Field annotation found in " + elem.getSimpleName()
                //        + " with " + elem.getSimpleName() + " -> " + field.value();
                //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
            }
        }
        return "    @Override\n" +
                "    public int describeContents() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void writeToParcel(Parcel dest, int flags) {\n" +
//                "        dest.writeInt(this.id);\n" +
                "    }\n" +
                "\n" +
                "    protected " + className + "(Parcel in) {\n" +
//                "        this.id = in.readInt();\n" +
                "    }";
    }

    @Override
    public String generateMembers() {
        return "    public static final Parcelable.Creator<" + className + "> CREATOR = new Parcelable.Creator<" + className + ">() {\n" +
                "        @Override\n" +
                "        public " + className + " createFromParcel(Parcel source) {\n" +
                "            return new " + className + "(source);\n" +
                "        }\n" +
                "\n" +
                "        @Override\n" +
                "        public " + className + "[] newArray(int size) {\n" +
                "            return new " + className + "[size];\n" +
                "        }\n" +
                "    };";
    }
}
