/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.coroutines

import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

data class MultipleAnnotationVisitorsDelegate(private val avs: Collection<AnnotationVisitor>) : AnnotationVisitor(Opcodes.ASM5) {
    override fun visitEnd() {
        avs.forEach { it.visitEnd() }
    }

    override fun visitAnnotation(name: String?, desc: String?): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitAnnotation(name, desc) }
    }

    private inline fun wrapAnnotationVisitors(c: (AnnotationVisitor) -> AnnotationVisitor) = MultipleAnnotationVisitorsDelegate(avs.map(c))

    override fun visitEnum(name: String?, desc: String?, value: String?) {
        avs.forEach { it.visitEnum(name, desc, value) }
    }

    override fun visit(name: String?, value: Any?) {
        avs.forEach { it.visit(name, value) }
    }

    override fun visitArray(name: String?): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitArray(name) }
    }
}