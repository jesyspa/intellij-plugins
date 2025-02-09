// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.codeStyle.CodeStyleManager

object Angular2HtmlBlockInsertHandler : InsertHandler<LookupElement> {

  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val name = item.lookupString.removePrefix("@")
    val config = getAngular2HtmlBlocksConfig(context.file)
    val definition = config.definitions[name]
    val insertOffset = context.editor.caretModel.offset
    val hasParameters = definition != null && definition.parameters.isNotEmpty()
    if (hasParameters) {
      context.document.insertString(insertOffset, " ()")
      context.editor.caretModel.currentCaret.moveToOffset(insertOffset + 2)
      val autoPopupController = AutoPopupController.getInstance(context.project)
      autoPopupController.scheduleAutoPopup(context.editor)
      autoPopupController.autoPopupParameterInfo(context.editor, null)
    }
    else {
      context.document.insertString(insertOffset, " {\n\n}")
      context.editor.caretModel.currentCaret.moveToOffset(insertOffset + 3)
    }
    context.commitDocument()
    CodeStyleManager.getInstance(context.project).reformatText(
      context.file, context.startOffset, context.offsetMap.getOffset(CompletionInitializationContext.SELECTION_END_OFFSET))
    if (!hasParameters) {
      CodeStyleManager.getInstance(context.project).adjustLineIndent(context.file, context.editor.caretModel.offset)
    }
  }

}