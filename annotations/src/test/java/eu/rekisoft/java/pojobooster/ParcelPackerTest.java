package eu.rekisoft.java.pojobooster;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.SettingsFactory;
import eu.rekisoft.java.pojotoolkit.testing.ProcessingEnvironmentMock;
import eu.rekisoft.java.pojotoolkit.testing.TypeMirrorMock;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by René Kilczan on 05.10.16.
 */
public class ParcelPackerTest {
    @Test
    public void testWithoutSpecialAnnotations() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(3);
        members.add(createMember(null, String.class, "string"));
        members.add(createMember(null, int.class, "number"));
        members.add(createMember(null, char.class, "character"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();

        // execute
        ParcelPacker sut = new ParcelPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, sut.getAttentionalInterfaces().size());
        List<FieldSpec> generatedMembers = sut.generateMembers();
        assertEquals(1, generatedMembers.size());
        assertEquals("CREATOR", generatedMembers.get(0).name);
        assertEquals(3, methods.size());
        // @Override public int describeContents()
        assertEquals(1, methods.get(0).annotations.size());
        assertEquals(ClassName.get(Override.class), methods.get(0).annotations.get(0).type);
        assertEquals(0, methods.get(0).annotations.get(0).members.size());
        assertEquals(1, methods.get(0).modifiers.size());
        assertTrue(methods.get(0).modifiers.contains(Modifier.PUBLIC));
        assertEquals(TypeName.INT, methods.get(0).returnType);
        assertEquals("describeContents", methods.get(0).name);
        assertEquals(0, methods.get(0).parameters.size());
        // @Override public void writeToParcel(Parcel dest, int flags)
        assertEquals(1, methods.get(1).annotations.size());
        assertEquals(ClassName.get(Override.class), methods.get(1).annotations.get(0).type);
        assertEquals(0, methods.get(1).annotations.get(0).members.size());
        assertEquals(1, methods.get(1).modifiers.size());
        assertTrue(methods.get(1).modifiers.contains(Modifier.PUBLIC));
        assertEquals(TypeName.VOID, methods.get(1).returnType);
        assertEquals("writeToParcel", methods.get(1).name);
        assertEquals(2, methods.get(1).parameters.size());
        assertEquals(ClassName.get(Parcel.class), methods.get(1).parameters.get(0).type);
        assertEquals(TypeName.INT, methods.get(1).parameters.get(1).type);
        // public void writeToParcel(Parcel dest, int flags)
        assertEquals(0, methods.get(2).annotations.size());
        assertEquals(1, methods.get(2).modifiers.size());
        assertTrue(methods.get(2).modifiers.contains(Modifier.PUBLIC));
        assertNull(methods.get(2).returnType);
        assertEquals("<init>", methods.get(2).name);
        assertEquals(1, methods.get(2).parameters.size());
        assertEquals(ClassName.get(Parcel.class), methods.get(2).parameters.get(0).type);

        assertEquals(1, sut.getAttentionalInterfaces().size());
        assertEquals(ClassName.get(Parcelable.class), sut.getAttentionalInterfaces().get(0));
    }

