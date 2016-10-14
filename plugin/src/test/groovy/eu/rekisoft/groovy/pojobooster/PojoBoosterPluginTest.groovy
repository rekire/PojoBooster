package eu.rekisoft.groovy.pojobooster

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
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
import org.mockito.BDDMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail
import static org.mockito.Matchers.any
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

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");

        project = ProjectBuilder.builder().build()
        //configure the project directly with included jars to prevent going to the internet to load these
        //project.configurations {
        //    pojobooster
        //}

        Configuration pojobooster = mock(Configuration.class)
        when(pojobooster.getName()).thenReturn("pojobooster")
        DefaultDependencySet dependencySet = new DefaultDependencySet("pojoDebs", CompositeDomainObjectSet.create(Dependency.class))
        when(pojobooster.getDependencies()).thenReturn(dependencySet)
        project.configurations.add(pojobooster)

        project.ext.libVersion = '0.0.0'
        /*
        project.dependencies {
            ['lib/jslint4java-1.4.4.jar', 'lib/jslint4java-ant-1.4.4.jar', 'lib/js-1.7R2.jar',
             'lib/jcommander-1.11.jar'].each {
                jslint project.files(new File(it).absolutePath)
            }
        }//*/
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
            def dependencies = config.allDependencies.asList()
            switch(config.name) {
            case 'pojobooster':
                assertEquals(3, dependencies.size())
                break;
            case 'provided':
                assertEquals(1, dependencies.size())
                break;
            default:
                throw new RuntimeException("Unexpected config")
            }
        }
        //project.configurations.asList().get(0).allDependencies.asList().get(0).group
        //assertThat(project.convention.plugins.pojobooster, instanceOf(PojoBoosterPlugin))
        try {
            project.evaluate()
            fail()
        } catch(ProjectConfigurationException e) {
            assertEquals(BuildException.class, e.cause.class)
            assertEquals('The project isn\'t a Java or Android project', e.cause.message)
        }
    }

    @Test
    public void testDetetctionCrash() {
        plugin.apply(project)
        try {
            plugin.applyToAndroidProject(project)
            fail()
        } catch(BuildException e) {
            assertEquals('Something went wrong in the detection.', e.message)
        }
    }

    @Test
    public void testAndroidProject() {
        AndroidPluginMock androidPlugin = mock(AndroidPluginMock.class)

        project = spy(project)
        def plugins = spy(project.plugins)
        when(project.plugins).thenReturn(plugins)
        when(plugins.findPlugin(matches('com.android.application'))).thenReturn(androidPlugin)
        when(androidPlugin.extension).thenReturn(new ExtensionMock())
        project.plugins.add(androidPlugin)
        plugin.apply(project)



        mockStatic(GUtil.class)
        BDDMockito.given(GUtil.asPath(any())).willReturn("/success/mocked/value")
        BDDMockito.given(GUtil.isTrue(any())).willReturn(true)

        plugin.applyToAndroidProject(project)
    }

    @Test
    public void testHelloWorldTask() throws IOException {
        String buildFileContent =
"""buildscript {
    repositories {
        jcenter()
        mavenLocal()
    }
    dependencies {
        classpath 'eu.rekisoft:pojobooster:0.0.0'
//      classpath "eu.rekisoft:Pojobooster:$project.libVersion"
    }
}
repositories {
    jcenter()
    mavenLocal()
}
apply plugin: 'java'
apply plugin: 'eu.rekisoft.pojobooster'""";
        writeFile(buildFile, buildFileContent)
        File sourceDir = new File(buildFile.absolutePath.replace("build.gradle", "src/main/java"))
        sourceDir.mkdirs()
        writeFile(new File(sourceDir, "Test.java"), "import eu.rekisoft.java.pojobooster.*;\n @Enhance(name = \"Foo\", extensions = JsonPacker.class) class Test { public int foo; }");

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("compileJava")
                .build();

        println testProjectDir
        //Thread.sleep(15000)

        println result.output
        //assertTrue(result.output.contains("Hello world!"));
        assertEquals(result.task(":compileJava").getOutcome(), SUCCESS);
    }

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    public static void all(Closure closure){
        closure.call(new VariantMock())
    }

    public static String getSourceSetName(VariantMock variant) {
        return variant.name
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
            Map<String, Map<String, String>> sourceSet = new HashMap<>(1);
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
}