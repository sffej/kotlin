/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.kotlin.jps.model.kotlinFacetExtension

class KotlinModuleBuilderTarget(val target: ModuleBuildTarget) {
    val expectedBy by lazy(::findExpectedBy)

    private fun findExpectedBy(): List<JpsModule> {
        val kotlinFacetExtension = target.module.kotlinFacetExtension
        val implementedModuleNames = kotlinFacetExtension?.settings?.implementedModuleNames ?: return listOf()
        if (implementedModuleNames.isEmpty()) return listOf()

        return target.allDependencies.modules.filter { it.name in implementedModuleNames }
    }
}