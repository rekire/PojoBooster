package eu.rekisoft.java.pojotoolkit;

import android.annotation.SuppressLint;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Locale;

import eu.rekisoft.java.pojobooster.Formatter;

/**
 * Created on 31.07.2016.
 */
public class LocaleHelper {
    public final String prefix;
    public final String suffix;
    public final TypeName type;

    @SuppressLint("NewApi")
    private LocaleHelper(AnnotatedClass.Member member) {
        type = ClassName.get(Locale.class);
        String locale = (String) member.getAnnotatedProperty(Formatter.class, "locale");
        if(locale == null || locale.isEmpty()) {
            // use default
            prefix = "";
            suffix = ".getDefault()";
        } else {
            boolean first = true;
            prefix = "new ";
            StringBuilder sb = new StringBuilder("(");
            for(String part : locale.split("_")) {
                if(first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append('"').append(part).append('"');
            }
            sb.append(")");
            suffix = sb.toString();
        }
    }

    public static LocaleHelper from(AnnotatedClass.Member member) {
        return new LocaleHelper(member);
    }
}