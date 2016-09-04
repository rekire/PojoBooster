package eu.rekisoft.groovy.pojobooster

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.tooling.BuildException

import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.StandardJavaFileManager
import javax.tools.ToolProvider

class PojoBoosterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.configurations.create 'pojobooster'
        project.extensions.create 'pojobooster', PojoBoosterPluginExtension

        // TODO this should simplify it and not more complex

        project.dependencies.
                add("compile", project.getDependencies().create("eu.rekisoft.pojobooster:Annotations:$project.libVersion"))
        project.dependencies.
                add("compile", project.getDependencies().create("com.android.support:support-annotations:24.1.0"))
        project.dependencies.
                add("pojobooster", project.getDependencies().create("eu.rekisoft.pojobooster:Annotations:$project.libVersion"))
        project.dependencies.
                add("pojobooster", project.getDependencies().create("eu.rekisoft.pojobooster:Preprocessor:$project.libVersion"))
        project.dependencies.
                add("pojobooster", project.getDependencies().create("com.squareup:javapoet:1.7.0"))
        project.dependencies.
                add("pojobooster", project.getDependencies().create("com.android.support:support-annotations:24.1.0"))
        project.dependencies.
                add("pojobooster", project.getDependencies().create("org.robolectric:android-all:6.0.0_r1-robolectric-0"))

        project.afterEvaluate {
            if (project.plugins.hasPlugin('java')) {
                applyToJavaProject(project)
            } else if (project.plugins.hasPlugin('com.android.application')
                    || project.plugins.hasPlugin('com.android.library')) {
                applyToAndroidProject(project)
            } else {
                throw new BuildException('The project isn\'t a Java or Android project', null)
            }
        }
    }

    // FIXME
    def applyToJavaProject(project) {
        /*
        File aptOutputDir = getOutputDir(project, null)
        println "do some magic with $aptOutputDir.path"
        project.task('addPojoboosterCompilerArgs') << {
            project.compileJava.options.compilerArgs.addAll '-processorpath',
                    project.configurations.apt.asPath, '-s', aptOutputDir.path

            project.compileJava.source = project.compileJava.source.filter {
                !it.path.startsWith(aptOutputDir.path)
            }

            project.compileJava.doFirst {
                logger.info "Generating sources using the annotation processing tool:"
                logger.info "  Output directory: ${aptOutputDir}"

                variant.javaCompile.classpath += project.configurations.pojobooster

                aptOutputDir.mkdirs()
            }
        }
        project.tasks.getByName('compileJava').dependsOn 'addPojoboosterCompilerArgs'
        //*/

        //def extension = project.plugins.getPlugin('java').sourceSets

        //println "plugin: " + project.plugins.getPlugin('java')
        //println "sources: " + project.sourceSets.main.getClass()

        //return;

        //FIXME: def generatedFilesDir = getOutputDir(project, null)
        //FIXME: project.sourceSets.main.compileClasspath.add(Project.files((Object)generatedFilesDir))
        //FIXME: project.sourceSets.main.runtimeClasspath.add(Project.files((Object)generatedFilesDir))

        println "TYPE: " + project.sourceSets.main.getClass()

        String path = project.configurations.pojobooster.asPath
        def stubTaskName = "generatePojoBoosterStubs"
        def classTaskName = "generatePojoBoosterClasses"
        project.task(stubTaskName) {
            group = "Code generation"
            description = "Generated stubs which will been replaced by $classTaskName."
            // TODO enable when stable
            //inputs.file androidExtension.sourceSets.main.java.srcDirs
            //outputs.dir 'build/generated/source/pojo-stubs/' + variant.name
            doLast {
                //FIXME: runPreprocessor(true, path, logger, null, project.sourceSets.main, project)
            }
        }
        project.task(classTaskName) {
            group = "Code generation"
            description = "Generated classes for the java project."
            // TODO enable when stable
            //inputs.file 'build/generated/source/pojo-stubs/' + variant.name
            //outputs.dir 'build/generated/source/pojo/' + variant.name
            doLast {
                //FIXME: runPreprocessor(false, path, logger, null, project.sourceSets.main, project)
            }
        }
        project.tasks[classTaskName].dependsOn stubTaskName
        project.tasks.compileJava.dependsOn classTaskName
    }

    def applyToAndroidProject(project) {
        def androidExtension
        def variants
        def logger = project.logger

        if (project.plugins.hasPlugin('com.android.application')) {
            logger.info "PojoBoosterPlugin detected app project"
            androidExtension = project.plugins.getPlugin('com.android.application').extension
            variants = androidExtension.applicationVariants
        } else if (project.plugins.hasPlugin('com.android.library')) {
            logger.info "PojoBoosterPlugin detected lib project"
            androidExtension = project.plugins.getPlugin('com.android.library').extension
            variants = androidExtension.libraryVariants
        } else {
            throw new BuildException("Something went wrong in the detection.")
        }

        variants.all { variant ->
            File generatedFilesDir = getOutputDir(project, variant.name)
            androidExtension.sourceSets[sourceSetName(variant)].java.srcDirs += generatedFilesDir
            javaCompile.source += generatedFilesDir

            String path = project.configurations.pojobooster.asPath
            def stubTaskName = "generate${variant.name.capitalize()}PojoBoosterStubs"
            def classTaskName = "generate${variant.name.capitalize()}PojoBoosterClasses"
            project.task(stubTaskName) {
                group = "Code generation"
                description = "Generated stubs for the ${variant.name} variant which will been replaced by $classTaskName."
                // TODO enable when stable
                //inputs.file androidExtension.sourceSets.main.java.srcDirs
                //outputs.dir 'build/generated/source/pojo-stubs/' + variant.name
                doLast {
                    runPreprocessor(true, path, logger, variant.name, androidExtension.sourceSets, project)
                }
            }
            project.task(classTaskName) {
                group = "Code generation"
                description = "Generated classes for the ${variant.name} variant."
                // TODO enable when stable
                //inputs.file 'build/generated/source/pojo-stubs/' + variant.name
                //outputs.dir 'build/generated/source/pojo/' + variant.name
                doLast {
                    runPreprocessor(false, path, logger, variant.name, androidExtension.sourceSets, project)
                }
            }
            project.tasks[classTaskName].dependsOn stubTaskName
            project.tasks.preBuild.dependsOn classTaskName
        }
    }

    def static sourceSetName(variant) {
        variant.dirName.split('/').last()
    }

    def static getOutputDir(project, variant) {
        File outputDir
        if(variant != null) {
            outputDir = new File(project.pojobooster.outputDirName, variant);
        } else {
            outputDir = new File(project.pojobooster.outputDirName);
        }
        if(!outputDir.exists()) {
            outputDir.mkdirs()
        }
        project.file outputDir
    }

    def static getStubDir(project, variant) {
        File outputDir
        if(variant != null) {
            outputDir = new File(project.pojobooster.stubDirName, variant);
        } else {
            outputDir = new File(project.pojobooster.stubDirName);
        }
        if(!outputDir.exists()) {
            outputDir.mkdirs()
        }
        project.file outputDir
    }

    private static void runPreprocessor(boolean justStubs, String classPath, Logger logger, String variantName, NamedDomainObjectCollection sourceSets, Project project) {
        logger.debug "Generating code for $variantName with stubs = $justStubs"

        List<String> sourceFiles = finder(sourceSets['main'].getJava().getSrcDirs())

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler()

        String step = justStubs ? "stub" : "generation"
        LogLevel compileLogLevel = justStubs ? LogLevel.DEBUG : LogLevel.WARN
        File target = justStubs ? getStubDir(project, variantName) : getOutputDir(project, variantName)
        if(!justStubs) {
            classPath += File.pathSeparator + getStubDir(project, variantName)
        }

        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>()
        final StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)

        final Iterable<? extends JavaFileObject> sources = manager.getJavaFileObjectsFromFiles(sourceFiles)

        File output = project.file(justStubs ? "build/tmp/pojo-stub" : "build/intermediates/classes/" + variantName);
        if(!output.exists()) {
            output.mkdirs()
        }

        // set compiler's classpath to be same as the runtime's
        List<String> optionList = Arrays.asList("-classpath", classPath, "-Astep=" + step,
                "-Atarget=" + target.toString(), // TODO forward the current log level
                "-d", output.toString());

        final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics,
                optionList, null, sources);
        task.call();

        for (final Diagnostic<? extends JavaFileObject> diagnostic :
                diagnostics.getDiagnostics()) {
            logger.log(compileLogLevel, String.format("COMPILE-Error: %s, line %d in %s",
                    diagnostic.getMessage(null),
                    diagnostic.getLineNumber(),
                    diagnostic.getSource() != null ? diagnostic.getSource().getName() : "null"));
        }
    }
    public static File[] finder(Set<File> dirs) {
        List<File> files = new ArrayList<>();
        for(File dir : dirs) {
            scanDir(dir, files)
        }
        return files;
    }
    private static void scanDir(File dir, List<File> files) {
        if(dir.exists()) {
            files.addAll(dir.listFiles(new FilenameFilter() {
                public boolean accept(File file, String filename) {
                    File full = new File(file, filename)
                    if (full.isDirectory()) {
                        scanDir(full, files)
                    }
                    return filename.endsWith(".java");
                }
            }));
        }
    }
}