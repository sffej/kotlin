/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.fir.java.symbols.JavaClassSymbol
import org.jetbrains.kotlin.fir.resolve.FirSymbolProvider
import org.jetbrains.kotlin.fir.symbols.ConeSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.KotlinJavaPsiFacade

abstract class AbstractJavaSymbolProvider(val project: Project, val module: Module?) : FirSymbolProvider {

    protected abstract val searchScope: GlobalSearchScope

    private val classCache = mutableMapOf<ClassId, ConeSymbol?>()
    private val packageCache = mutableMapOf<FqName, FqName?>()


    private inline fun <K, V : Any?> MutableMap<K, V>.lookupCacheOrCalculate(key: K, l: (K) -> V): V? {
        return if (key in this.keys) {
            this[key]
        } else {
            val calculated = l(key)
            this[key] = calculated
            calculated
        }
    }

    override fun getSymbolByFqName(classId: ClassId): ConeSymbol? {
        return classCache.lookupCacheOrCalculate(classId) {
            val facade = KotlinJavaPsiFacade.getInstance(project)
            val foundClass = facade.findClass(classId, searchScope)
            foundClass?.let { JavaClassSymbol(it) }
        }
    }

    override fun getPackage(fqName: FqName): FqName? {
        return packageCache.lookupCacheOrCalculate(fqName) {
            val facade = KotlinJavaPsiFacade.getInstance(project)
            val javaPackage = facade.findPackage(fqName.asString(), searchScope) ?: return@lookupCacheOrCalculate null
            FqName(javaPackage.qualifiedName)
        }
    }

}

class JavaSourceSymbolProvider(project: Project, module: Module? = null) : AbstractJavaSymbolProvider(project, module) {
    override val searchScope: GlobalSearchScope =
        module?.moduleContentScope ?: ProjectScope.getProjectScope(project)

    override val doesLookupInFir: Boolean
        get() = true
}

class JavaLibrariesSymbolProvider(project: Project, module: Module? = null) : AbstractJavaSymbolProvider(project, module) {
    override val searchScope: GlobalSearchScope =
        module?.moduleWithLibrariesScope?.intersectWith(ProjectScope.getLibrariesScope(project)) ?: ProjectScope.getLibrariesScope(project)

    override val doesLookupInFir: Boolean
        get() = false
}

