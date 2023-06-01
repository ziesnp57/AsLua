package com.yongle.aslua.lua

import android.os.Bundle
import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager
import io.github.rosemoe.sora.lang.analysis.IncrementalAnalyzeManager
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.lang.brackets.SimpleBracketsCollector
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete
import io.github.rosemoe.sora.lang.styling.CodeBlock
import io.github.rosemoe.sora.lang.styling.Span
import io.github.rosemoe.sora.lang.styling.TextStyle
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.util.ArrayList
import io.github.rosemoe.sora.util.IntPair
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import java.util.Stack

// 定义了一个继承 AsyncIncrementalAnalyzeManager<State, Long> 的类 LuaIncrementalAnalyzeManager
class LuaIncrementalAnalyzeManager : AsyncIncrementalAnalyzeManager<State, Long>() {
    // 定义了一个 ThreadLocal 变量 tokenizerProvider，用来存储 LuaTextTokenizer 的实例
    private val tokenizerProvider = ThreadLocal<LuaTextTokenizer>()
    // 定义了一个 SyncIdentifiers 类型的变量 identifiers，用来存储标识符的信息
    var identifiers = IdentifierAutoComplete.SyncIdentifiers()

    // 定义了一个同步方法 obtainTokenizer，用来获取 LuaTextTokenizer 的实例
    @Synchronized
    private fun obtainTokenizer(): LuaTextTokenizer {
        var res = tokenizerProvider.get()
        if (res == null) {
            res = LuaTextTokenizer("")
            tokenizerProvider.set(res)
        }
        return res
    }

