package eu.rekisoft.java.pojobooster;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.SettingsFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created on 21.09.2016.
 *
 * @author René Kilczan
 */
public class SerializerTest {
    @Test
    public void verifyOutput() {
        // initialize
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        members.add(new AnnotatedClass.Member(new HashMap<String, Map<? extends ExecutableElement, ? extends AnnotationValue>>(0), null, String.class.getName(), "string"));
        Serializer sut = new Serializer(SettingsFactory.create(members, null, null, "DEBUG", null, false));

        // verify
        assertEquals(0, sut.generateCode().size());
        assertEquals(1, sut.getAttentionalInterfaces().size());
        assertEquals(1, sut.generateMembers().size());
        assertEquals(ClassName.get(Serializable.class), sut.getAttentionalInterfaces().get(0));
        FieldSpec version = sut.generateMembers().get(0);
        assertEquals(3, version.modifiers.size());
        assertTrue("Field is not private", version.modifiers.contains(Modifier.PRIVATE));
        assertTrue("Field is not static", version.modifiers.contains(Modifier.STATIC));
        assertTrue("Field is not final", version.modifiers.contains(Modifier.FINAL));
        assertEquals("serialVersionUID", version.name);
        assertEquals(TypeName.get(long.class), version.type);
    }
}