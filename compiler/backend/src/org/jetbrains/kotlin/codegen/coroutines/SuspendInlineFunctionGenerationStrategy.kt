/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.coroutines

import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.codegen.FunctionCodegen
import org.jetbrains.kotlin.codegen.FunctionGenerationStrategy
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.JVMConstructorCallNormalizationMode
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.resolve.jvm.diagnostics.OtherOrigin
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.MethodNode

// For named suspend function we generate two methods:
// 1) to use as noinline function, which have state machine
// 2) to use from inliner: private one without state machine
class SuspendInlineFunctionGenerationStrategy(
    state: GenerationState,
    originalSuspendDescriptor: FunctionDescriptor,
    declaration: KtFunction,
    containingClassInternalName: String,
    constructorCallNormalizationMode: JVMConstructorCallNormalizationMode,
    private val codegen: FunctionCodegen
) : SuspendFunctionGenerationStrategy(
    state,
    originalSuspendDescriptor,
    declaration,
    containingClassInternalName,
    constructorCallNormalizationMode
) {
    private val defaultStrategy = FunctionGenerationStrategy.FunctionDefault(state, declaration)

    override fun wrapMethodVisitor(mv: MethodVisitor, access: Int, name: String, desc: String): MethodVisitor {
        if (access and Opcodes.ACC_ABSTRACT != 0) return mv

        return MultipleMethodVisitorsDelegate(
            listOf(
                super.wrapMethodVisitor(mv, access, name, desc),
                defaultStrategy.wrapMethodVisitor(
                    codegen.newMethod(
                        OtherOrigin(declaration, getOrCreateJvmSuspendFunctionView(originalSuspendDescriptor)),
                        access,
                        "$name\$\$forInline",
                        desc,
                        null,
                        null
                    ),
                    access,
                    "$name\$\$forInline",
                    desc
                )
            )
        )
    }

    override fun doGenerateBody(codegen: ExpressionCodegen, signature: JvmMethodSignature) {
        super.doGenerateBody(codegen, signature)
        defaultStrategy.doGenerateBody(codegen, signature)
    }
}