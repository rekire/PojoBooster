package eu.rekisoft.java.pojotoolkit;

import android.support.annotation.NonNull;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import eu.rekisoft.java.pojotoolkit.testing.ProcessingEnvironmentMock;
import eu.rekisoft.java.pojotoolkit.testing.TypeMirrorMock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by René Kilczan on 06.10.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TypeKind.class)
public class ExtensionTest {
    @Test
    public void basicTest() {
        List<AnnotatedClass.Member> members = new ArrayList<>(3);
        members.add(createMember(null, String.class, "string"));
        ProcessingEnvironmentMock environment = new ProcessingEnvironmentMock();

        final Flags flags = new Flags();
        final ExtensionSettings settings = SettingsFactory.create(members, null, environment, "DEBUG", "release", false);

        // execute
        Extension sut = new Extension(settings) {
            @NonNull
            @Override
            public List<TypeName> getAttentionalInterfaces() {
                flags.interfaces = true;
                assertEquals("DEBUG", getLogLevel());
                assertEquals(false, isCreatingStub());
                assertEquals("release", getVariantName());
                assertEquals(settings.annotatedClass, this.annotatedClass);
                assertEquals(settings.processingEnv.getTypeUtils(), getTypeHelper());
                Class<UUID> uuidClass = UUID.class;
                return new ArrayList<>(0);
            }

            @NonNull
            @Override
            public List<FieldSpec> generateMembers() {
                flags.members = true;
                return new ArrayList<>(0);
            }

            @NonNull
            @Override
            public List<MethodSpec> generateCode() {
                flags.generatedCode = true;
                return new ArrayList<>(0);
            }
        };
        sut.getAttentionalInterfaces();
        assertTrue(flags.interfaces);
        sut.generateMembers();
        assertTrue(flags.members);
        sut.generateCode();
        assertTrue(flags.generatedCode);

    }

    private AnnotatedClass.Member createMember(Map<String, Map<? extends ExecutableElement, ? extends AnnotationValue>> annotations,
                                               Class<?> type, String name) {
        if(annotations == null) {
            annotations = new HashMap<>(0);
        }
        return new AnnotatedClass.Member(annotations, new TypeMirrorMock(type), type.getName(), name);
    }

    private static class Flags {
        public boolean interfaces;
        public boolean members;
        public boolean generatedCode;
    }
}