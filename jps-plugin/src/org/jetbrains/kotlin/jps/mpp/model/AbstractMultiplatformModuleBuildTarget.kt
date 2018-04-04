/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.mpp.model

import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.kotlin.build.JvmSourceRoot

abstract class AbstractMppModuleBuildTarget(val target: ModuleBuildTarget) {

}

fun addSourceRoots(
    result: MutableList<JvmSourceRoot>,
    moduleBuildTarget: ModuleBuildTarget,
    context: CompileContext
): List<JvmSourceRoot> {
    val roots = context.projectDescriptor.buildRootIndex.getTargetRoots(moduleBuildTarget, context)
    for (root in roots) {
        val file = root.rootFile
        val prefix = root.packagePrefix
        if (file.exists()) {
            result.add(JvmSourceRoot(file, if (prefix.isEmpty()) null else prefix))
        }
    }
    return result
}
