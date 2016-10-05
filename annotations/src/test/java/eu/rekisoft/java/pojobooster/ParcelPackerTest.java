package eu.rekisoft.java.pojobooster;

import android.os.Parcelable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

import eu.rekisoft.java.pojotoolkit.AnnotatedClass;
import eu.rekisoft.java.pojotoolkit.SettingsFactory;
import eu.rekisoft.java.pojotoolkit.testing.ProcessingEnvironmentMock;
import eu.rekisoft.java.pojotoolkit.testing.TypeMirrorMock;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
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
        environment.mockTypeAsAssignable(String.class);

        // execute
        ParcelPacker sut = new ParcelPacker(SettingsFactory.create(members, null, environment, "DEBUG", null, false));
        List<MethodSpec> methods = sut.generateCode();

        // verify
        assertEquals(1, sut.getAttentionalInterfaces().size());
        List<FieldSpec> generatedMembers = sut.generateMembers();
        assertEquals(1, generatedMembers.size());
        assertEquals("CREATOR", generatedMembers.get(0).name);
        assertEquals(3, methods.size());
        assertEquals(1, methods.get(0).modifiers.size());
        // @Override public int describeContents()
        assertEquals(1, methods.get(0).annotations.size());
        assertEquals(ClassName.get(Override.class), methods.get(0).annotations.get(0).type);
        assertEquals(0, methods.get(0).annotations.get(0).members.size());
        assertTrue(methods.get(0).modifiers.contains(Modifier.PUBLIC));
        assertEquals(TypeName.INT, methods.get(0).returnType);
        assertEquals("describeContents", methods.get(0).name);
        assertEquals(0, methods.get(0).parameters.size());
        // @Override public int writeToParcel(Parcel dest, int flags)
        assertEquals(ClassName.get(Parcelable.class), sut.getAttentionalInterfaces().get(0));
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