    @Test
    public void testMultipleTypes() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(31);
        members.add(createMember(null, boolean.class, "aBoolean"));
        members.add(createMember(null, byte.class, "aByte"));
        members.add(createMember(null, short.class, "aShort"));
        members.add(createMember(null, int.class, "aInt"));
        members.add(createMember(null, long.class, "aLong"));
        members.add(createMember(null, char.class, "aChar"));
        members.add(createMember(null, float.class, "aFloat"));
        members.add(createMember(null, double.class, "aDouble"));
        members.add(createMember(null, String.class, "string"));
        members.add(createMember(null, UUID.class, "aSerializable"));
        members.add(createMember(null, Boolean.class, "boxedBoolean"));
        members.add(createMember(null, Byte.class, "boxedByte"));
        members.add(createMember(null, Short.class, "boxedShort"));
        members.add(createMember(null, Integer.class, "boxedInt"));
        members.add(createMember(null, Long.class, "boxedLong"));
        members.add(createMember(null, Character.class, "boxedChar"));
        members.add(createMember(null, Float.class, "boxedFloat"));
        members.add(createMember(null, Double.class, "boxedDouble"));
        members.add(createMember(null, boolean[].class, "booleanArray"));
        members.add(createMember(null, byte[].class, "byteArray"));
        members.add(createMember(null, short[].class, "shortArray"));
        members.add(createMember(null, int[].class, "intArray"));
        members.add(createMember(null, long[].class, "longArray"));
        members.add(createMember(null, char[].class, "charArray"));
        members.add(createMember(null, float[].class, "floatArray"));
        members.add(createMember(null, double[].class, "doubleArray"));
        members.add(createMember(null, Date.class, "date"));
        members.add(createMember(null, Bundle.class, "bundle"));
        members.add(createMember(null, Parcelable.class, "parcelable"));
        members.add(createMember(null, Serializable.class, "serializable"));

        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(0);
        TypeMirrorMock type = new TypeMirrorMock(List.class);
        String typeName = List.class.getName() + "<" + String.class.getName() + ">";
        members.add(new AnnotatedClass.Member(annotations, type, typeName, "list"));

        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();

        // execute
        ParcelPacker sut = new ParcelPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        sut.generateCode();
    }

    @Test
    public void testStubHandling() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        TypeMirror type = mock(TypeMirror.class);
        when(type.getKind()).thenReturn(TypeKind.ERROR);
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(0);
        members.add(new AnnotatedClass.Member(annotations, type, "Foo", "bar"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();

        // execute
        ParcelPacker sut = new ParcelPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        sut.generateCode();
    }

    @Test
    public void testParcelable() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);

        TypeMirror type = mock(TypeMirror.class);
        when(type.getKind()).thenReturn(TypeKind.DECLARED);
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(0);
        members.add(new AnnotatedClass.Member(annotations, type, "Foo", "bar"));

        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();

        when(environment.types.directSupertypes(any(TypeMirror.class))).thenAnswer(new Answer<List<TypeMirror>>() {
            @Override
            public List<TypeMirror> answer(InvocationOnMock invocation) throws Throwable {
                final List<TypeMirror> types = new ArrayList<>(1);
                types.add(new TypeMirrorMock(Parcelable.class));
                return types;
            }
        });

        // execute
        ParcelPacker sut = new ParcelPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        sut.generateCode();
    }

    @Test
    public void testSerializable() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);

        TypeMirror type = mock(TypeMirror.class);
        when(type.getKind()).thenReturn(TypeKind.DECLARED);
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>();
        members.add(new AnnotatedClass.Member(annotations, type, "Foo", "bar"));

        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();
        when(environment.types.directSupertypes(any(TypeMirror.class))).thenAnswer(new Answer<List<TypeMirror>>() {
            @Override
            public List<TypeMirror> answer(InvocationOnMock invocation) throws Throwable {
                final List<TypeMirror> types = new ArrayList<>(2);
                types.add(new TypeMirrorMock(String.class));
                types.add(new TypeMirrorMock(Serializable.class));
                return types;
            }
        });

        // execute
        ParcelPacker sut = new ParcelPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        sut.generateCode();
    }

    @Test(expected = RuntimeException.class)
    public void testUnsupported() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        members.add(createMember(null, int[][].class, "boom"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();

        // execute
        ParcelPacker sut = new ParcelPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        sut.generateCode();
    }

    private AnnotatedClass.Member createMember(Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations,
                                               Class<?> type, String name) {
        if(annotations == null) {
            annotations = new HashMap<>(0);
        }
        return new AnnotatedClass.Member(annotations, new TypeMirrorMock(type), type.getName(), name);
    }
}