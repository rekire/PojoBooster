package eu.rekisoft.groovy.pojobooster

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.CompositeDomainObjectSet
import org.gradle.api.internal.artifacts.DefaultDependencySet
import org.gradle.api.internal.artifacts.configurations.DefaultConfiguration
import org.gradle.api.internal.file.AbstractFileCollection
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.tooling.BuildException
import org.gradle.util.GUtil
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

import java.util.zip.CRC32
import java.util.zip.ZipFile

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.junit.Assert.*
import static org.mockito.Matchers.matches
import static org.powermock.api.mockito.PowerMockito.*

/**
 * Created on 08.10.2016.
 *
 * @author René Kilczan
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = [PojoBoosterPlugin.class, GUtil.class, AbstractFileCollection.class])
public class PojoBoosterPluginTest {
    private Project project
    private PojoBoosterPlugin plugin
    private DefaultDependencySet dependencySet

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");

        project = ProjectBuilder.builder().build()

        Configuration pojobooster = mock(DefaultConfiguration.class)
        when(pojobooster.getName()).thenReturn("pojobooster")
        dependencySet = new MockDependencies()
        when(pojobooster.getDependencies()).thenReturn(dependencySet)
        when(pojobooster.iterator()).thenReturn(new ArrayList<File>(0).iterator())
        project.configurations.add(pojobooster)

        project.ext.libVersion = '0.0.0' // This value needs to be updated on every release

        plugin = new PojoBoosterPlugin() {
            @Override
            public Object getProperty(String property) {
                if('javaCompile') {
                    return new MockJavaCompile()
                }
                return super.getProperty(property)
            }
        }
    }

    @Test
    public void checkConfigurationsAndDependencies() {
        plugin.apply(project)
        def configurations = project.configurations.asList()
        assertEquals(2, configurations.size())
        for(DefaultConfiguration config : configurations) {
            def dependencies = config.dependencies
            switch(config.name) {
            case 'pojobooster':
                assertEquals(3, dependencies.size())
                assertTrue(config.dependencies.contains(project.dependencies.create("eu.rekisoft.pojobooster:preprocessor:$project.ext.libVersion")))
                assertTrue(config.dependencies.contains(project.dependencies.create("com.squareup:javapoet:1.7.0")))
                assertTrue(config.dependencies.contains(project.dependencies.create("org.robolectric:android-all:6.0.0_r1-robolectric-0")))
                break;
            case 'provided':
                assertEquals(1, dependencies.size())
                assertTrue(config.dependencies.contains(project.dependencies.create("eu.rekisoft.pojobooster:annotations:$project.ext.libVersion")))
                break;
            default:
                throw new RuntimeException("Unexpected config")
            }
        }
        try {
            project.evaluate()
            fail()
        } catch(ProjectConfigurationException e) {
            assertEquals(BuildException.class, e.cause.class)
            assertEquals('The project isn\'t a Java or Android project', e.cause.message)
        }
    }

    @Test
    public void testDetectionCrash() {
        plugin.apply(project)
        try {
            plugin.applyToAndroidProject(project)
            fail()
        } catch(BuildException e) {
            assertEquals('Something went wrong in the detection.', e.message)
        }
    }

    @Test
    public void testAndroidAppProject() {
        testAndroidProject('com.android.application')
    }

    @Test
    public void testAndroidLibraryProject() {
        testAndroidProject('com.android.library')
    }

    private void testAndroidProject(String pluginName) {
        // prepare
        AndroidPluginMock androidPlugin = mock(AndroidPluginMock.class)
        project = spy(project)
        def plugins = spy(project.plugins)
        when(project.plugins).thenReturn(plugins)
        when(plugins.findPlugin(matches(pluginName))).thenReturn(androidPlugin)
        when(plugins.hasPlugin(matches(pluginName))).thenReturn(true)
        when(androidPlugin.extension).thenReturn(new ExtensionMock())
        project.plugins.add(androidPlugin)
        Task base = project.task("generateFoobarSources") {}

        // execute
        plugin.apply(project)
        project.evaluate()

        // verify
        assertEquals('generateFoobarPojoBoosterClasses', base.finalizedBy.values[0].toString())
        assertNotNull(project.tasks['generateFoobarPojoBoosterStubs'])
        assertNotNull(project.tasks['generateFoobarPojoBoosterClasses'])
    }

    @Test
    public void testJavaProject() {
        // prepare
        AndroidPluginMock javaPlugin = mock(AndroidPluginMock.class)
        project = spy(project)
        def plugins = spy(project.plugins)
        when(project.plugins).thenReturn(plugins)
        when(plugins.findPlugin(matches('java'))).thenReturn(javaPlugin)
        when(plugins.hasPlugin(matches('java'))).thenReturn(true)
        ExtensionMock extension = new ExtensionMock()
        when(javaPlugin.extension).thenReturn(extension)
        project.plugins.add(javaPlugin)
        //when(project.getAt(matches("sourceSets"))).thenReturn(extension.sourceSets)
        Task base = project.task("compileJava") {}
        project.extensions.create 'sourceSets', JavaSourceSetMock

        // execute
        plugin.apply(project)
        project.evaluate()

        // verify
        assertEquals('generatePojoBoosterClasses', base.dependsOn[0].toString())
        assertNotNull(project.tasks['generatePojoBoosterStubs'])
        assertNotNull(project.tasks['generatePojoBoosterClasses'])
    }

    @Test
    public void compileExampleProjects() throws IOException {
        // prepare
        final String jar = ':examples:java:jar'
        final String app = ':examples:app:assembleDebug'
        final String lib = ':examples:library:assembleDebug'
        final String cleanJar = ':examples:java:clean'
        final String cleanApp = ':examples:app:clean'
        final String cleanLib = ':examples:library:clean'
        final File projectRoot = new File(System.getProperty("user.dir")).getParentFile()
        final File jarOutput = new File(projectRoot, "examples/java/build/libs/java.jar")
        final File appOutput = new File(projectRoot, "examples/app/build/outputs/apk/app-debug.apk")
        final File libOutput = new File(projectRoot, "examples/library/build/outputs/aar/library-debug.aar")
        jarOutput.delete()
        appOutput.delete()
        libOutput.delete()

        // execute
        def result = GradleRunner.create()
                .withProjectDir(projectRoot)
                .withArguments(cleanJar, jar, cleanApp, app, cleanLib, lib)
                .build();

        // verify
        assertEquals(SUCCESS, result.task(jar).getOutcome());
        assertEquals(SUCCESS, result.task(app).getOutcome());
        assertEquals(SUCCESS, result.task(lib).getOutcome());

        assertTrue("Java project output not found", jarOutput.exists())
        assertTrue("Android app project output not found", appOutput.exists())
        assertTrue("Android library project output not found", libOutput.exists())

        ZipFile jarArchive = new ZipFile(jarOutput)
        ZipFile appArchive = new ZipFile(appOutput)
        ZipFile libArchive = new ZipFile(libOutput)
        //assertTrue(jarArchive.size() > 2)
        assertTrue(appArchive.size() > 3)
        assertTrue(libArchive.size() > 3)


        File libClasses = new File(projectRoot, "examples/library/build/intermediates/bundles/debug/classes.jar")
        assertEquals(crcFile(libClasses), libArchive.getEntry("classes.jar").crc)
    }

    private static long crcFile(File file) {
        InputStream inputStream = file.newInputStream()
        CRC32 hash = new CRC32()
        byte[] buffer = new byte[8192]
        int bytesRead
        while((bytesRead = inputStream.read(buffer)) != -1) {
            hash.update(buffer, 0, bytesRead)
        }
        return hash.value
    }

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null
        try {
            output = new BufferedWriter(new FileWriter(destination))
            output.write(content)
        } finally {
            if(output != null) {
                output.close()
            }
        }
    }

    public static void all(Closure closure) {
        closure.call(new VariantMock())
    }

    private static abstract class AndroidPluginMock implements Plugin<Project> {
        public ExtensionMock extension = new ExtensionMock()
    }

    private static class ExtensionMock {
        public final List<VariantMock> applicationVariants
        public final List<VariantMock> libraryVariants
        public final Map<String, Map<String, Map<String, String>>> sourceSets

        ExtensionMock() {
            applicationVariants = new AllList<>(1)
            applicationVariants.add(new VariantMock())
            libraryVariants = new AllList<>(1)
            libraryVariants.add(new VariantMock())
            sourceSets = new HashMap<>(1)
            Map<String, String> java = new HashMap<>(1)
            java.put("srcDirs", "nothing")
            Map<String, Map<String, String>> sourceSet = new HashMap<>(1)
            sourceSet.put("java", java)
            sourceSets.put("release", sourceSet)
            sourceSets.put("main", sourceSet)
        }
    }

    private static class VariantMock {
        String name = "foobar"
        String dirName = "release"
        String release = "what"
    }

    private static class AllList extends ArrayList<VariantMock> {
        AllList(int i) {
            super(i);
        }

        public List<VariantMock> all() {
            return this;
        }
    }

    static class MockJavaCompile {
        List<String> source = new ArrayList<>()
    }

    private static class MockDependencies extends DefaultDependencySet {
        List<Dependency> set = new ArrayList<>()

        public MockDependencies() {
            super("pojobooster", CompositeDomainObjectSet.create(Dependency.class))
        }

        public boolean add(Dependency dependency) {
            return set.add(dependency)
        }

        public int size() {
            return set.size()
        }

        public Iterator<Dependency> iterator() {
            return set.iterator()
        }

        public boolean contains(Dependency dependency) {
            return set.contains(dependency)
        }
    }

    private static class JavaSourceSetMock {
        public final Map<String, Object> main

        JavaSourceSetMock() {
            main = new HashMap<>(2);
            main.put("java", new JavaConfigMock())
            main.put("compileClasspath", new ArrayList<DefaultConfiguration>())
        }
    }

    private static class JavaConfigMock {
        public void setIncludes(String str) {}
        public List<String> srcDirs = new ArrayList<>()
        public List<File> includes = new ArrayList<>()

    }
}