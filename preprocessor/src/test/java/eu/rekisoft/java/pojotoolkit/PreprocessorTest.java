package eu.rekisoft.java.pojotoolkit;

import android.support.annotation.NonNull;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import eu.rekisoft.java.pojobooster.Enhance;
import eu.rekisoft.java.pojobooster.FactoryOf;
import eu.rekisoft.java.pojobooster.JsonDecorator;
import eu.rekisoft.java.pojobooster.Serializer;
import eu.rekisoft.java.pojotoolkit.testing.AnnotationMirrorMock;
import eu.rekisoft.java.pojotoolkit.testing.ElementMock;
import eu.rekisoft.java.pojotoolkit.testing.ProcessingEnvironmentMock;
import eu.rekisoft.java.pojotoolkit.testing.RoundEnvironmentMock;
import eu.rekisoft.java.pojotoolkit.testing.TypeMirrorMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/**
 * Created on 18.09.2016.
 *
 * @author René Kilczan
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BufferedWriter.class, FileWriter.class, Preprocessor.class, Class.class})
public class PreprocessorTest {
    private Preprocessor sut;
    private ProcessingEnvironmentMock processingEnvironment;
    private RoundEnvironmentMock roundEnvironment;

    @Before
    public void setup() throws Exception {
        sut = new Preprocessor();
        // suppress file creation
        suppress(constructor(FileWriter.class, String.class));
        BufferedWriter writer = mock(BufferedWriter.class);
        whenNew(BufferedWriter.class).withAnyArguments().thenReturn(writer);
        File file = mock(File.class);
        whenNew(File.class).withAnyArguments().thenReturn(file);
        processingEnvironment = spy(new ProcessingEnvironmentMock());
        roundEnvironment = spy(new RoundEnvironmentMock());
    }

    @Test
    public void simpleTestWithTargetAndNothingToProcess() {
        processingEnvironment.options.put("target", "foo");
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);
    }

    @Test
    public void detectTargetPath() throws Exception {
        JavaFileObject jfo = mock(JavaFileObject.class);
        when(processingEnvironment.filer.createSourceFile(anyString())).thenReturn(jfo);
        Writer w = mock(Writer.class);
        when(jfo.openWriter()).thenReturn(w);
        when(jfo.toUri()).thenReturn(new URI("/foo/bar"));
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // TODO verify the correct path
    }

    @Test
    public void basicEnhanceAnnotationProcessing() {
        processingEnvironment.options.put("target", "foo");
        sut.init(processingEnvironment);
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[0]))));
        sut.process(null, roundEnvironment);
    }

    @Test
    public void copyConstructorTest() {
        processingEnvironment.options.put("target", "foo");
        processingEnvironment.elements.members.clear();
        ElementMock constructor = new ElementMock("<init>", ElementKind.CONSTRUCTOR, TypeKind.EXECUTABLE);
        processingEnvironment.elements.members.add(constructor);
        constructor.type.parameters.add(new TypeMirrorMock(int.class));
        constructor.type.parameters.add(new TypeMirrorMock(String.class));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass"))));
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);
    }

    @Test
    public void checkExtensionProcessing() {
        processingEnvironment.options.put("target", "foo");
        sut.init(processingEnvironment);
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[] {Serializer.class}))));
        sut.process(null, roundEnvironment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashForWrongAnnotationOnConstructor() {
        processingEnvironment.options.put("target", "foo");
        sut.init(processingEnvironment);
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("<init>", ElementKind.CONSTRUCTOR, TypeKind.EXECUTABLE)));
        sut.process(null, roundEnvironment);
    }

    @Test
    public void checkConstructorProcessing() {
        processingEnvironment.options.put("target", "foo");
        processingEnvironment.options.put("loglevel", "DEBUG");
        sut.init(processingEnvironment);
        Set<ElementMock> members = new HashSet<>(2);
        members.add(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[] {Serializer.class})));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(), members);
        processingEnvironment.elements.members.clear();
        processingEnvironment.elements.members.add(new ElementMock("<init>", ElementKind.CONSTRUCTOR, TypeKind.EXECUTABLE));
        processingEnvironment.elements.members.add(new ElementMock("test", ElementKind.METHOD, TypeKind.EXECUTABLE));
        sut.process(null, roundEnvironment);
    }

    @Test(expected = RuntimeException.class)
    public void testExceptionHandling() throws IOException {
        when(processingEnvironment.filer.createSourceFile(any())).thenThrow(new IOException("expected!"));
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);
    }

    @Test
    public void checkJsonDecoratorProcessing() {
        // prepare
        processingEnvironment.options.put("target", "foo");
        processingEnvironment.options.put("loglevel", "DEBUG");
        Set<Element> members = new HashSet<>(2);
        ExecutableElement element = mock(ExecutableElement.class);
        TypeMirror type = new TypeMirrorMock(int.class);
        when(element.toString()).thenReturn("Bad");
        when(element.getReturnType()).thenReturn(type);
        when(element.getParameters()).thenAnswer(new Answer<List<VariableElement>>() {
            @Override
            public List<VariableElement> answer(InvocationOnMock invocation) throws Throwable {
                return new ArrayList<>(0);
            }
        });
        members.add(element);
        element = mock(ExecutableElement.class);
        type = new TypeMirrorMock(String.class);
        when(element.getReturnType()).thenReturn(type);
        when(element.toString()).thenReturn("Good");
        when(element.getParameters()).thenAnswer(new Answer<List<VariableElement>>() {
            @Override
            public List<VariableElement> answer(InvocationOnMock invocation) throws Throwable {
                VariableElement element = mock(VariableElement.class);
                TypeMirror type = mock(TypeMirror.class);
                when(type.toString()).thenReturn(StringBuilder.class.getName());
                when(element.asType()).thenReturn(type);
                List<VariableElement> args = new ArrayList<>(1);
                args.add(element);
                return args;
            }
        });
        members.add(element);
        roundEnvironment.annotatedElements.put(JsonDecorator.class.getName(), members);
        processingEnvironment.elements.members.clear();

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        verify(processingEnvironment.messager, times(1)).printMessage(eq(Diagnostic.Kind.NOTE), eq("Yey Good this is fine!"));
        verify(processingEnvironment.messager, times(1)).printMessage(eq(Diagnostic.Kind.ERROR), eq("No! Bad has the wrong args!"));
    }

    @Test
    public void checkFactoryOfProcessing() {
        // prepare
        processingEnvironment.options.put("target", "foo");
        processingEnvironment.options.put("loglevel", "DEBUG");
        Set<Element> members = new HashSet<>(1);
        Element element = mock(Element.class);
        when(element.toString()).thenReturn("Mocked Element");
        when(element.getAnnotationMirrors()).thenAnswer(new Answer<List<AnnotationMirror>>() {
            @Override
            public List<AnnotationMirror> answer(InvocationOnMock invocation) throws Throwable {
                List<AnnotationMirror> list = new ArrayList<>(1);
                list.add(new AnnotationMirrorMock(FactoryOf.class, "value", "example"));
                return list;
            }
        });
        members.add(element);
        roundEnvironment.annotatedElements.put(FactoryOf.class.getName(), members);
        processingEnvironment.elements.members.clear();

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        //verify(System.out).println(eq("type of java.lang.String"));
    }

    @Test
    public void testBrokenExtensionHandling() throws ClassNotFoundException {
        PowerMockito.mockStatic(Class.class);
        BDDMockito.given(Class.forName(any())).willAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return Extension.class;
            }
        });
        processingEnvironment.options.put("target", "foo");
        Set<ElementMock> members = new HashSet<>(2);
        members.add(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[] {Serializer.class})));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(), members);

        sut.init(processingEnvironment);
        try {
            sut.process(null, roundEnvironment);
        } catch(RuntimeException e) {
            if(e.getCause() instanceof InstantiationException) {
                assertEquals("Cannot load extension eu.rekisoft.java.pojotoolkit.Extension", e.getCause().getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    public void testExtensionInit() throws ClassNotFoundException {
        PowerMockito.mockStatic(Class.class);
        BDDMockito.given(Class.forName(any())).willAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                return MockExtension.class;
            }
        });
        processingEnvironment.options.put("target", "foo");
        processingEnvironment.options.put("step", "stub");
        Set<ElementMock> members = new HashSet<>(2);
        members.add(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[] {MockExtension.class})));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(), members);

        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);
    }

    private Set<? extends Element> createSet(ElementMock... elementMocks) {
        Set<ElementMock> set = new HashSet<>(elementMocks.length);
        Collections.addAll(set, elementMocks);
        return set;
    }

    public TypeMirror mockTypeMirror(TypeKind kind) {
        TypeMirror mirror = mock(TypeMirror.class);
        when(mirror.getKind()).thenReturn(kind);
        return mirror;
    }

    private static class MockExtension extends Extension {
        public MockExtension(@NonNull ExtensionSettings settings) {
            super(settings);
        }

        @NonNull
        @Override
        public List<TypeName> getAttentionalInterfaces() {
            return new ArrayList<>(0);
        }

        @NonNull
        @Override
        public List<FieldSpec> generateMembers() {
            return new ArrayList<>(0);
        }

        @NonNull
        @Override
        public List<MethodSpec> generateCode() {
            return new ArrayList<>(0);
        }
    }
}