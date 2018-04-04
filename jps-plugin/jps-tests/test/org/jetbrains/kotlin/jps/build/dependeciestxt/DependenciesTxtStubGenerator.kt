/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build.dependeciestxt

import org.jetbrains.kotlin.config.TargetPlatformKind
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeFirstLetter
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeFirstWord
import java.io.File

/**
 * Utility for generating common/platform module stub contents based on it's dependencies.
 * Files generated only for modules without any file.
 *
 * Generated files:
 *  - for common modules: `service.kt` with:
 *    - `expect fun platformDependent(): String`
 *    - `fun platformIndependent() = "platformIndependent"`
 *  - for platform modules: `service.kt` with:
 *    - `actual fun platformDependent() = "$platformName"`
 *    - `fun platformOnly() = "$platformName only"`
 */
class DependenciesTxtStubGenerator(val txt: DependenciesTxt, val dir: File) {
    val moduleContents = mutableMapOf<DependenciesTxt.Module, ModuleContents>()

    val DependenciesTxt.Module.contents get() = moduleContents.getOrPut(this) { ModuleContents(this) }
    val DependenciesTxt.Module.capitalName get() = name.capitalizeFirstLetter()
    val DependenciesTxt.Module.serviceKtFileName get() = "service${capitalName}.kt"

    class ModuleContents(val module: DependenciesTxt.Module) {
        val files = mutableListOf<File>()
    }

    fun generate() {
        scanFiles()

        txt.modules.forEach {
            if (it !in moduleContents) {
                generateModuleStub(it)
            }
        }
    }

    private fun generateModuleStub(module: DependenciesTxt.Module) {
        val expectedBy = module.dependencies.filter { it.expectedBy }
        if (expectedBy.isNotEmpty()) {
            expectedBy.forEach {
                val commonModule = it.to
                generatePlatformStub(module, commonModule)
            }
            return
        }

        val kotlinFacetSettings = module.kotlinFacetSettings
        if (kotlinFacetSettings != null) {
            val targetPlatformKind = kotlinFacetSettings.targetPlatformKind
            if (targetPlatformKind == TargetPlatformKind.Common) {
                generateCommonStub(module)
                return
            }
        }
    }

    private fun generateCommonStub(module: DependenciesTxt.Module) {
        File(dir, "${module.name}_${module.serviceKtFileName}").writeText(
            """
            | expect fun ${module.name}_platformDependent(): String
            | fun ${module.name}_platformIndependent() = "common"
            """.trimMargin()
        )
    }

    private fun generatePlatformStub(module: DependenciesTxt.Module, commonModule: DependenciesTxt.Module) {
        File(dir, "${module.name}_${commonModule.serviceKtFileName}").writeText(
            """
            | actual fun ${commonModule.name}_platformDependent(): String = "${module.name}"
            | fun ${commonModule.name}_platformOnly() = "${module.name}"
            """.trimMargin()
        )
    }

    private fun scanFiles() {
        dir.listFiles().forEach { file ->
            if (file.name.endsWith(".kt")) {
                txt.modules.forEach { module ->
                    val prefix = "${module.name}_"
                    if (file.name.startsWith(prefix)) {
                        module.contents.files.add(file)
                    }
                }
            }
        }
    }
}