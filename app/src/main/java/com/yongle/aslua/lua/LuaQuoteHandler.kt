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

import io.github.rosemoe.sora.lang.QuickQuoteHandler
import io.github.rosemoe.sora.lang.QuickQuoteHandler.HandleResult
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.lang.styling.StylesUtils
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.TextRange

// LuaQuoteHandler 类实现了 QuickQuoteHandler 接口
class LuaQuoteHandler : QuickQuoteHandler {
    // 重写 onHandleTyping 方法，用于处理输入事件
    override fun onHandleTyping(
        candidateCharacter: String, // 候选字符
        text: Content, // 当前文本内容
        cursor: TextRange, // 光标所在位置
        style: Styles? // 当前样式
    ): HandleResult {
        // 判断样式是否支持自动补全，以及候选字符是否为双引号，光标是否在同一行
        if (!StylesUtils.checkNoCompletion(style, cursor.start) && !StylesUtils.checkNoCompletion(
                style,
                cursor.end
            ) && "\"" == candidateCharacter && cursor.start.line == cursor.end.line
        ) {
            // 在光标所在行的起始位置和结束位置分别插入双引号
            text.insert(cursor.start.line, cursor.start.column, "\"")
            text.insert(cursor.end.line, cursor.end.column + 1, "\"")
            // 返回处理结果，包括是否消费了该事件和新的光标位置
            return HandleResult(
                true,
                TextRange(
                    text.indexer.getCharPosition(cursor.startIndex + 1),
                    text.indexer.getCharPosition(cursor.endIndex + 1)
                )
            )
        }
        // 如果没有消费该事件，则返回 NOT_CONSUMED
        return HandleResult.NOT_CONSUMED
    }
}
