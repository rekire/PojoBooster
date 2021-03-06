package eu.rekisoft.java.pojotoolkit;

import android.support.annotation.NonNull;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.progress.HandyReturnValues;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
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
    private BufferedWriter writer;

    @Before
    public void setup() throws Exception {
        sut = new Preprocessor();
        // suppress file creation
        suppress(constructor(FileWriter.class, String.class));
        writer = mock(BufferedWriter.class);
        whenNew(BufferedWriter.class).withAnyArguments().thenReturn(writer);
        File file = mock(File.class);
        whenNew(File.class).withAnyArguments().thenReturn(file);
        processingEnvironment = spy(new ProcessingEnvironmentMock());
        roundEnvironment = spy(new RoundEnvironmentMock());
    }

    @Test
    public void simpleTestWithTargetAndNothingToProcess() {
        // prepare
        processingEnvironment.options.put("target", "foo");

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        assertNull(Whitebox.getInternalState(sut, "sourcePath"));
        assertEquals("foo", Whitebox.getInternalState(sut, "targetPath"));
    }

    @Test
    public void detectTargetPath() throws Exception {
        // prepare
        JavaFileObject jfo = mock(JavaFileObject.class);
        when(processingEnvironment.filer.createSourceFile(anyString())).thenReturn(jfo);
        Writer w = mock(Writer.class);
        when(jfo.openWriter()).thenReturn(w);
        when(jfo.toUri()).thenReturn(new URI("/foo/bar"));

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        assertEquals("/foo/bar", Whitebox.getInternalState(sut, "sourcePath"));
    }

    @Test
    public void basicEnhanceAnnotationProcessing() throws Exception {
        // prepare
        processingEnvironment.options.put("target", "foo");
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[0]))));
        sut = spy(sut);

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        final String outfile = "package some.source;\n" +
                "\n" +
                "public class TargetClass extends Class {\n" +
                "    public TargetClass() {\n" +
                "        super();\n" +
                "    }\n" +
                "}\n";
        ArgumentCaptor<JavaFile> fileArg = ArgumentCaptor.forClass(JavaFile.class);
        verifyPrivate(sut, times(1)).invoke("writeFile", eqPath("foo/some/source"), any(), fileArg.capture());
        assertEquals(outfile, fileArg.getValue().toString());
    }

    @Test
    public void copyConstructorTest() throws Exception {
        // prepare
        processingEnvironment.options.put("target", "foo");
        processingEnvironment.elements.members.clear();
        ElementMock constructor = new ElementMock("<init>", ElementKind.CONSTRUCTOR, TypeKind.EXECUTABLE);
        processingEnvironment.elements.members.add(constructor);
        constructor.type.parameters.add(new TypeMirrorMock(int.class));
        constructor.type.parameters.add(new TypeMirrorMock(String.class));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass"))));
        sut = spy(sut);

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        ArgumentCaptor<JavaFile> fileArg = ArgumentCaptor.forClass(JavaFile.class);
        verifyPrivate(sut, times(1)).invoke("writeFile", eqPath("foo/some/source"), any(), fileArg.capture());
        assertEquals(1, fileArg.getValue().typeSpec.methodSpecs.size());
        MethodSpec method = fileArg.getValue().typeSpec.methodSpecs.get(0);
        assertTrue(method.isConstructor());
        assertEquals(2, method.parameters.size());
        assertEquals(TypeName.INT, method.parameters.get(0).type);
        assertEquals(ClassName.get(String.class), method.parameters.get(1).type);
    }

    @Test
    public void crashForWrongAnnotationOnConstructor() {
        // prepare
        processingEnvironment.options.put("target", "foo");
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("<init>", ElementKind.CONSTRUCTOR, TypeKind.EXECUTABLE)));

        // execute
        sut.init(processingEnvironment);
        try {
            sut.process(null, roundEnvironment);
            // verify
            fail();
        } catch(IllegalArgumentException e) {
            assertEquals("No class was annotated", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void checkConstructorProcessing() throws Exception {
        // prepare
        processingEnvironment.options.put("target", "foo");
        processingEnvironment.options.put("loglevel", "DEBUG");
        Set<ElementMock> members = new HashSet<>(2);
        members.add(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[] {Serializer.class})));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(), members);
        processingEnvironment.elements.members.clear();
        processingEnvironment.elements.members.add(new ElementMock("<init>", ElementKind.CONSTRUCTOR, TypeKind.EXECUTABLE));
        processingEnvironment.elements.members.add(new ElementMock("test", ElementKind.METHOD, TypeKind.EXECUTABLE));
        sut = spy(sut);

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        ArgumentCaptor<List<Element>> methodArg = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<List<Element>> constructorArg = ArgumentCaptor.forClass((Class) List.class);
        verifyPrivate(sut, times(1)).invoke("collectInfo", any(), any(), any(), methodArg.capture(), constructorArg.capture());
        assertEquals(1, methodArg.getValue().size());
        assertEquals(ElementKind.METHOD, methodArg.getValue().get(0).getKind());
        assertEquals("test", methodArg.getValue().get(0).toString());
        assertEquals(1, constructorArg.getValue().size());
        assertEquals(ElementKind.CONSTRUCTOR, constructorArg.getValue().get(0).getKind());
    }

    @Test
    public void testExceptionHandling() throws IOException {
        // prepare
        when(processingEnvironment.filer.createSourceFile(any())).thenThrow(new IOException("expected!"));

        // execute
        sut.init(processingEnvironment);
        try {
            sut.process(null, roundEnvironment);
            // verify
            fail();
        } catch(RuntimeException e) {
            assertEquals("Cannot find the target directory", e.getMessage());
            assertEquals("expected!", e.getCause().getMessage());
        }
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
        // TODO
        //verify(System.out).println(eq("type of java.lang.String"));
    }

    @Test
    public void testBrokenExtensionHandling() throws ClassNotFoundException {
        // prepare
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

        // execute
        sut.init(processingEnvironment);
        try {
            sut.process(null, roundEnvironment);
            // verify
            fail();
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
        // prepare
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

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        // TODO

        // prepare
        processingEnvironment.options.remove("step");

        // execute
        sut = new Preprocessor();
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        // TODO
    }

    @Test
    public void specialPathHandling() throws Exception {
        // prepare
        JavaFileObject jfo = mock(JavaFileObject.class);
        when(processingEnvironment.filer.createSourceFile(anyString())).thenReturn(jfo);
        Writer w = mock(Writer.class);
        when(jfo.openWriter()).thenReturn(w);
        when(jfo.toUri()).thenReturn(new URI("/tmp/test/"));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[0]))));
        sut = spy(sut);

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        verifyPrivate(sut, times(1)).invoke("writeFile", eqPath("/generated/source/pojo/some/source"), any(), any());
    }

    @Test
    public void specialPathHandling2() throws Exception {
        // prepare
        JavaFileObject jfo = mock(JavaFileObject.class);
        when(processingEnvironment.filer.createSourceFile(anyString())).thenReturn(jfo);
        Writer w = mock(Writer.class);
        when(jfo.openWriter()).thenReturn(w);
        when(jfo.toUri()).thenReturn(new URI("/tmp/build/classes/foo/"));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[0]))));
        sut = spy(sut);

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        verifyPrivate(sut, times(1)).invoke("writeFile", eqPath("/tmp/src/generated/some/source"), any(), any());
    }

    @Test
    public void specialPathHandling3() throws Exception {
        // prepare
        JavaFileObject jfo = mock(JavaFileObject.class);
        when(processingEnvironment.filer.createSourceFile(anyString())).thenReturn(jfo);
        Writer w = mock(Writer.class);
        when(jfo.openWriter()).thenReturn(w);
        when(jfo.toUri()).thenReturn(new URI("/tmp/build/foo/"));
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[0]))));
        sut = spy(sut);

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        verifyPrivate(sut, times(1)).invoke("writeFile", eqPath("/tmp/generated/source/pojo/some/source"), any(), any());
    }

    @Test
    public void testFileWriteExceptions() throws IOException {
        // prepare
        processingEnvironment.options.put("target", "foo");
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, TypeKind.DECLARED,
                        new AnnotationMirrorMock(Enhance.class, "name", "TargetClass", "extensions", new Class[0]))));
        Exception exception = spy(new IOException("Expected Crash!"));
        doThrow(exception).when(writer).close();

        // execute
        sut.init(processingEnvironment);
        sut.process(null, roundEnvironment);

        // verify
        verify(exception, times(1)).printStackTrace();

        // prepare
        exception = spy(new StringIndexOutOfBoundsException("Expected Crash!"));
        doThrow(exception).when(writer).close();

        // execute
        try {
            sut.process(null, roundEnvironment);
            // verify
            fail();
        } catch(RuntimeException e) {
            assertEquals("Expected Crash!", e.getCause().getMessage());
        }
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

    public static String eqPath(String value) {
        return PathEquals.reportMatcher(new PathEquals(value)).returnFor(value);
    }

    private static class PathEquals extends Equals {
        static final MockingProgress MOCKING_PROGRESS = new ThreadSafeMockingProgress();

        PathEquals(String wanted) {
            super(wanted);
        }

        static HandyReturnValues reportMatcher(Matcher<?> matcher) {
            return MOCKING_PROGRESS.getArgumentMatcherStorage().reportMatcher(matcher);
        }

        @Override
        public boolean matches(Object actual) {
            // This will change the Windows path style to the Linux style. On Linux this method just pass the value.
            return super.matches(((String)actual).replace(File.separator, "/"));
        }
    }

    private static class MockExtension extends Extension {
        public MockExtension(@NonNull ExtensionSettings settings) {
            super(settings);
        }

        @NonNull
        @Override
        public List<TypeName> getAttentionalInterfaces() {
            List<TypeName> interfaces = new ArrayList<>(1);
            interfaces.add(ClassName.get(Serializable.class));
            return interfaces;
        }

        @NonNull
        @Override
        public List<FieldSpec> generateMembers() {
            List<FieldSpec> members = new ArrayList<>(1);
            members.add(FieldSpec.builder(TypeName.INT, "number").build());
            return members;
        }

        @NonNull
        @Override
        public List<MethodSpec> generateCode() {
            return new ArrayList<>(0);
        }
    }
}