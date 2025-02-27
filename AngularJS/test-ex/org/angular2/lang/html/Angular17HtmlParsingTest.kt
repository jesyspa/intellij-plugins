// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

class Angular17HtmlParsingTest : Angular2HtmlParsingTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_17

  fun testIncompleteBlock6() {
    doTestHtml("""
      @switch (user.name) {
          @c
      }
    """.trimIndent())
  }

  fun testForBlockParens() {
    doTestHtml("""
      @for ((item of items) ; ) {
      }
    """.trimIndent())
  }

  fun testForBlockParens2() {
    doTestHtml("""
      @for (((item of items ff bar 12) dd ) ) ff ; ) {
      }
    """.trimIndent())
  }

  fun testForBlockParens3() {
    doTestHtml("""
      @for (((item of items ff bar 12) dd ; ) {
      }
    """.trimIndent())
  }

  fun testForBlockParens4() {
    doTestHtml("""
      @for ((item of items); track trackingFn(item, compProp)) {{{item}}}
    """.trimIndent())
  }

}
