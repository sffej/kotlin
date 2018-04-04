/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.coroutines

import org.jetbrains.org.objectweb.asm.*

// Method visitor to generate multiple methods simultaneously
data class MultipleMethodVisitorsDelegate(private val mvs: Collection<MethodVisitor>) : MethodVisitor(Opcodes.ASM5) {
    override fun visitMultiANewArrayInsn(desc: String?, dims: Int) {
        mvs.forEach { it.visitMultiANewArrayInsn(desc, dims) }
    }

    override fun visitFrame(type: Int, nLocal: Int, local: Array<out Any>?, nStack: Int, stack: Array<out Any>?) {
        mvs.forEach { it.visitFrame(type, nLocal, local, nStack, stack) }
    }

    override fun visitVarInsn(opcode: Int, `var`: Int) {
        mvs.forEach { it.visitVarInsn(opcode, `var`) }
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
        mvs.forEach { it.visitTryCatchBlock(start, end, handler, type) }
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
        mvs.forEach { it.visitLookupSwitchInsn(dflt, keys, labels) }
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
        mvs.forEach { it.visitJumpInsn(opcode, label) }
    }

    override fun visitLdcInsn(cst: Any?) {
        mvs.forEach { it.visitLdcInsn(cst) }
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        mvs.forEach { it.visitIntInsn(opcode, operand) }
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        mvs.forEach { it.visitTypeInsn(opcode, type) }
    }

    override fun visitAnnotationDefault(): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitAnnotationDefault() }
    }

    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitAnnotation(desc, visible) }
    }

    private inline fun wrapAnnotationVisitors(c: (MethodVisitor) -> AnnotationVisitor) = MultipleAnnotationVisitorsDelegate(mvs.map(c))

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitTypeAnnotation(typeRef, typePath, desc, visible) }
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        mvs.forEach { it.visitMaxs(maxStack, maxLocals) }
    }

    override fun visitInvokeDynamicInsn(name: String?, desc: String?, bsm: Handle?, vararg bsmArgs: Any?) {
        mvs.forEach { it.visitInvokeDynamicInsn(name, desc, bsm, *bsmArgs) }
    }

    override fun visitLabel(label: Label?) {
        mvs.forEach { it.visitLabel(label) }
    }

    override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitTryCatchAnnotation(typeRef, typePath, desc, visible) }
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
        mvs.forEach { it.visitMethodInsn(opcode, owner, name, desc) }
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
        mvs.forEach { it.visitMethodInsn(opcode, owner, name, desc, itf) }
    }

    override fun visitInsn(opcode: Int) {
        mvs.forEach { it.visitInsn(opcode) }
    }

    override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitInsnAnnotation(typeRef, typePath, desc, visible) }
    }

    override fun visitParameterAnnotation(parameter: Int, desc: String?, visible: Boolean): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitParameterAnnotation(parameter, desc, visible) }
    }

    override fun visitIincInsn(`var`: Int, increment: Int) {
        mvs.forEach { it.visitIincInsn(`var`, increment) }
    }

    override fun visitLineNumber(line: Int, start: Label?) {
        mvs.forEach { it.visitLineNumber(line, start) }
    }

    override fun visitLocalVariableAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        start: Array<out Label>?,
        end: Array<out Label>?,
        index: IntArray?,
        desc: String?,
        visible: Boolean
    ): AnnotationVisitor {
        return wrapAnnotationVisitors { it.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible) }
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        mvs.forEach { it.visitTableSwitchInsn(min, max, dflt, *labels) }
    }

    override fun visitEnd() {
        mvs.forEach { it.visitEnd() }
    }

    override fun visitLocalVariable(name: String?, desc: String?, signature: String?, start: Label?, end: Label?, index: Int) {
        mvs.forEach { it.visitLocalVariable(name, desc, signature, start, end, index) }
    }

    override fun visitParameter(name: String?, access: Int) {
        mvs.forEach { it.visitParameter(name, access) }
    }

    override fun visitAttribute(attr: Attribute?) {
        mvs.forEach { it.visitAttribute(attr) }
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
        mvs.forEach { it.visitFieldInsn(opcode, owner, name, desc) }
    }

    override fun visitCode() {
        mvs.forEach { it.visitCode() }
    }
}