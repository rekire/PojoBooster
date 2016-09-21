package eu.rekisoft.java.pojotoolkit;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import eu.rekisoft.java.pojobooster.Enhance;
import eu.rekisoft.java.pojobooster.Serializer;
import eu.rekisoft.java.pojotoolkit.testing.AnnotationMirrorMock;
import eu.rekisoft.java.pojotoolkit.testing.ElementMock;
import eu.rekisoft.java.pojotoolkit.testing.ProcessingEnvironmentMock;
import eu.rekisoft.java.pojotoolkit.testing.RoundEnvironmentMock;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created on 18.09.2016.
 *
 * @author René Kilczan
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BufferedWriter.class)
public class PreprocessorTest {
    private Preprocessor sut;
    private ProcessingEnvironmentMock processingEnvironment;
    private RoundEnvironmentMock roundEnvironment;

    @Before
    public void setup() {
        sut = new Preprocessor() {
            @Override
            protected void writeFile(String dir, ClassName targetType, JavaFile fileContent) {
            }
        };
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
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, new AnnotationMirrorMock(Enhance.class,
                        "name", "TargetClass", "extensions", new Class[0]))));
        sut.process(null, roundEnvironment);
    }

    @Test
    public void checkExtensionProcessing() {
        processingEnvironment.options.put("target", "foo");
        sut.init(processingEnvironment);
        roundEnvironment.annotatedElements.put(Enhance.class.getName(),
                createSet(new ElementMock("some.source.Class", ElementKind.CLASS, new AnnotationMirrorMock(Enhance.class,
                        "name", "TargetClass", "extensions", new Class[] {Serializer.class}))));
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
}