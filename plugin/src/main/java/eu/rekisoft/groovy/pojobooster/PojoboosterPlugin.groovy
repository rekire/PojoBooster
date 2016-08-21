package eu.rekisoft.groovy.pojobooster

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.BuildException

class PojoboosterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.configurations.create 'pojobooster'
        project.extensions.create 'pojobooster', PojoboosterPluginExtension

        project.dependencies.
                add("compile", project.files("$project.rootDir/annotations/build/libs/annotations.jar"))
        project.dependencies.
                add("pojobooster", project.files("$project.rootDir/preprocessor/build/libs/preprocessor.jar"))

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

    def applyToJavaProject(project) {
        File aptOutputDir = getOutputDir(project)
        project.task('addPojoboosterCompilerArgs') << {
            project.compileJava.options.compilerArgs.addAll '-processorpath',
                    project.configurations.apt.asPath, '-s', aptOutputDir.path

            project.compileJava.source = project.compileJava.source.filter {
                !it.path.startsWith(aptOutputDir.path)
            }

            project.compileJava.doFirst {
                logger.info "Generating sources using the annotation processing tool:"
                logger.info "  Output directory: ${aptOutputDir}"

                aptOutputDir.mkdirs()
            }
        }
        project.tasks.getByName('compileJava').dependsOn 'addPojoboosterCompilerArgs'
    }

    def applyToAndroidProject(project) {
        def androidExtension
        def variants

        if (project.plugins.hasPlugin('com.android.application')) {
            androidExtension = project.plugins.getPlugin('com.android.application').extension
            variants = androidExtension.applicationVariants
        } else if (project.plugins.hasPlugin('com.android.library')) {
            androidExtension = project.plugins.getPlugin('com.android.library').extension
            variants = androidExtension.libraryVariants
        } else {
            throw new BuildException("Something went wrong in the detection.")
        }

        //androidExtension.sourceSets.main.java.srcDirs += "build/generated/source/pojo/debug"

        variants.all { variant ->
            File aptOutputDir = getOutputDir(project)
            File variantAptOutputDir = project.file("${aptOutputDir}/${dirName}")

            androidExtension.sourceSets[sourceSetName(variant)].java.srcDirs.addAll variantAptOutputDir.path

            javaCompile.options.compilerArgs.addAll '-processorpath',
                    project.configurations.apt.asPath, '-s', variantAptOutputDir.path

            javaCompile.source = javaCompile.source.filter {
                !variant.variantData.extraGeneratedSourceFolders.each { folder ->
                    folder.path.startsWith(aptOutputDir.path)
                }
            }

            javaCompile.doFirst {
                logger.info "Generating sources using the annotation processing tool:"
                logger.info "  Variant: ${variant.name}"
                logger.info "  Output directory: ${variantAptOutputDir}"

                variantAptOutputDir.mkdirs()
            }
        }
    }

    def static sourceSetName(variant) {
        variant.dirName.split('/').last()
    }

    def static getOutputDir(project) {
        def outputDirName = project.pojobooster.outputDirName
        if (!outputDirName) {
            outputDirName = 'build/source/pojo'
        }
        project.file outputDirName
    }
}