package eu.rekisoft.java.pojotoolkit.testing;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created on 19.09.2016.
 *
 * @author René Kilczan
 */
public class ProcessingEnvironmentMock implements ProcessingEnvironment {
    public final Map<String, String> options = new HashMap<>();
    public final Messager messager = mock(Messager.class);
    public final Filer filer = mock(Filer.class);
    public final ElementUtils elements = spy(new ElementUtils());
    public final Types types = mock(Types.class);
    public SourceVersion sourceVersion = SourceVersion.RELEASE_7;

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public Messager getMessager() {
        return messager;
    }

    @Override
    public Filer getFiler() {
        return filer;
    }

    @Override
    public Elements getElementUtils() {
        return elements;
    }

    @Override
    public Types getTypeUtils() {
        return types;
    }

    @Override
    public SourceVersion getSourceVersion() {
        return sourceVersion;
    }

    @Override
    public Locale getLocale() {
        return Locale.GERMANY;
    }

    public void mockTypeAsAssignable(final Class<?> type) {
        when(types.isAssignable(any(TypeMirror.class), any(TypeMirror.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                //TypeMirror t1 = (TypeMirror)invocation.getArguments()[0];
                TypeMirror t2 = (TypeMirror)invocation.getArguments()[1];
                return type.getName().equals(t2.toString());
            }
        });
        when(elements.getTypeElement(any(CharSequence.class))).thenAnswer(new Answer<TypeElement>() {
            @Override
            public TypeElement answer(InvocationOnMock invocation) throws Throwable {
                return new ElementMock((String)invocation.getArguments()[0], ElementKind.FIELD, TypeKind.DECLARED);
            }
        });
    }
}