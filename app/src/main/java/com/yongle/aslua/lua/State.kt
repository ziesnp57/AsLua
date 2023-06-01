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

import java.util.Objects

class State {
    var state = 0  // 状态值，默认为0
    var hasBraces = false  // 是否包含花括号，默认为false
    var identifiers: MutableList<String>? = null  // 标识符列表，初始值为null

    // 添加标识符到标识符列表中
    fun addIdentifier(idt: CharSequence) {
        if (identifiers == null) {
            identifiers = ArrayList()
        }
        if (idt is String) {
            identifiers!!.add(idt)
        } else {
            identifiers!!.add(idt.toString())
        }
    }

    // 判断两个State对象是否相等，只比较state和hasBraces属性
    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val state1 = other as State
        return state == state1.state && hasBraces == state1.hasBraces
    }

    // 计算State对象的哈希值，只考虑state和hasBraces属性
    override fun hashCode(): Int {
        return Objects.hash(state, hasBraces)
    }
}
