package eu.rekisoft.java.pojobooster;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.Extension;
import eu.rekisoft.java.pojotoolkit.ExtensionSettings;
import eu.rekisoft.java.pojotoolkit.JsonWriter;
import eu.rekisoft.java.pojotoolkit.Method;
import eu.rekisoft.java.pojotoolkit.SettingsFactory;
import eu.rekisoft.java.pojotoolkit.testing.ProcessingEnvironmentMock;
import eu.rekisoft.java.pojotoolkit.testing.TypeMirrorMock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by René Kilczan on 29.09.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TypeKind.class)
public class JsonPackerTest {
    @Test
    public void testWithoutSpecialAnnotations() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(3);
        members.add(createMember(null, String.class, "string"));
        members.add(createMember(null, int.class, "number"));
        members.add(createMember(null, char.class, "character"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();
        environment.mockTypeAsAssignable(String.class);

        // execute
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, sut.getAttentionalInterfaces().size());
        assertEquals(0, sut.generateMembers().size());
        assertEquals(1, methods.size());
        assertEquals(ClassName.get(JsonWriter.class), sut.getAttentionalInterfaces().get(0));
    }

    @Test
    public void testWithFormatAnnotation() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(2);
        Map<ExecutableElement, AnnotationValue> format = new HashMap<>(2);
        format.put(mockElement("value"), mockValue("%1.2f"));
        format.put(mockElement("locale"), mockValue("en_US"));
        annotations.put(Formatter.class.getName(), format);
        Map<ExecutableElement, AnnotationValue> field = new HashMap<>(1);
        field.put(mockElement("value"), mockValue("alias"));
        annotations.put(Field.class.getName(), field);
        members.add(createMember(annotations, float.class, "number"));
        Map<ExecutableElement, AnnotationValue> emptyAlias = new HashMap<>(1);
        emptyAlias.put(mockElement(""), mockValue("alias"));
        annotations.put(Field.class.getName(), emptyAlias);
        members.add(createMember(annotations, boolean.class, "noAlias"));

