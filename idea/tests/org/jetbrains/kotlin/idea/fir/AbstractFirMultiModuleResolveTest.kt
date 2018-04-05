/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.fir

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.fir.builder.RawFirBuilder
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.doFirResolveTestBench
import org.jetbrains.kotlin.fir.java.FirJavaIdeBasedSession
import org.jetbrains.kotlin.fir.resolve.FirProvider
import org.jetbrains.kotlin.fir.resolve.impl.FirProviderImpl
import org.jetbrains.kotlin.fir.resolve.transformers.FirTotalResolveTransformer
import org.jetbrains.kotlin.fir.service
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.caches.project.productionSourceInfo
import org.jetbrains.kotlin.idea.multiplatform.setupMppProjectFromDirStructure
import org.jetbrains.kotlin.idea.stubs.AbstractMultiModuleTest
import org.jetbrains.kotlin.idea.test.PluginTestCaseBase
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

abstract class AbstractFirMultiModuleResolveTest : AbstractMultiModuleTest() {
    override fun getTestDataPath(): String {
        return File(PluginTestCaseBase.getTestDataPathBase(), "/fir/multiModule").path + File.separator
    }

    fun doTest(dirPath: String) {
        setupMppProjectFromDirStructure(File(dirPath))
        doFirResolveTest()
    }

    private fun createSession(module: Module) = FirJavaIdeBasedSession(module.productionSourceInfo()!!, module, project)

    private fun doFirResolveTest() {
        val firFiles = mutableListOf<FirFile>()
        for (module in project.allModules().drop(1)) {
            val session = createSession(module)

            val builder = RawFirBuilder(session)
            val psiManager = PsiManager.getInstance(project)

            val files = FileTypeIndex.getFiles(KotlinFileType.INSTANCE, GlobalSearchScope.moduleScope(module))

            println("Got vfiles: ${files.size}")
            files.forEach {
                val file = psiManager.findFile(it) as? KtFile ?: return@forEach
                val firFile = builder.buildFirFile(file)
                (session.service<FirProvider>() as FirProviderImpl).recordFile(firFile)
                firFiles += firFile
            }
        }
        println("Raw fir up, files: ${firFiles.size}")

        doFirResolveTestBench(firFiles, FirTotalResolveTransformer().transformers, project, dumpNeeded = true)
    }
}