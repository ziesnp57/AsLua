/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2023  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package com.yongle.aslua.lua

import android.os.Bundle
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.QuickQuoteHandler
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.completion.CompletionHelper
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem
import io.github.rosemoe.sora.lang.completion.SnippetDescription
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser
import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandleResult
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.lang.styling.StylesUtils
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.text.TextUtils
import io.github.rosemoe.sora.util.MyCharacter
import io.github.rosemoe.sora.widget.SymbolPairMatch
import io.github.rosemoe.sora.widget.SymbolPairMatch.DefaultSymbolPairs
import kotlin.math.max
import kotlin.math.min

class LuaLanguage : Language {
    private var autoComplete: IdentifierAutoComplete? // 自动补全对象
    private val manager: LuaIncrementalAnalyzeManager // Lua增量分析管理器
    private val luaQuoteHandler = LuaQuoteHandler() // Lua引号处理器

    override fun getAnalyzeManager(): AnalyzeManager { // 获取分析管理器
        return manager
    }

    override fun getQuickQuoteHandler(): QuickQuoteHandler { // 获取快速引号处理器
        return luaQuoteHandler
    }

    override fun destroy() { // 销毁方法
        autoComplete = null
    }

    override fun getInterruptionLevel(): Int { // 获取中断级别
        return Language.INTERRUPTION_LEVEL_STRONG
    }

    override fun requireAutoComplete(
        content: ContentReference, position: CharPosition,
        publisher: CompletionPublisher, extraArguments: Bundle
    ) { // 请求自动补全
        val prefix = CompletionHelper.computePrefix(
            content,
            position
        ) { key: Char -> MyCharacter.isJavaIdentifierPart(key) } // 计算前缀
        val idt = manager.identifiers // 获取标识符
        autoComplete!!.requireAutoComplete(content, position, prefix, publisher, idt) // 请求自动补全
        if ("for".startsWith(prefix) && prefix.isNotEmpty()) { // 如果前缀匹配"for"
            publisher.addItem(
                SimpleSnippetCompletionItem(
                    "for",
                    "for 循环",
                    SnippetDescription(prefix.length, FOR_SNIPPET, true)
                )
            ) // 添加"fori"代码段
        }
        if ("sconst".startsWith(prefix) && prefix.isNotEmpty()) { // 如果前缀匹配"sconst"
            publisher.addItem(
                SimpleSnippetCompletionItem(
                    "sconst",
                    "Snippet - Static Constant",
                    SnippetDescription(prefix.length, STATIC_CONST_SNIPPET, true)
                )
            ) // 添加"sconst"代码段
        }
        if ("clip".startsWith(prefix) && prefix.isNotEmpty()) { // 如果前缀匹配"clip"
            publisher.addItem(
                SimpleSnippetCompletionItem(
                    "clip",
                    "Snippet - Clipboard contents",
                    SnippetDescription(prefix.length, CLIPBOARD_SNIPPET, true)
                )
            ) // 添加"clip"代码段
        }
    }

    override fun getIndentAdvance(text: ContentReference, line: Int, column: Int): Int { // 获取缩进增量
        val content = text.getLine(line).substring(0, column)
        return getIndentAdvance(content)
    }

    private fun getIndentAdvance(content: String): Int { // 获取缩进增量
        val t = LuaTextTokenizer(content)
        var token: Tokens
        var advance = 0
        while (t.nextToken().also { token = it } != Tokens.EOF) {
            if (token == Tokens.LBRACE) {
                advance++
            }
        }
        advance = max(0, advance)
        return advance * 4
    }

    private val newlineHandlers = arrayOf<NewlineHandler>(BraceHandler()) // 换行处理器数组

    init {
        autoComplete = IdentifierAutoComplete(LuaTextTokenizer.sKeywords) // 初始化自动补全对象
        manager = LuaIncrementalAnalyzeManager() // 初始化Java增量分析管理器
    }

    override fun useTab(): Boolean { // 是否使用制表符
        return false
    }

    override fun getFormatter(): Formatter { // 获取格式化器
        return EmptyLanguage.EmptyFormatter.INSTANCE
    }

    override fun getSymbolPairs(): SymbolPairMatch { // 获取符号对匹配器
        return DefaultSymbolPairs()
    }

    override fun getNewlineHandlers(): Array<NewlineHandler> { // 获取换行处理器数组
        return newlineHandlers
    }

    // 大括号换行处理器
    internal inner class BraceHandler : NewlineHandler {
        // 是否满足要求
        override fun matchesRequirement(
            text: Content,
            position: CharPosition,
            style: Styles?
        ): Boolean {
            val line = text.getLine(position.line)
            return !StylesUtils.checkNoCompletion(style, position) && getNonEmptyTextBefore(
                line,
                position.column,
                1
            ) == "{" && getNonEmptyTextAfter(line, position.column, 1) == "}"
        }

        // 处理换行
        override fun handleNewline(
            text: Content,
            position: CharPosition,
            style: Styles?,
            tabSize: Int
        ): NewlineHandleResult {
            val line = text.getLine(position.line)
            val index = position.column
            val beforeText = line.subSequence(0, index).toString()
            val afterText = line.subSequence(index, line.length).toString()
            return handleNewline(beforeText, afterText, tabSize)
        }

        private fun handleNewline(
            beforeText: String,
            afterText: String,
            tabSize: Int
        ): NewlineHandleResult {
            val count = TextUtils.countLeadingSpaceCount(beforeText, tabSize)
            val advanceBefore = getIndentAdvance(beforeText)
            val advanceAfter = getIndentAdvance(afterText)
            var text: String
            val sb = StringBuilder("\n")
                .append(TextUtils.createIndent(count + advanceBefore, tabSize, useTab()))
                .append('\n')
                .append(
                    TextUtils.createIndent(count + advanceAfter, tabSize, useTab())
                        .also { text = it })
            val shiftLeft = text.length + 1
            return NewlineHandleResult(sb, shiftLeft)
        }
    }

    companion object {
        // for循环代码段
        private val FOR_SNIPPET =
            CodeSnippetParser.parse(
                "for i = 1,10 do\n" +
                        "  print(i)\n" +
                        "end"
            )

        // 静态常量代码段
        private val STATIC_CONST_SNIPPET =
            CodeSnippetParser.parse("private final static \${1:type} \${2/(.*)/\${1:/upcase}/} = \${3:value};")

        // 剪贴板内容代码段
        private val CLIPBOARD_SNIPPET = CodeSnippetParser.parse("\${1:\${CLIPBOARD}}")

        // 获取指定位置之前的非空文本
        private fun getNonEmptyTextBefore(text: CharSequence, index: Int, length: Int): String {
            var index = index
            while (index > 0 && Character.isWhitespace(text[index - 1])) {
                index--
            }
            return text.subSequence(max(0, index - length), index).toString()
        }

        // 获取指定位置之后的非空文本
        @Suppress("NAME_SHADOWING")
        private fun getNonEmptyTextAfter(text: CharSequence, index: Int, length: Int): String {
            var index = index
            while (index < text.length && Character.isWhitespace(text[index])) {
                index++
            }
            return text.subSequence(index, min(index + length, text.length)).toString()
        }
    }
}
