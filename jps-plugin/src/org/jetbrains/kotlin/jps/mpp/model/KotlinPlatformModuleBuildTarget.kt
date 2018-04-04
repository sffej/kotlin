/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.mpp.model

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.util.SmartList
import com.intellij.util.containers.MultiMap
import com.intellij.util.io.URLUtil
import org.jetbrains.jps.builders.BuildTargetRegistry
import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.model.java.JpsJavaClasspathKind
import org.jetbrains.jps.model.java.JpsJavaDependenciesEnumerator
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.module.JpsSdkDependency
import org.jetbrains.kotlin.build.JvmSourceRoot
import org.jetbrains.kotlin.config.IncrementalCompilation
import org.jetbrains.kotlin.config.TargetPlatformKind
import org.jetbrains.kotlin.jps.build.KotlinBuilderModuleScriptGenerator
import org.jetbrains.kotlin.jps.model.KotlinCommonModule
import org.jetbrains.kotlin.jps.targetPlatform
import java.io.File

class KotlinPlatformModuleBuildTarget(target: ModuleBuildTarget) : AbstractMppModuleBuildTarget(target) {
    val module = KotlinPlatformModule(target.module)
    val expectedBy = mutableListOf<KotlinCommonModule>()

    init {
        findExpectedBy()
    }

    private fun findExpectedBy() {
        target.allDependencies.modules.forEach {
            val targetPlatform = it.targetPlatform
            if (targetPlatform == TargetPlatformKind.Common) {
                expectedBy.add(KotlinCommonModule(it))
            }
        }
    }

    val additionalOutputDirsWhereInternalsAreVisible: List<File>
        get() {
            return KotlinBuilderModuleScriptGenerator.getProductionModulesWhichInternalsAreVisible(target).mapNotNullTo(SmartList<File>()) {
                JpsJavaExtensionService.getInstance().getOutputDirectory(it, false)
            }
        }

    /**
     * @param dirtySourceFiles ignored for non-incremental compilation
     */
    fun findSources(dirtySourceFiles: MultiMap<ModuleBuildTarget, File>): List<File> = mutableListOf<File>().also { result ->
        expectedBy.forEach {
            it.addAllKotlinSourceFilesWithDependenciesRecursivly(result, target.isTests)
        }

        if (IncrementalCompilation.isEnabled()) result.addAll(dirtySourceFiles.get(target))
        else module.addAllKotlinSourceFiles(result, target.isTests)
    }

    fun findSourceRoots(context: CompileContext): List<JvmSourceRoot> {
        return mutableListOf<JvmSourceRoot>().also { result ->
            expectedBy.forEach {
                val selector =
                    if (target.isTests) BuildTargetRegistry.ModuleTargetSelector.TEST
                    else BuildTargetRegistry.ModuleTargetSelector.PRODUCTION

                val commonTargets = context.projectDescriptor.buildTargetIndex.getModuleBasedTargets(it.module, selector)
                commonTargets.forEach {
                    if (it is ModuleBuildTarget) {
                        addSourceRoots(result, it, context)
                    }
                }
            }

            addSourceRoots(result, target, context)
        }
    }

    fun findClassPathRoots(): Collection<File> {
        return target.allDependencies.classes().roots.filter { file ->
            if (!file.exists()) {
                val extension = file.extension

                // Don't filter out files, we want to report warnings about absence through the common place
                if (!(extension == "class" || extension == "jar")) {
                    return@filter false
                }
            }

            true
        }
    }

    fun findModularJdkRoot(): File? {
        // List of paths to JRE modules in the following format:
        // jrt:///Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home!/java.base
        val urls = JpsJavaExtensionService.dependencies(target.module)
            .satisfying { dependency -> dependency is JpsSdkDependency }
            .classes().urls

        val url = urls.firstOrNull { it.startsWith(StandardFileSystems.JRT_PROTOCOL_PREFIX) } ?: return null

        return File(url.substringAfter(StandardFileSystems.JRT_PROTOCOL_PREFIX).substringBeforeLast(URLUtil.JAR_SEPARATOR))
    }
}

val ModuleBuildTarget.allDependencies: JpsJavaDependenciesEnumerator
    get() {
        return JpsJavaExtensionService.dependencies(module).recursively().exportedOnly()
            .includedIn(JpsJavaClasspathKind.compile(isTests))
    }