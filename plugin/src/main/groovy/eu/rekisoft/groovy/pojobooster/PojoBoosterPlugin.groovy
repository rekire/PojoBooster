package eu.rekisoft.groovy.pojobooster

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.tooling.BuildException

import javax.tools.*

class PojoBoosterPlugin implements Plugin<Project> {
    private static final String libVersion = '0.0.0'

    @Override
    void apply(Project project) {
        project.configurations.maybeCreate 'pojobooster'
        project.configurations.maybeCreate 'provided'
        project.extensions.create 'pojobooster', PojoBoosterPluginExtension

        project.dependencies.add('provided', project.dependencies.create("eu.rekisoft.pojobooster:annotations:$libVersion"))
        project.dependencies.add('pojobooster', project.dependencies.create("eu.rekisoft.pojobooster:preprocessor:$libVersion"))
        project.dependencies.add('pojobooster', project.dependencies.create("com.squareup:javapoet:1.7.0"))
        project.dependencies.add('pojobooster', project.dependencies.create("org.robolectric:android-all:6.0.0_r1-robolectric-0"))

        project.afterEvaluate {
            if(project.plugins.hasPlugin('java')) {
                applyToJavaProject(project)
            } else if(project.plugins.hasPlugin('com.android.application')
                    || project.plugins.hasPlugin('com.android.library')) {
                applyToAndroidProject(project)
            } else {
                throw new BuildException('The project isn\'t a Java or Android project', null)
            }
        }
    }

    def applyToJavaProject(project) {
        project.configurations.maybeCreate 'compileOnly'
        project.dependencies.add('compileOnly', project.dependencies.create("eu.rekisoft.pojobooster:annotations:$libVersion"))

        // add the required compile time libs to the classpath
        project.sourceSets.main.compileClasspath += project.configurations.pojobooster

        File generatedFilesDir = getOutputDir(project, null)
        project.sourceSets.main.java.srcDirs += generatedFilesDir

        // add the generated sources to the source sets
        List<String> includes = new ArrayList<>()
        for(String include : project.sourceSets.main.java.includes) {
            includes.add(include)
        }

        project.sourceSets.main.java.setIncludes(Arrays.asList(getOutputDir(project, null).path + File.separator + "**.*"))

        def stubTaskName = "generatePojoBoosterStubs"
        def classTaskName = "generatePojoBoosterClasses"
        project.task(stubTaskName) {
            group = "Code generation"
            description = "Generated stubs which will been replaced by $classTaskName."
            inputs.file project.sourceSets.main.java.srcDirs
            outputs.dir getStubDir(project, null)
            doLast {
                String path = project.configurations.pojobooster.asPath //+ File.pathSeparator + javaCompile.source.asPath
                runPreprocessor(true, path, logger, null, project.sourceSets.main.java.srcDirs, project)
            }
        }
        project.task(classTaskName) {
            group = "Code generation"
            description = "Generated classes for the java project."
            inputs.file getStubDir(project, null)
            outputs.dir getOutputDir(project, null)
            doLast {
                String path = project.configurations.pojobooster.asPath //+ File.pathSeparator + javaCompile.source.asPath
                runPreprocessor(false, path, logger, null, project.sourceSets.main.java.srcDirs, project)
            }
        }
        if(!project.pojobooster.outputDirName.startsWith('build/') ||
                !project.pojobooster.stubDirName.startsWith('build/')) {
            project.task("deleteGeneratedCode") {
                group = "Build"
                description = "Delete the generated classes."
                doLast {
                    delete getOutputDir(project, null)
                    delete getStubDir(project, null)
                }
            }
            project.tasks.clean.dependsOn project.deleteGeneratedCode
        }
        project.tasks[classTaskName].dependsOn stubTaskName
        project.tasks.compileJava.dependsOn classTaskName
    }

