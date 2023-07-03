// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.types.JSAsyncReturnType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.evaluable.JSReturnedExpressionType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

class VueCompositionInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfo? =
    descriptor.source
      .asSafely<JSObjectLiteralExpression>()
      ?.let { VueCompositionInfo(it) }

  class VueCompositionInfo(val initializer: JSObjectLiteralExpression) : VueContainerInfo {

    override val computed: List<VueComputedProperty>
      get() = symbols.filterIsInstance(VueComputedProperty::class.java)

    override val data: List<VueDataProperty>
      get() = symbols.filterIsInstance(VueDataProperty::class.java)

    override val methods: List<VueMethod>
      get() = symbols.filterIsInstance(VueMethod::class.java)

    override val provides: List<VueProvide>
      get() = symbols.filterIsInstance(VueProvide::class.java)

    override val injects: List<VueInject>
      get() = symbols.filterIsInstance(VueInject::class.java)

    private val symbols: List<VueNamedSymbol>
      get() = CachedValuesManager.getCachedValue(initializer) {
        val rawBindings = VueCompositionInfoHelper.createRawBindings(
          initializer, getSetupFunctionType(initializer)
        )
        val methodCalls = getSetupCalls(initializer)
        CachedValueProvider.Result.create(rawBindings + methodCalls, PsiModificationTracker.MODIFICATION_COUNT)
      }

    private fun getSetupFunctionType(initializer: JSObjectLiteralExpression): JSType? =
      resolveElementTo(initializer.findProperty(SETUP_METHOD), JSFunction::class)
        ?.returnType
        ?.let { returnType ->
          (returnType as? JSAsyncReturnType)
            ?.substitute()
            ?.asSafely<JSGenericTypeImpl>()
            ?.takeIf { (it.type as? JSTypeImpl)?.typeText == "Promise" }
            ?.arguments
            ?.getOrNull(0)
          ?: (returnType as? JSReturnedExpressionType)?.findAssociatedExpression()?.let {
            JSCodeBasedTypeFactory.getPsiBasedType(it, JSEvaluateContext(it.containingFile))
          }
          ?: returnType
        }

    private fun getSetupCalls(initializer: JSObjectLiteralExpression): List<VueNamedSymbol> =
      resolveElementTo(initializer.findProperty(SETUP_METHOD), JSFunction::class)
        ?.let { JSStubBasedPsiTreeUtil.findDescendants<JSCallExpression>(it, TokenSet.create(JSStubElementTypes.CALL_EXPRESSION)) }
        ?.mapNotNull {
          when (VueFrameworkHandler.getFunctionNameFromVueIndex(it)) {
            PROVIDE_FUN -> analyzeProvide(it)
            INJECT_FUN -> analyzeInject(it)
            else -> null
          }
        }
      ?: emptyList()

    override fun equals(other: Any?): Boolean =
      (other as? VueCompositionInfo)?.initializer == initializer

    override fun hashCode(): Int = initializer.hashCode()
  }
}

