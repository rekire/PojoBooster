package eu.rekisoft.java.pojotoolkit.testing;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

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
}