    def applyToAndroidProject(project) {
        def androidExtension
        def variants
        def logger = project.logger

        if(project.plugins.hasPlugin('com.android.application')) {
            logger.info "PojoBoosterPlugin detected app project"
            androidExtension = project.plugins.getPlugin('com.android.application').extension
            variants = androidExtension.applicationVariants
        } else if(project.plugins.hasPlugin('com.android.library')) {
            logger.info "PojoBoosterPlugin detected lib project"
            androidExtension = project.plugins.getPlugin('com.android.library').extension
            variants = androidExtension.libraryVariants
        } else {
            throw new BuildException("Something went wrong in the detection.", null)
        }

        variants.all { variant ->
            File generatedFilesDir = getOutputDir(project, variant.name)
            androidExtension.sourceSets[sourceSetName(variant)].java.srcDirs += generatedFilesDir
            javaCompile.source += generatedFilesDir

            def stubTaskName = "generate${variant.name.capitalize()}PojoBoosterStubs"
            def classTaskName = "generate${variant.name.capitalize()}PojoBoosterClasses"
            project.task(stubTaskName) {
                group = "Code generation"
                description = "Generated stubs for the ${variant.name} variant which will been replaced by $classTaskName."
                //inputs.file androidExtension.sourceSets.main.java.srcDirs
                //outputs.dir getStubDir(project, variant.name)
                doLast {
                    String path = project.configurations.pojobooster.asPath + File.pathSeparator + javaCompile.source.asPath
                    runPreprocessor(true, path, logger, variant.name, androidExtension.sourceSets.main.java.srcDirs, project)
                }
            }
            project.task(classTaskName) {
                group = "Code generation"
                description = "Generated classes for the ${variant.name} variant."
                //inputs.file getStubDir(project, variant.name)
                //outputs.dir getOutputDir(project, variant.name)
                doLast {
                    String path = project.configurations.pojobooster.asPath + File.pathSeparator + javaCompile.source.asPath
                    runPreprocessor(false, path, logger, variant.name, androidExtension.sourceSets.main.java.srcDirs, project)
                }
            }
            if(!project.pojobooster.outputDirName.startsWith('build/') ||
                    !project.pojobooster.stubDirName.startsWith('build/')) {
                project.task("deleteGeneratedCode") {
                    group = "Build"
                    description = "Delete the generated classes."
                    doLast {
                        delete getOutputDir(project, variant.name)
                        delete getStubDir(project, variant.name)
                    }
                }
                project.tasks.clean.dependsOn project.deleteGeneratedCode
            }
            project.tasks[classTaskName].dependsOn stubTaskName
            project.tasks["generate${variant.name.capitalize()}Sources"].finalizedBy classTaskName
        }
    }

    def static delete(file) {
        if(file.exists()) {
            if(file.directory) {
                for(File f : file.listFiles()) {
                    delete(f)
                }
            }
            if(!file.delete()) {
                throw new FileNotFoundException("Failed to delete " + file)
            }
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

    private static void runPreprocessor(boolean justStubs, String classPath, Logger logger, String variantName,
                                        Set<File> sourceDirs, Project project) {
        logger.debug "Generating code for $variantName with stubs = $justStubs"

        List<String> sourceFiles = finder(sourceDirs)

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

        LogLevel logLevel = LogLevel.ERROR
        for(LogLevel level : LogLevel.values()) {
            if(logger.isEnabled(level)) {
                logLevel = level
                break;
            }
        }

        // set compiler's classpath to be same as the runtime's
        List<String> optionList = Arrays.asList("-classpath", classPath, "-Astep=" + step, "-Atarget=" + target.toString(),
                "-Aloglevel=" + logLevel.name(), "-Avariant=" + variantName, "-d", output.toString());

        final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics,
                optionList, null, sources);
        task.call();

        for(final Diagnostic<? extends JavaFileObject> diagnostic :
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
                    if(full.isDirectory()) {
                        scanDir(full, files)
                    }
                    return filename.endsWith(".java");
                }
            }));
        }
    }
}