    // 重写了 computeBlocks 方法，用来计算代码块
    override fun computeBlocks(text: Content, delegate: CodeBlockAnalyzeDelegate): List<CodeBlock> {
        // 定义了一些变量
        val stack = Stack<CodeBlock>()
        val blocks = ArrayList<CodeBlock>()
        var maxSwitch = 0
        var currSwitch = 0
        val brackets = SimpleBracketsCollector()
        val bracketsStack = Stack<Long>()
        var i = 0
        // 循环每一行
        while (i < text.lineCount && delegate.isNotCancelled) {
            val state = getState(i)
            // 判断是否需要检查标识符
            val checkForIdentifiers =
                state.state!!.state == STATE_NORMAL || state.state!!.state == STATE_INCOMPLETE_COMMENT && state.tokens.size > 1
            if (state.state!!.hasBraces || checkForIdentifiers) {
                // 遍历 tokens
                for (i1 in state.tokens.indices) {
                    val tokenRecord = state.tokens[i1]
                    val token = IntPair.getFirst(tokenRecord!!)
                    if (token == ORDINAL_LBRACE) {
                        val offset = IntPair.getSecond(tokenRecord)
                        if (stack.isEmpty()) {
                            if (currSwitch > maxSwitch) {
                                maxSwitch = currSwitch
                            }
                            currSwitch = 0
                        }
                        currSwitch++
                        val block = CodeBlock()
                        block.startLine = i
                        block.startColumn = offset
                        stack.push(block)
                    } else if (token == ORDINAL_RBRACE) {
                        val offset = IntPair.getSecond(tokenRecord)
                        if (!stack.isEmpty()) {
                            val block = stack.pop()
                            block.endLine = i
                            block.endColumn = offset
                            if (block.startLine != block.endLine) {
                                blocks.add(block)
                            }
                        }
                    }
                    val type = getType(token)
                    if (type > 0) {
                        if (isStart(token)) {
                            bracketsStack.push(
                                IntPair.pack(
                                    type, text.getCharIndex(
                                        i, IntPair.getSecond(
                                            tokenRecord
                                        )
                                    )
                                )
                            )
                        } else {
                            if (!bracketsStack.isEmpty()) {
                                var record = bracketsStack.pop()
                                val typeRecord = IntPair.getFirst(record!!)
                                if (typeRecord == type) {
                                    brackets.add(
                                        IntPair.getSecond(record), text.getCharIndex(
                                            i, IntPair.getSecond(
                                                tokenRecord
                                            )
                                        )
                                    )
                                } else if (type == 3) {
                                    // Bad syntax, try to find type 3
                                    while (!bracketsStack.isEmpty()) {
                                        record = bracketsStack.pop()
                                        if (IntPair.getFirst(record) == 3) {
                                            brackets.add(
                                                IntPair.getSecond(record), text.getCharIndex(
                                                    i, IntPair.getSecond(
                                                        tokenRecord
                                                    )
                                                )
                                            )
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            i++
        }
        // 更新 BracketProvider
        if (delegate.isNotCancelled) {
            withReceiver { r: StyleReceiver -> r.updateBracketProvider(this, brackets) }
        }
        return blocks
    }

    // 重写了 getInitialState 方法，用来获取初始状态
    override fun getInitialState(): State {
        return State()
    }

    // 重写了 stateEquals 方法，用来比较两个状态是否相等
    override fun stateEquals(state: State, another: State): Boolean {
        return state == another
    }

    // 重写了 onAddState 方法，用来在添加状态时更新标识符信息
    override fun onAddState(state: State) {
        if (state.identifiers != null) {
            for (identifier in state.identifiers!!) {
                identifiers.identifierIncrease(identifier)
            }
        }
    }

    // 重写了 onAbandonState 方法，用来在放弃状态时更新标识符信息
    override fun onAbandonState(state: State) {
        if (state.identifiers != null) {
            for (identifier in state.identifiers!!) {
                identifiers.identifierDecrease(identifier)
            }
        }
    }

    // 重写了 reset 方法，用来重置标识符信息
    override fun reset(content: ContentReference, extraArguments: Bundle) {
        super.reset(content, extraArguments)
        identifiers.clear()
    }

    // 重写了 tokenizeLine 方法，用来对一行进行分词
    override fun tokenizeLine(
        line: CharSequence,
        state: State,
        lineIndex: Int
    ): IncrementalAnalyzeManager.LineTokenizeResult<State, Long> {
        val tokens = ArrayList<Long>()
        var newState = 0
        val stateObj = State()
        if (state.state == STATE_NORMAL) {
            newState = tokenizeNormal(line, 0, tokens, stateObj)
        } else if (state.state == STATE_INCOMPLETE_COMMENT) {
            val res = tryFillIncompleteComment(line, tokens)
            newState = IntPair.getFirst(res)
            newState = if (newState == STATE_NORMAL) {
                tokenizeNormal(line, IntPair.getSecond(res), tokens, stateObj)
            } else {
                STATE_INCOMPLETE_COMMENT
            }
        }
        if (tokens.isEmpty()) {
            tokens.add(token(Tokens.UNKNOWN, 0))
            }
            stateObj.state = newState
            return IncrementalAnalyzeManager.LineTokenizeResult(stateObj, tokens)
        }

        /**
         * @return state and offset
         */
        private fun tryFillIncompleteComment(line: CharSequence, tokens: MutableList<Long>): Long {
            var pre = '\u0000'
            var cur = '\u0000'
            var offset = 0
            while ((pre != '*' || cur != '/') && offset < line.length) {
                pre = cur
                cur = line[offset]
                offset++
            }
            if (pre == '*' && cur == '/') {
                tokens.add(token(Tokens.LONG_COMMENT_COMPLETE, 0))
                return IntPair.pack(STATE_NORMAL, offset)
            }
            tokens.add(token(Tokens.LONG_COMMENT_INCOMPLETE, 0))
            return IntPair.pack(STATE_INCOMPLETE_COMMENT, offset)
        }

        private fun tokenizeNormal(
            text: CharSequence,
            offset: Int,
            tokens: MutableList<Long>,
            st: State
        ): Int {
            val tokenizer = obtainTokenizer()
            tokenizer.reset(text)
            tokenizer.offset = offset
            var token: Tokens
            var state = STATE_NORMAL
            while (tokenizer.nextToken().also { token = it } != Tokens.EOF) {
                tokens.add(token(token, tokenizer.offset))
                if (token == Tokens.LBRACE || token == Tokens.RBRACE) {
                    st.hasBraces = true
                }
                if (token == Tokens.IDENTIFIER) {
                    st.addIdentifier(tokenizer.tokenText)
                }
                if (token == Tokens.LONG_COMMENT_INCOMPLETE) {
                    state = STATE_INCOMPLETE_COMMENT
                    break
                }
            }
            return state
        }

        override fun generateSpansForLine(lineResult: IncrementalAnalyzeManager.LineTokenizeResult<State, Long>): List<Span> {
            val spans = ArrayList<Span>()
            val tokens = lineResult.tokens
            var previous = Tokens.UNKNOWN
            var classNamePrevious = false
            for (i in tokens.indices) {
                val tokenRecord = tokens[i]
                val token = ordinalToToken(IntPair.getFirst(tokenRecord))
                val offset = IntPair.getSecond(tokenRecord)
                when (token) {
                    Tokens.WHITESPACE, Tokens.NEWLINE -> spans.add(
                        Span.obtain(
                            offset,
                            TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL)
                        )
                    )

                    Tokens.CHARACTER_LITERAL, Tokens.FLOATING_POINT_LITERAL, Tokens.INTEGER_LITERAL, Tokens.STRING -> {
                        classNamePrevious = false
                        spans.add(
                            Span.obtain(
                                offset,
                                TextStyle.makeStyle(EditorColorScheme.LITERAL, true)
                            )
                        )
                    }

                    Tokens.INT, Tokens.LONG, Tokens.BOOLEAN, Tokens.BYTE, Tokens.CHAR, Tokens.FLOAT, Tokens.DOUBLE, Tokens.SHORT, Tokens.VOID, Tokens.VAR -> {
                        classNamePrevious = true
                        spans.add(
                            Span.obtain(
                                offset,
                                TextStyle.makeStyle(EditorColorScheme.KEYWORD, 0, true, false, false)
                            )
                        )
                    }

                    Tokens.AND, Tokens.BREAK, Tokens.CASE, Tokens.CATCH, Tokens.CLASS, Tokens.CONST, Tokens.CONTINUE, Tokens.DEFAULT, Tokens.DO, Tokens.ELSE, Tokens.ENUM, Tokens.EXTENDS, Tokens.FINAL, Tokens.FINALLY, Tokens.FOR, Tokens.GOTO, Tokens.IF, Tokens.IMPLEMENTS, Tokens.IMPORT, Tokens.INSTANCEOF, Tokens.INTERFACE, Tokens.NATIVE, Tokens.NEW, Tokens.NOT, Tokens.OR, Tokens.PACKAGE, Tokens.PRIVATE, Tokens.PROTECTED, Tokens.PUBLIC, Tokens.RETURN, Tokens.STATIC, Tokens.STRICTFP, Tokens.SUPER, Tokens.SWITCH, Tokens.SYNCHRONIZED, Tokens.THIS, Tokens.THROW, Tokens.THROWS, Tokens.TRANSIENT, Tokens.TRY, Tokens.VOLATILE, Tokens.WHILE -> {
                        classNamePrevious = false
                        spans.add(
                            Span.obtain(
                                offset,
                                TextStyle.makeStyle(EditorColorScheme.KEYWORD, 0, true, false, false)
                            )
                        )
                    }

                    Tokens.LINE_COMMENT, Tokens.LONG_COMMENT_COMPLETE, Tokens.LONG_COMMENT_INCOMPLETE -> spans.add(
                        Span.obtain(
                            offset,
                            TextStyle.makeStyle(EditorColorScheme.COMMENT, 0, false, true, false, true)
                        )
                    )

                    Tokens.IDENTIFIER -> {
                        var type = EditorColorScheme.IDENTIFIER_NAME
                        if (classNamePrevious) {
                            type = EditorColorScheme.IDENTIFIER_VAR
                            classNamePrevious = false
                        } else {
                            if (previous == Tokens.AT) {
                                type = EditorColorScheme.ANNOTATION
                            } else {
                                // Peek next token
                                var j = i + 1
                                var next: Tokens? = Tokens.UNKNOWN
                                label@ while (j < tokens.size) {
                                    next = ordinalToToken(
                                        IntPair.getFirst(
                                            tokens[j]
                                        )
                                    )
                                    when (next) {
                                        Tokens.WHITESPACE, Tokens.NEWLINE, Tokens.LONG_COMMENT_INCOMPLETE, Tokens.LONG_COMMENT_COMPLETE, Tokens.LINE_COMMENT -> {}
                                        else -> break@label
                                    }
                                    j++
                                }
                                if (next == Tokens.LPAREN) {
                                    type = EditorColorScheme.FUNCTION_NAME
                                } else {
                                    classNamePrevious = true
                                }
                            }
                        }
                        spans.add(Span.obtain(offset, TextStyle.makeStyle(type)))
                    }

                    else -> {
                        if (token == Tokens.LBRACK || token == Tokens.RBRACK && previous == Tokens.LBRACK) {
                            spans.add(Span.obtain(offset, EditorColorScheme.OPERATOR.toLong()))
                            break
                        }
                        classNamePrevious = false
                        spans.add(Span.obtain(offset, EditorColorScheme.OPERATOR.toLong()))
                    }
                }
                when (token) {
                    Tokens.LINE_COMMENT, Tokens.LONG_COMMENT_COMPLETE, Tokens.LONG_COMMENT_INCOMPLETE, Tokens.WHITESPACE, Tokens.NEWLINE -> {}
                    else -> previous = token!!
                }
            }
            return spans
        }

        companion object {
            private const val STATE_NORMAL = 0
            private const val STATE_INCOMPLETE_COMMENT = 1
            private fun getType(token: Int): Int {
                if (token == Tokens.LBRACE.ordinal || token == Tokens.RBRACE.ordinal) {
                    return 3
                }
                if (token == Tokens.LBRACK.ordinal || token == Tokens.RBRACK.ordinal) {
                    return 2
                }
                return if (token == Tokens.LPAREN.ordinal || token == Tokens.RPAREN.ordinal) {
                    1
                } else 0
            }

            private fun isStart(token: Int): Boolean {
                return token == Tokens.LBRACE.ordinal || token == Tokens.LBRACK.ordinal || token == Tokens.LPAREN.ordinal
            }

            private fun token(type: Tokens, column: Int): Long {
                return IntPair.pack(type.ordinal, column)
            }

            private fun ordinalToToken(ordinal: Int): Tokens? {
                if (mapping == null) {
                    val tokens = Tokens.values()
                    mapping = arrayOfNulls(tokens.size)
                    for (token in tokens) {
                        mapping!![token.ordinal] = token
                    }
                }
                return mapping!![ordinal]
            }

            private val ORDINAL_LBRACE = Tokens.LBRACE.ordinal
            private val ORDINAL_RBRACE = Tokens.RBRACE.ordinal
            private var mapping: Array<Tokens?>? = null

        }
    }
