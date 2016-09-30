package eu.rekisoft.java.pojobooster;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.JsonWriter;
import eu.rekisoft.java.pojotoolkit.SettingsFactory;
import eu.rekisoft.java.pojotoolkit.testing.ProcessingEnvironmentMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by René Kilczan on 29.09.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TypeKind.class)
public class JsonPackerTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testWithoutSpecialAnnotations() {
        // initialize
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(0);
        TypeMirror type = mock(TypeMirror.class);
        when(type.getKind()).thenReturn(TypeKind.DECLARED);
        members.add(new AnnotatedClass.Member(annotations, type, String.class.getName(), "string"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();
        when(environment.types.isAssignable(any(TypeMirror.class), any(TypeMirror.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                TypeMirror t1 = (TypeMirror)invocation.getArguments()[0];
                //TypeMirror t2 = (TypeMirror)invocation.getArguments()[1];
                return String.class.getName().equals(t1.toString());
            }
        });
        when(environment.elements.getTypeElement(any(CharSequence.class))).thenAnswer(new Answer<TypeElement>() {
            @Override
            public TypeElement answer(InvocationOnMock invocation) throws Throwable {
                return mock(TypeElement.class);
            }
        });
        //when(environment.types.getTypeElement(clazz.getName()).asType())
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));

        // verify
        assertEquals(1, sut.getAttentionalInterfaces().size());
        assertEquals(0, sut.generateMembers().size());
        List<MethodSpec> methods = sut.generateCode();
        assertEquals(1, methods.size());
        assertEquals(ClassName.get(JsonWriter.class), sut.getAttentionalInterfaces().get(0));
    }

    @Test
    public void testWithFormatAnnotation() {
        // initialize
        List<AnnotatedClass.Member> members = new ArrayList<>(1);
        Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations = new HashMap<>(2);
        Map<ExecutableElement, AnnotationValue> format = new HashMap<>(2);
        format.put(mockElement("value"), mockValue("%1.2f"));
        format.put(mockElement("locale"), mockValue("en_US"));
        annotations.put(Formatter.class.getName(), format);
        Map<ExecutableElement, AnnotationValue> field = new HashMap<>(1);
        field.put(mockElement("value"), mockValue("alias"));
        annotations.put(Field.class.getName(), field);
        TypeMirror type = mock(TypeMirror.class);
        when(type.getKind()).thenReturn(TypeKind.INT);
        members.add(new AnnotatedClass.Member(annotations, type, float.class.getName(), "number"));
        JsonPacker sut = new JsonPacker(SettingsFactory.create(members, null, null, "DEBUG", null, false));

        // verify
        assertEquals(1, sut.getAttentionalInterfaces().size());
        assertEquals(0, sut.generateMembers().size());
        List<MethodSpec> methods = sut.generateCode();
        assertEquals(1, methods.size());
        assertEquals(ClassName.get(JsonWriter.class), sut.getAttentionalInterfaces().get(0));
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