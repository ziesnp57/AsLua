package com.yongle.aslua.ui.aeiun

import android.content.Context
import android.content.res.Configuration
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.widget.CodeEditor

fun switchThemeIfRequired(context: Context, editor: CodeEditor) {
// 检查当前是否是夜间模式
    if ((context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {

            ThemeRegistry.getInstance().setTheme("darcula")

    } else {


            ThemeRegistry.getInstance().setTheme("quietlight")

    }
// 使编辑器无效，以重新绘制所有内容
    editor.invalidate()
}