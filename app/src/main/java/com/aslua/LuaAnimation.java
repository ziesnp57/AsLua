package com.aslua;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.luajava.LuaError;
import com.luajava.LuaFunction;

/**
 * Created by Administrator on 2016/12/08 0008.
 */

public class LuaAnimation extends Animation {

    private final LuaContext mContext; // Lua上下文对象
    private LuaFunction mAnimation; // Lua动画函数
    private LuaFunction mApplyTransformation; // Lua应用变换函数

    public LuaAnimation(LuaFunction animation) {
        mAnimation = animation; // 设置Lua动画函数
        mContext = mAnimation.getLuaState().getContext(); // 获取Lua上下文对象
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t); // 调用父类的变换方法
        try {
            mAnimation.call(interpolatedTime, t); // 调用Lua动画函数
            if (mApplyTransformation == null) {
                Object r = mAnimation.call(interpolatedTime, t, this); // 调用Lua动画函数，并传入当前对象
                if (r != null && r instanceof LuaFunction)
                    mApplyTransformation = (LuaFunction) r; // 设置Lua应用变换函数
            }
            if (mApplyTransformation != null) {
                mApplyTransformation.call(interpolatedTime, t); // 调用Lua应用变换函数
            }
        } catch (LuaError e) {
            mContext.sendError("applyTransformation", e); // 发送错误信息到Lua上下文对象
        }
    }

    @Override
    protected float resolveSize(int type, float value, int size, int parentSize) {
        return super.resolveSize(type, value, size, parentSize); // 解析尺寸大小
    }
}
