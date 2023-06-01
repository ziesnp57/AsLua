package com.android.cglib.proxy;

import java.lang.reflect.Method;



public interface MethodFilter {
    boolean filter(Method mode,String name);
}
