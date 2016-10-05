package eu.rekisoft.java.pojotoolkit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import eu.rekisoft.java.pojotoolkit.testing.TypeMirrorMock;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created on 05.10.2016.
 *
 * @author René Kilczan
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ExecutableElement.class, VariableElement.class, TypeMirror.class})
public class MethodTest {
    @Test
    public void noArgTest() {
        // prepare
        ExecutableElement element = mock(ExecutableElement.class);
        TypeMirror type = new TypeMirrorMock(int.class);
        when(element.getReturnType()).thenReturn(type);
        when(element.getParameters()).thenAnswer(new Answer<List<VariableElement>>() {
            @Override
            public List<VariableElement> answer(InvocationOnMock invocation) throws Throwable {
                return new ArrayList<>(0);
            }
        });
        Method sut = Method.from(element);

        // verify
        assertTrue(sut.matchesTypes(int.class));
    }

    @Test
    public void multipleArgTest() {
        // prepare
        ExecutableElement element = mock(ExecutableElement.class);
        TypeMirror type = new TypeMirrorMock(int.class);
        when(element.getReturnType()).thenReturn(type);
        when(element.getParameters()).thenAnswer(new Answer<List<VariableElement>>() {
            @Override
            public List<VariableElement> answer(InvocationOnMock invocation) throws Throwable {
                List<VariableElement> args = new ArrayList<>(2);
                args.add(createMock(double.class));
                args.add(createMock(byte.class));
                return args;
            }
        });
        Method sut = Method.from(element);

        // verify
        assertTrue(sut.matchesTypes(int.class, double.class, byte.class));
    }

    @Test
    public void argCountMismatch() {
        // prepare
        ExecutableElement element = mock(ExecutableElement.class);
        TypeMirror type = new TypeMirrorMock(int.class);
        when(element.getReturnType()).thenReturn(type);
        when(element.getParameters()).thenAnswer(new Answer<List<VariableElement>>() {
            @Override
            public List<VariableElement> answer(InvocationOnMock invocation) throws Throwable {
                List<VariableElement> args = new ArrayList<>(2);
                args.add(createMock(double.class));
                return args;
            }
        });
        Method sut = Method.from(element);

        // verify
        assertFalse(sut.matchesTypes(int.class));
    }

    @Test
    public void typeMismatch() {
        // prepare
        ExecutableElement element = mock(ExecutableElement.class);
        TypeMirror type = new TypeMirrorMock(int.class);
        when(element.getReturnType()).thenReturn(type);
        when(element.getParameters()).thenAnswer(new Answer<List<VariableElement>>() {
            @Override
            public List<VariableElement> answer(InvocationOnMock invocation) throws Throwable {
                List<VariableElement> args = new ArrayList<>(2);
                args.add(createMock(double.class));
                return args;
            }
        });
        Method sut = Method.from(element);

        // verify
        assertFalse(sut.matchesTypes(int.class, int.class));
    }

    private VariableElement createMock(Class<?> clazz) {
        VariableElement element = mock(VariableElement.class);
        TypeMirror type = mock(TypeMirror.class);
        when(type.toString()).thenReturn(clazz.getName());
        when(element.asType()).thenReturn(type);
        return element;
    }
}