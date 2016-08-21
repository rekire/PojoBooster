package eu.rekisoft.groovy.pojobooster

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.BuildException

class PojoboosterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.configurations.create 'pojobooster'
        project.extensions.create 'pojobooster', PojoboosterPluginExtension

        // TODO this should simplify it and not more complex

        project.dependencies.
                add("compile", project.getDependencies().create("eu.rekisoft.pojobooster:Annotations:$project.libVersion"))
        project.dependencies.
                add("compile", project.getDependencies().create("eu.rekisoft.pojobooster:Preprocessor:$project.libVersion"))
        project.dependencies.
                add("compile", project.getDependencies().create("com.squareup:javapoet:1.7.0"))
        project.dependencies.
                add("compile", project.getDependencies().create("com.android.support:support-annotations:24.1.0"))
        project.dependencies.
                add("compile", project.getDependencies().create("org.robolectric:android-all:6.0.0_r1-robolectric-0"))
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

    def applyToJavaProject(project) {
        File aptOutputDir = getOutputDir(project)
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
    }

    def applyToAndroidProject(project) {
        def androidExtension
        def variants

        if (project.plugins.hasPlugin('com.android.application')) {
            println "detected app"
            androidExtension = project.plugins.getPlugin('com.android.application').extension
            variants = androidExtension.applicationVariants
        } else if (project.plugins.hasPlugin('com.android.library')) {
            println "detected lib"
            androidExtension = project.plugins.getPlugin('com.android.library').extension
            variants = androidExtension.libraryVariants
        } else {
            throw new BuildException("Something went wrong in the detection.")
        }

        //androidExtension.sourceSets.main.java.srcDirs += "build/generated/source/pojo/debug"

        variants.all { variant ->
            File aptOutputDir = getOutputDir(project)
            File variantAptOutputDir = project.file("${aptOutputDir}/${dirName}")

            println sourceSetName(variant) + " -- " + androidExtension.sourceSets[sourceSetName(variant)].java.srcDirs

            androidExtension.sourceSets.main.java.srcDirs += "build/generated/source/pojo/" + sourceSetName(variant)
            androidExtension.sourceSets[sourceSetName(variant)].java.srcDirs.addAll variantAptOutputDir.path

            javaCompile.options.compilerArgs.addAll '-processorpath',
                    project.configurations.pojobooster.asPath, '-s', variantAptOutputDir.path

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