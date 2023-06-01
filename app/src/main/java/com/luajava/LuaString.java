package com.luajava;

import androidx.annotation.NonNull;

/**
 * Created by nirenr on 2018/12/17.
 */

public class LuaString implements CharSequence {

    private final byte[] mByte;

    public LuaString(String string) {
        mByte=string.getBytes();
    }

    public LuaString(byte[] string) {
        mByte=string;
    }

    public byte[] toByteArray() {
        return mByte;
    }

    @Override
    public int length() {
        return mByte.length;
    }

    @Override
    public char charAt(int index) {
        return (char) mByte[index];
    }

    @NonNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return new String(mByte,start,end);
    }

    @NonNull
    @Override
    public String toString() {
        return new String(mByte);
    }
}