        // execute
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, null, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, sut.getAttentionalInterfaces().size());
        assertEquals(0, sut.generateMembers().size());
        assertEquals(1, methods.size());
        assertEquals(ClassName.get(JsonWriter.class), sut.getAttentionalInterfaces().get(0));
    }

    @Test
    public void testArrayHandling() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        members.add(createMember(null, int[].class, "array"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();

        // execute
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, methods.size());
        assertTrue(methods.toString().contains("sb.append(array[arrayIndex]);"));
    }

    @Test
    public void testListHandling() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        members.add(createMember(null, List.class, "list"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();
        environment.mockTypeAsAssignable(List.class);

        // execute
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, methods.size());
        assertTrue(methods.toString().contains("sb.append(list.get(listIndex));"));
    }

    @Test
    public void testJsonWriterHandling() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        members.add(createMember(null, JsonWriter.class, "json"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();
        environment.mockTypeAsAssignable(JsonWriter.class);

        // execute
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, methods.size());
        assertTrue(methods.toString().contains("sb.append(json.toJson());"));
    }

    @Test
    public void testNumberAndBooleanHandling() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(4);
        members.add(createMember(null, Boolean.class, "nullableBoolean"));
        members.add(createMember(null, Double.class, "nullableDouble"));
        members.add(createMember(null, Integer.class, "nullableInteger"));
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(1);
        Map<ExecutableElement, AnnotationValue> format = new HashMap<>(1);
        format.put(mockElement("value"), mockValue("%1.2f"));
        annotations.put(Formatter.class.getName(), format);
        members.add(createMember(annotations, Float.class, "nullableFloat"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();
        environment.mockTypeAsAssignable(Number.class);

        // execute
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, methods.size());
        String method = methods.get(0).toString();
        assertTrue(method.contains("sb.append(nullableBoolean);"));
        assertTrue(method.contains("sb.append(nullableDouble);"));
        assertTrue(method.contains("sb.append(nullableInteger);"));
        assertTrue(method.contains("sb.append(java.lang.String.format(java.util.Locale.getDefault(), \"%1.2f\", nullableFloat));"));
    }

    @Test
    public void testDateHandling() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(1);
        Map<ExecutableElement, AnnotationValue> format = new HashMap<>(1);
        format.put(mockElement("value"), mockValue("MMMMM"));
        annotations.put(Formatter.class.getName(), format);
        members.add(createMember(annotations, Float.class, "month"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();
        environment.mockTypeAsAssignable(Date.class);

        // execute
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, methods.size());
        String method = methods.get(0).toString();
        assertTrue(method.contains("java.text.SimpleDateFormat monthFormatter = new java.text.SimpleDateFormat(\"MMMMM\", java.util.Locale.getDefault());"));
        assertTrue(method.contains("sb.append(monthFormatter.format(month));"));
    }

    @Test
    public void testUnknownClassHandling() {
        // setup
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        members.add(createMember(null, UUID.class, "id"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();
        environment.mockTypeAsAssignable(Object.class); // nonsense we just need to mock some methods

        // execute
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, methods.size());
        String method = methods.get(0).toString();
        assertTrue(method.contains("sb.append('\"').append(id.toString()).append('\"');"));
    }

    @Test
    public void verifyDateProcessing() throws Exception {
        JsonPacker sut = spy(new JsonPacker(mock(ExtensionSettings.class)) {
            @Override
            protected boolean isInstanceOf(AnnotatedClass.Member member, Class<?> clazz) {
                return false;
            }
        });
        java.lang.reflect.Method addElementToJson = Whitebox.getMethod(JsonPacker.class, "addElementToJson", MethodSpec.Builder.class, AnnotatedClass.Member.class);
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("toJson")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);
        AnnotatedClass.Member member = new AnnotatedClass.Member(
                new HashMap<String, Map<? extends ExecutableElement, ? extends AnnotationValue>>(),
                new TypeMirrorMock(String.class), "typeName", "name") {
            @Override
            public Object getAnnotatedProperty(Class<? extends Annotation> type, String field) {
                return "";
            }
        };
        addElementToJson.invoke(sut, method, member);
        verifyPrivate(sut, times(1)).invoke("isInstanceOf", eq(member), eq(Date.class));
    }

    @Test
    public void blah() throws Exception {
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>();
        AnnotatedClass.Member member = new AnnotatedClass.Member(annotations, new TypeMirrorMock(boolean.class), null, null) {
            int run = 1;
            @Override
            public Object getAnnotatedProperty(Class<? extends Annotation> type, String field) {
                switch(run++) {
                case 1:
                    return "";
                case 2:
                    return "x";
                default:
                    return null;
                }
            }
        };
        AnnotatedClass clazz = mock(AnnotatedClass.class);
        Whitebox.setInternalState(clazz, "members", Collections.singletonList(member));
        ExtensionSettings settings = mock(ExtensionSettings.class);
        Whitebox.setInternalState(settings, "annotatedClass", clazz);
        JsonPacker sut = spy(new JsonPacker(settings));
        sut.generateCode();
        sut.generateCode();
        sut.generateCode();
        //verifyPrivate(sut, times(1)).invoke("addElementToJson", any(), eq(member));
    }

    @Test
    public void isDirectEmbeddableTest() throws InvocationTargetException, IllegalAccessException {
        AnnotatedClass.Member number = new AnnotatedClass.Member(
                new HashMap<String, Map<? extends ExecutableElement, ? extends AnnotationValue>>(),
                new TypeMirrorMock(Number.class), "typeName", "name");
        AnnotatedClass.Member bool = new AnnotatedClass.Member(
                new HashMap<String, Map<? extends ExecutableElement, ? extends AnnotationValue>>(),
                new TypeMirrorMock(Boolean.class), "typeName", "name");
        ExtensionSettings settings = mock(ExtensionSettings.class);
        JsonPacker sut = spy(new JsonPacker(settings) {
            @Override
            protected boolean isInstanceOf(AnnotatedClass.Member member, Class<?> clazz) {
                return member.type.toString().equals(clazz.getName());
            }
        });
        java.lang.reflect.Method isDirectEmbeddable = Whitebox.getMethod(JsonPacker.class, "isDirectEmbeddable", AnnotatedClass.Member.class);
        assertTrue((boolean)isDirectEmbeddable.invoke(sut, number));
        assertTrue((boolean)isDirectEmbeddable.invoke(sut, bool));
    }

    private AnnotatedClass.Member createMember(Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations,
                                               Class<?> type, String name) {
        if(annotations == null) {
            annotations = new HashMap<>(0);
        }
        return new AnnotatedClass.Member(annotations, new TypeMirrorMock(type), type.getName(), name);
    }

    private ExecutableElement mockElement(String toString) {
        ExecutableElement mock = mock(ExecutableElement.class);
        Name name = mock(Name.class);
        when(name.toString()).thenReturn(toString);
        when(mock.getSimpleName()).thenReturn(name);
        return mock;
    }

    private AnnotationValue mockValue(String value) {
        AnnotationValue mock = mock(AnnotationValue.class);
        when(mock.getValue()).thenReturn(value);
        return mock;
    }
}