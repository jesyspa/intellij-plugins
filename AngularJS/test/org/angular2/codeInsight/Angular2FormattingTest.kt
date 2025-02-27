// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.javascript.web.WebFrameworkTestModule
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.lang.html.psi.formatter.Angular2HtmlCodeStyleSettings

class Angular2FormattingTest : Angular2TestCase("formatting") {

  fun testStyles() = testFormatting()

  fun testTemplate() = testFormatting()

  fun testAttrs() = testFormatting(extension = "html") {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
  }

  fun testInnerAttrs() = testFormatting {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true
  }

  fun testNoKeepLineBreaks() = testFormatting(extension = "html") {
    val htmlSettings = getCustomSettings(HtmlCodeStyleSettings::class.java)
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = false
    htmlSettings.HTML_KEEP_LINE_BREAKS = false
    htmlSettings.HTML_KEEP_BLANK_LINES = 0
  }

  fun testAttributeTyping() = doConfiguredTest(extension = "html", checkResult = true) {
    myFixture.type("\ntest2\n[test]=\"\"\n[(banana)]=\"\"\nother\n")
  }

  fun testInterpolationNoNewLineDoNotWrap() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.DO_NOT_WRAP
  )

  fun testInterpolationNoNewLineWrapAsNeeded() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  fun testInterpolationNoNewLineWrapAlways() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_ALWAYS
  )

  fun testInterpolationNewLineDoNotWrap() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.DO_NOT_WRAP
  )

  fun testInterpolationNewLineWrapAsNeeded() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  fun testInterpolationNewLineWrapAlways() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_ALWAYS
  )

  fun testInterpolationNewLineBeforeEnd() = testInterpolation(
    newLineAfterStart = false,
    newLineBeforeEnd = true,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  fun testInterpolationNewLineAfterStart() = testInterpolation(
    newLineAfterStart = true,
    newLineBeforeEnd = false,
    wrap = CommonCodeStyleSettings.WRAP_AS_NEEDED
  )

  fun testBasicBlocks() = testFormatting(Angular2TestModule.ANGULAR_CORE_17_0_0_RC_0, extension = "html")

  private fun testFormatting(vararg modules: WebFrameworkTestModule,
                             dir: Boolean = false,
                             extension: String = "ts",
                             configureFileName: String = "$testName.$extension",
                             configureCodeStyleSettings: CodeStyleSettings.() -> Unit = {}) =
    doConfiguredTest(*modules, dir = dir, extension = extension,
                     checkResult = true, configureFileName = configureFileName,
                     configureCodeStyleSettings = configureCodeStyleSettings) {
      val codeStyleManager = CodeStyleManager.getInstance(project)
      WriteCommandAction.runWriteCommandAction(project) { codeStyleManager.reformat(file) }
    }

  private fun testInterpolation(newLineAfterStart: Boolean, newLineBeforeEnd: Boolean, wrap: Int) =
    testFormatting(configureFileName = "interpolation.html") {
      val vueSettings = getCustomSettings(Angular2HtmlCodeStyleSettings::class.java)
      vueSettings.SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = false
      getLanguageIndentOptions(HTMLLanguage.INSTANCE).INDENT_SIZE = 4
      vueSettings.INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER = newLineBeforeEnd
      vueSettings.INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER = newLineAfterStart
      vueSettings.INTERPOLATION_WRAP = wrap
    }

}
